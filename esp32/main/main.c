#include <stdio.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/event_groups.h"
#include "freertos/queue.h"
#include "esp_system.h"
#include "esp_log.h"
#include "nvs_flash.h"
#include "esp_event.h"
#include "esp_netif.h"
#include "esp_timer.h"
#include "driver/gpio.h"

// Application modules
#include "gpio_handler.h"
#include "config.h"
#include "wifi_manager.h"
#include "nvs_storage.h"
#include "http_server.h"
#include "http_client.h"
#include "mqtt_client_tb.h"
#include "waste_manager.h"
#include "camera_handler.h"
#include "pir_sensor.h"
#include "ultrasonic_sensor.h"
#include "ota.h"

static const char *TAG = "SMART_BIN";

// Global handles
EventGroupHandle_t g_event_group = NULL;
static QueueHandle_t g_image_queue = NULL;

// ==================== Sensor Detection Task ====================
static void sensor_detection_task(void *param) {
    // bool person_detected = false;
    
    ESP_LOGI(TAG, "Sensor detection task started");
    
    while (1) {
        // Check if in config mode
        // ESP_LOGI(TAG, "Sensor task");
        if (xEventGroupGetBits(g_event_group) & CONFIG_MODE_BIT) {
            // ESP_LOGI(TAG, "In config mode");
            vTaskDelay(pdMS_TO_TICKS(1000));
            continue;
        }
        
        // Read PIR sensor
        // bool pir_state = pir_sensor_read();
        if(wifi_is_connected()){
        // if (pir_state && !person_detected) {
        //     person_detected = true;
            // ESP_LOGI(TAG, "PIR: Person detected!");
            
            // Stabilization delay
            vTaskDelay(pdMS_TO_TICKS(500));
            
            // Read ultrasonic sensor
            float distance = ultrasonic_sensor_get_distance_for_detect();
            
            if (distance > 0 && distance < DISTANCE_THRESHOLD_CM) {
                ESP_LOGI(TAG, "Ultrasonic: Waste detected at %.2f cm", distance);
                ESP_LOGI(TAG, "Triggering camera capture!");
                
                // Sound alert
                beep_pattern(2, 200);
                
                // Capture image
                camera_fb_t *fb = NULL;
                if (camera_handler_capture(&fb) == ESP_OK) {
                    // Send to classification queue
                    if (xQueueSend(g_image_queue, &fb, pdMS_TO_TICKS(100)) != pdPASS) {
                        ESP_LOGW(TAG, "Image queue full, discarding");
                        camera_handler_return_fb(fb);
                    }
                }
                
                // read distance in bin -> check fill level
                // TODO
                // Update fill level -> thay đổi -> khi nhận diện xong 
                waste_manager_update_fill_level(ultrasonic_sensor_get_distance_for_check_full());
                
                // Wait before next detection
                vTaskDelay(pdMS_TO_TICKS(5000));
            }
        }
        // else if (!pir_state && person_detected) {
        //     person_detected = false;
        //     ESP_LOGI(TAG, "PIR: Person left");
        // }
        
        vTaskDelay(pdMS_TO_TICKS(SENSOR_CHECK_INTERVAL_MS));
    }
}

// ==================== Image Classification Task ====================

static void classification_task(void *param) {
    camera_fb_t *fb = NULL;
    classification_result_t result;
    waste_stats_t stats;
    
    ESP_LOGI(TAG, "Classification task started");
    
    while (1) {
        if (xQueueReceive(g_image_queue, &fb, portMAX_DELAY) == pdTRUE) {
            if (!fb) {
                ESP_LOGW(TAG, "Received NULL frame");
                continue;
            }
            
            ESP_LOGI(TAG, "Processing image for classification...");
            
            // Send to AI server
            if (wifi_is_connected() && http_client_classify_waste(fb, &result) == ESP_OK) {
                ESP_LOGI(TAG, "Classification result: %s (%.1f%% confidence)",
                         result.description, result.confidence);
                
                // Record waste statistics
                waste_manager_record_waste(result.category);
                
                // Show LED indication
                indicate_waste_category(result.category);

                if(mqtt_is_connected()){
                    if(waste_manager_get_stats(&stats) == ESP_OK){
                        mqtt_send_telemetry(&stats);
                        ESP_LOGI(TAG, "Telemetry sent after detection");
                    }
                }
            } else {
                ESP_LOGE(TAG, "Classification failed");
            }
            
            // Return frame buffer
            camera_handler_return_fb(fb);
            fb = NULL;
        }
    }
}

// ==================== Heartbeat Task ====================
static void heartbeat_task(void *param) {
    waste_stats_t stats;
    
    ESP_LOGI(TAG, "Heartbeat task started");
    
    while (1) {
        vTaskDelay(pdMS_TO_TICKS(HEARTBEAT_INTERVAL_MS));
        
        if (wifi_is_connected() && mqtt_is_connected()) {
            if (waste_manager_get_stats(&stats) == ESP_OK) {
                mqtt_send_telemetry(&stats);
                ESP_LOGI(TAG, "Heartbeat sent");
            }
        }
    }
}

// ==================== OTA Task ====================
static void ota_task(void *param){
    ESP_LOGI(TAG, "OTA task started");
    TickType_t last_send_time = xTaskGetTickCount();
    
    while(1){
        // Wait for WiFi connection
        EventBits_t bits = xEventGroupWaitBits(g_event_group,
                                               WIFI_CONNECTED_BIT,
                                               pdFALSE, pdTRUE,
                                               pdMS_TO_TICKS(10000));

        // 
        if(bits & WIFI_CONNECTED_BIT){
            // Check if it's time to send
            TickType_t now = xTaskGetTickCount();
            if ((now - last_send_time) >= pdMS_TO_TICKS(OTA_INTERVAL_MS)) {
                
 
                // Send via MQTT
                if (ota_handle(NULL) == ESP_OK) {
                    last_send_time = now;
                    ESP_LOGI(TAG, "ESP_HTTPS_OTA upgrade successful. Rebooting ...");
                    vTaskDelay(1000 / portTICK_PERIOD_MS);
                    esp_restart();
                } else {
                    ESP_LOGW(TAG, "Failed to update firmware");
                }
                
            }
        }
        vTaskDelay(pdMS_TO_TICKS(60000));
    }
}

// ==================== Configuration Mode Handler ====================

static void handle_config_mode(void) {
    ESP_LOGI(TAG, "=== ENTERING CONFIGURATION MODE ===");
    
    // Stop normal operations
    // waste_manager_enter_config_mode();
    xEventGroupSetBits(g_event_group, CONFIG_MODE_BIT);
    blink_led(5);
    
    // Stop current WiFi
    wifi_stop();
    mqtt_client_stop();
    
    // Start AP mode
    if (wifi_start_ap_mode() == ESP_OK) {
        ESP_LOGI(TAG, "AP Mode active - SSID: %s", AP_SSID);
        gpio_set_level(LED_STATUS_PIN, 1);  // Status LED on
        
        // Start HTTP server for configuration
        if (http_server_start() == ESP_OK) {
            ESP_LOGI(TAG, "Configuration server started");
            ESP_LOGI(TAG, "Connect to AP and go to http://192.168.4.1");
            
            // Wait for configuration (will restart after config)
            while (1) {
                EventBits_t bits = xEventGroupWaitBits(g_event_group, EXIT_CONFIG_MODE_BIT, pdTRUE, pdFAIL, portMAX_DELAY);

                if(bits & EXIT_CONFIG_MODE_BIT){
                    ESP_LOGI(TAG, "Exiting config mode...");
                    http_server_stop();
                    gpio_set_level(LED_STATUS_PIN, 0);
                    beep_pattern(3, 150); // Triple beep
                    vTaskDelay(pdMS_TO_TICKS(1000));
                    esp_restart();
                }
                vTaskDelay(pdMS_TO_TICKS(1000));
            }
        }
    }
}

// ==================== Main Application ====================

void app_main(void) {
    ESP_LOGI(TAG, "=== Smart Waste Bin System Starting ===");
    
    // Initialize NVS
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);
    
    // Initialize event loop
    ESP_ERROR_CHECK(esp_event_loop_create_default());
    
    // Initialize network interface
    ESP_ERROR_CHECK(esp_netif_init());
    
    // Create event group
    g_event_group = xEventGroupCreate();
    
    // Initialize waste manager
    ESP_ERROR_CHECK(waste_manager_init(g_event_group));
    
    // Initialize GPIO
    ESP_ERROR_CHECK(gpio_handler_init(g_event_group));

    // Initialize sensors
    ESP_ERROR_CHECK(pir_sensor_init());
    ESP_ERROR_CHECK(ultrasonic_sensor_init());
    
    // Initialize camera
    if (camera_handler_init() != ESP_OK) {
        ESP_LOGE(TAG, "Failed to initialize camera - system halted");
        return;
    }
    
    vTaskDelay(pdMS_TO_TICKS(500));

    // Create queues
    g_image_queue = xQueueCreate(IMAGE_QUEUE_LENGTH, sizeof(camera_fb_t *));
    if (!g_image_queue) {
        ESP_LOGE(TAG, "Failed to create image queue");
        return;
    }
    
    // Initialize WiFi
    ESP_ERROR_CHECK(wifi_manager_init(g_event_group));
    
    // Try to load saved WiFi credentials
    char ssid[SSID_MAX_LEN] = {0};
    char password[PASSWORD_MAX_LEN] = {0};
    
    if (nvs_load_wifi_config(ssid, password)) {
        ESP_LOGI(TAG, "Found saved WiFi credentials");
        vTaskDelay(pdMS_TO_TICKS(100));
        // Connect to WiFi
        if (wifi_start_station_mode(ssid, password)) {
            xEventGroupSetBits(g_event_group, WIFI_CONNECTED_BIT);
            ESP_LOGI(TAG, "Connected to WiFi successfully");
            gpio_set_level(LED_STATUS_PIN, 1);  // Status LED on
            
            // Initialize HTTP client
            ESP_ERROR_CHECK(http_client_init());
            
            // Initialize and start MQTT
            ESP_ERROR_CHECK(mqtt_client_init(g_event_group));
            ESP_ERROR_CHECK(mqtt_client_start());
            
            // Start waste manager
            ESP_ERROR_CHECK(waste_manager_start());
            
            // Create tasks
            xTaskCreate(sensor_detection_task, "sensor_task", 
                       SENSOR_TASK_STACK_SIZE, NULL, 5, NULL);
            xTaskCreate(classification_task, "classify_task", 
                       HTTP_TASK_STACK_SIZE, NULL, 5, NULL);
            xTaskCreate(heartbeat_task, "heartbeat_task", 
                       HEARTBEAT_TASK_STACK_SIZE, NULL, 5, NULL);
            
            ESP_LOGI(TAG, "=== System Ready ===");
            
            // Monitor for config mode trigger
            while (1) {
                EventBits_t bits = xEventGroupWaitBits(g_event_group,
                                                       CONFIG_MODE_BIT,
                                                       pdFALSE,  // Don't clear on read
                                                       pdFALSE,
                                                       portMAX_DELAY);
                
                if (bits & CONFIG_MODE_BIT) {
                    handle_config_mode();
                }
                vTaskDelay(pdMS_TO_TICKS(100));
            }
        } else {
            ESP_LOGE(TAG, "Failed to connect to WiFi");
            handle_config_mode();
        }
    } else {
        ESP_LOGI(TAG, "No saved WiFi credentials found");
        handle_config_mode();
    }
}