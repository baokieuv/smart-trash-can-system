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
static EventGroupHandle_t g_event_group = NULL;
static QueueHandle_t g_image_queue = NULL;
static TaskHandle_t g_telemetry_task_handle = NULL;

// ==================== LED & Buzzer Control ====================

static void led_indicator_init(void) {
    gpio_config_t led_cfg = {
        .pin_bit_mask = (1ULL << LED_RECYCLABLE_PIN) | 
                       (1ULL << LED_COMPOSTABLE_PIN) | 
                       (1ULL << LED_HAZARDOUS_PIN) |
                       (1ULL << LED_STATUS_PIN),
        .mode = GPIO_MODE_OUTPUT,
        .pull_up_en = GPIO_PULLUP_DISABLE,
        .pull_down_en = GPIO_PULLDOWN_ENABLE,
        .intr_type = GPIO_INTR_DISABLE,
    };
    ESP_ERROR_CHECK(gpio_config(&led_cfg));
    
    // Turn off all LEDs
    gpio_set_level(LED_RECYCLABLE_PIN, 0);
    gpio_set_level(LED_COMPOSTABLE_PIN, 0);
    gpio_set_level(LED_HAZARDOUS_PIN, 0);
    gpio_set_level(LED_STATUS_PIN, 0);
    
    ESP_LOGI(TAG, "LED indicators initialized");
}

static void buzzer_init(void) {
    gpio_config_t buzzer_cfg = {
        .pin_bit_mask = (1ULL << BUZZER_PIN),
        .mode = GPIO_MODE_OUTPUT,
        .pull_up_en = GPIO_PULLUP_DISABLE,
        .pull_down_en = GPIO_PULLDOWN_DISABLE,
        .intr_type = GPIO_INTR_DISABLE,
    };
    ESP_ERROR_CHECK(gpio_config(&buzzer_cfg));
    gpio_set_level(BUZZER_PIN, 0);
    
    ESP_LOGI(TAG, "Buzzer initialized");
}

static void beep_double(void) {
    gpio_set_level(BUZZER_PIN, 1);
    vTaskDelay(pdMS_TO_TICKS(200));
    gpio_set_level(BUZZER_PIN, 0);
    vTaskDelay(pdMS_TO_TICKS(200));
    gpio_set_level(BUZZER_PIN, 1);
    vTaskDelay(pdMS_TO_TICKS(200));
    gpio_set_level(BUZZER_PIN, 0);
}

static void indicate_waste_category(waste_category_t category) {
    // Turn off all LEDs first
    gpio_set_level(LED_RECYCLABLE_PIN, 0);
    gpio_set_level(LED_COMPOSTABLE_PIN, 0);
    gpio_set_level(LED_HAZARDOUS_PIN, 0);
    
    // Turn on appropriate LED
    switch (category) {
        case WASTE_RECYCLABLE:
            ESP_LOGI(TAG, "Indicating RECYCLABLE waste");
            gpio_set_level(LED_RECYCLABLE_PIN, 1);
            break;
        case WASTE_COMPOSTABLE:
            ESP_LOGI(TAG, "Indicating COMPOSTABLE waste");
            gpio_set_level(LED_COMPOSTABLE_PIN, 1);
            break;
        case WASTE_HAZARDOUS:
            ESP_LOGI(TAG, "Indicating HAZARDOUS waste");
            gpio_set_level(LED_HAZARDOUS_PIN, 1);
            break;
        default:
            ESP_LOGW(TAG, "Unknown waste category");
            break;
    }
    
    // Keep LED on for indication duration
    vTaskDelay(pdMS_TO_TICKS(LED_INDICATION_DURATION_MS));
    
    // Turn off LED
    gpio_set_level(LED_RECYCLABLE_PIN, 0);
    gpio_set_level(LED_COMPOSTABLE_PIN, 0);
    gpio_set_level(LED_HAZARDOUS_PIN, 0);
}

// ==================== Button Handler ====================

static volatile int64_t g_button_press_time = 0;

static void IRAM_ATTR button_isr_handler(void* arg) {
    int64_t now = esp_timer_get_time() / 1000;  // Convert to ms
    
    if (gpio_get_level(BTN_CONFIG_PIN) == 0) {
        // Button pressed
        g_button_press_time = now;
    } else {
        // Button released
        int64_t press_duration = now - g_button_press_time;
        
        if (press_duration >= BUTTON_LONG_PRESS_MS) {
            // Long press: enter config mode
            BaseType_t xHigherPriorityTaskWoken = pdFALSE;
            xEventGroupSetBitsFromISR(g_event_group, CONFIG_MODE_BIT, &xHigherPriorityTaskWoken);
            portYIELD_FROM_ISR(xHigherPriorityTaskWoken);
        }
        
        g_button_press_time = 0;
    }
}

static void button_init(void) {
    gpio_config_t btn_cfg = {
        .pin_bit_mask = (1ULL << BTN_CONFIG_PIN),
        .mode = GPIO_MODE_INPUT,
        .pull_up_en = GPIO_PULLUP_ENABLE,
        .pull_down_en = GPIO_PULLDOWN_DISABLE,
        .intr_type = GPIO_INTR_ANYEDGE,
    };
    ESP_ERROR_CHECK(gpio_config(&btn_cfg));
    
    ESP_ERROR_CHECK(gpio_install_isr_service(0));
    ESP_ERROR_CHECK(gpio_isr_handler_add(BTN_CONFIG_PIN, button_isr_handler, NULL));
    
    ESP_LOGI(TAG, "Button initialized (GPIO%d)", BTN_CONFIG_PIN);
}

// ==================== Sensor Detection Task ====================

static void sensor_detection_task(void *param) {
    bool person_detected = false;
    
    ESP_LOGI(TAG, "Sensor detection task started");
    
    while (1) {
        // Check if in config mode
        if (waste_manager_is_config_mode()) {
            vTaskDelay(pdMS_TO_TICKS(1000));
            continue;
        }
        
        // Read PIR sensor
        bool pir_state = pir_sensor_read();
        
        if (pir_state && !person_detected) {
            person_detected = true;
            ESP_LOGI(TAG, "PIR: Person detected!");
            
            // Stabilization delay
            vTaskDelay(pdMS_TO_TICKS(500));
            
            // Read ultrasonic sensor
            float distance = ultrasonic_sensor_read_distance();
            
            if (distance > 0 && distance < DISTANCE_THRESHOLD_CM) {
                ESP_LOGI(TAG, "Ultrasonic: Waste detected at %.2f cm", distance);
                ESP_LOGI(TAG, "Triggering camera capture!");
                
                // Sound alert
                beep_double();
                
                // Capture image
                camera_fb_t *fb = NULL;
                if (camera_handler_capture(&fb) == ESP_OK) {
                    // Send to classification queue
                    if (xQueueSend(g_image_queue, &fb, pdMS_TO_TICKS(100)) != pdPASS) {
                        ESP_LOGW(TAG, "Image queue full, discarding");
                        camera_handler_return_fb(fb);
                    }
                }
                
                // Update fill level
                waste_manager_update_fill_level(distance);
                
                // Wait before next detection
                vTaskDelay(pdMS_TO_TICKS(5000));
            } else if (distance > 0) {
                ESP_LOGD(TAG, "No waste detected (distance: %.2f cm)", distance);
            }
        } else if (!pir_state && person_detected) {
            person_detected = false;
            ESP_LOGI(TAG, "PIR: Person left");
        }
        
        vTaskDelay(pdMS_TO_TICKS(SENSOR_CHECK_INTERVAL_MS));
    }
}

// ==================== Image Classification Task ====================

static void classification_task(void *param) {
    camera_fb_t *fb = NULL;
    classification_result_t result;
    
    ESP_LOGI(TAG, "Classification task started");
    
    while (1) {
        if (xQueueReceive(g_image_queue, &fb, portMAX_DELAY) == pdTRUE) {
            if (!fb) {
                ESP_LOGW(TAG, "Received NULL frame");
                continue;
            }
            
            ESP_LOGI(TAG, "Processing image for classification...");
            
            // Send to AI server
            if (http_client_classify_waste(fb, &result) == ESP_OK) {
                ESP_LOGI(TAG, "Classification result: %s (%.1f%% confidence)",
                         result.description, result.confidence);
                
                // Record waste statistics
                waste_manager_record_waste(result.category);
                
                // Show LED indication
                indicate_waste_category(result.category);
            } else {
                ESP_LOGE(TAG, "Classification failed");
            }
            
            // Return frame buffer
            camera_handler_return_fb(fb);
            fb = NULL;
        }
    }
}

// ==================== Telemetry Task ====================

static void telemetry_task(void *param) {
    waste_stats_t stats;
    TickType_t last_send_time = xTaskGetTickCount();
    
    ESP_LOGI(TAG, "Telemetry task started");
    
    while (1) {
        // Wait for WiFi and MQTT connection
        EventBits_t bits = xEventGroupWaitBits(g_event_group,
                                               WIFI_CONNECTED_BIT | MQTT_CONNECTED_BIT,
                                               pdFALSE, pdTRUE,
                                               pdMS_TO_TICKS(10000));
        
        if ((bits & (WIFI_CONNECTED_BIT | MQTT_CONNECTED_BIT)) == 
            (WIFI_CONNECTED_BIT | MQTT_CONNECTED_BIT)) {
            
            // Check if it's time to send
            TickType_t now = xTaskGetTickCount();
            if ((now - last_send_time) >= pdMS_TO_TICKS(TELEMETRY_INTERVAL_MS)) {
                
                // Get current statistics
                if (waste_manager_get_stats(&stats) == ESP_OK) {
                    ESP_LOGI(TAG, "Sending telemetry - Total: %lu, Fill: %.1f%%",
                             stats.total_count, stats.current_fill_level);
                    
                    // Send via MQTT
                    if (mqtt_send_telemetry(&stats) == ESP_OK) {
                        last_send_time = now;
                    } else {
                        ESP_LOGW(TAG, "Failed to send telemetry");
                    }
                }
            }
        }
        
        vTaskDelay(pdMS_TO_TICKS(5000));
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
        vTaskDelay(pdMS_TO_TICKS(5000));
    }
}

// ==================== Configuration Mode Handler ====================

static void handle_config_mode(void) {
    ESP_LOGI(TAG, "=== ENTERING CONFIGURATION MODE ===");
    
    // Stop normal operations
    waste_manager_enter_config_mode();
    
    // Blink status LED to indicate config mode
    for (int i = 0; i < 5; i++) {
        gpio_set_level(LED_STATUS_PIN, 1);
        vTaskDelay(pdMS_TO_TICKS(200));
        gpio_set_level(LED_STATUS_PIN, 0);
        vTaskDelay(pdMS_TO_TICKS(200));
    }
    
    // Stop current WiFi
    wifi_stop();
    
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
    ESP_ERROR_CHECK(waste_manager_init());
    
    // Initialize GPIO
    led_indicator_init();
    buzzer_init();
    button_init();
    
    // Initialize sensors
    ESP_ERROR_CHECK(pir_sensor_init());
    ESP_ERROR_CHECK(ultrasonic_sensor_init());
    
    // Initialize camera
    if (camera_handler_init() != ESP_OK) {
        ESP_LOGE(TAG, "Failed to initialize camera - system halted");
        return;
    }
    
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
        
        // Connect to WiFi
        if (wifi_start_station_mode(ssid, password)) {
            ESP_LOGI(TAG, "Connected to WiFi successfully");
            gpio_set_level(LED_STATUS_PIN, 1);  // Status LED on
            
            // Initialize HTTP client
            ESP_ERROR_CHECK(http_client_init());
            
            // Initialize and start MQTT
            ESP_ERROR_CHECK(mqtt_client_init());
            ESP_ERROR_CHECK(mqtt_client_start());
            
            // Start waste manager
            ESP_ERROR_CHECK(waste_manager_start());
            
            // Create tasks
            xTaskCreate(sensor_detection_task, "sensor_task", 
                       SENSOR_TASK_STACK_SIZE, NULL, 5, NULL);
            xTaskCreate(classification_task, "classify_task", 
                       HTTP_TASK_STACK_SIZE, NULL, 5, NULL);
            xTaskCreate(telemetry_task, "telemetry_task", 
                       TELEMETRY_TASK_STACK_SIZE, NULL, 4, &g_telemetry_task_handle);
            

            ESP_LOGI(TAG, "=== System Ready ===");
            
            // Monitor for config mode trigger
            while (1) {
                EventBits_t bits = xEventGroupWaitBits(g_event_group,
                                                       CONFIG_MODE_BIT,
                                                       pdTRUE,  // Clear on exit
                                                       pdFALSE,
                                                       portMAX_DELAY);
                
                if (bits & CONFIG_MODE_BIT) {
                    handle_config_mode();
                }
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