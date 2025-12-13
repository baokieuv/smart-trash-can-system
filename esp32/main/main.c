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
#include "esp_mac.h"
#include "esp_sleep.h"

// Application modules
#include "gpio_handler.h"
#include "config.h"
#include "wifi_manager.h"
#include "nvs_storage.h"
#include "http_client.h"
#include "bluetooth_manager.h"
#include "waste_manager.h"
#include "camera_handler.h"
#include "ultrasonic_sensor.h"
#include "servo.h"

static const char *TAG = "SMART_BIN";

// Global handles
EventGroupHandle_t g_event_group = NULL;
static QueueHandle_t g_image_queue = NULL;

static void enter_deep_sleep(void){
    ESP_LOGI(TAG, "Entering deep sleep mode...");

    // Stop all services
    wifi_stop();

    // Configure wakeup on button press
    esp_sleep_enable_ext0_wakeup(BTN_CONFIG_PIN, 0); // Wake on LOW (button press)
    
    // Clear active bit
    xEventGroupClearBits(g_event_group, DEVICE_ACTIVE_BIT);

    vTaskDelay(pdMS_TO_TICKS(100));
    esp_deep_sleep_start();
}


// ==================== Configuration Mode Handler ====================

static void handle_config_mode(void) {
    ESP_LOGI(TAG, "=== ENTERING CONFIGURATION MODE ===");
    
    // // Stop normal operations
    // xEventGroupSetBits(g_event_group, CONFIG_MODE_BIT);
    EventBits_t bits = xEventGroupGetBits(g_event_group);
    if((bits & CONFIG_MODE_BIT) || (bits & RESET_MODE_BIT)) return;

    blink_led(5);
    // Stop current WiFi
    wifi_stop();
    
    // Enable Bluetooth
    bluetooth_manager_init();
}

static void button_event_handler(void *args, esp_event_base_t base, int32_t id, void *event_data){
    switch (id) {
        case BUTTON_EVENT_SINGLE_CLICK:
            ESP_LOGI(TAG, "Button: Single click - Toggle sleep");
            // EventBits_t bits = xEventGroupGetBits(g_event_group);
            if(xEventGroupGetBits(g_event_group) & DEVICE_ACTIVE_BIT){
                enter_deep_sleep();
            }
            break;
        case BUTTON_EVENT_DOUBLE_CLICK:
            ESP_LOGI(TAG, "Button: Double click - Toggle config mode");
            EventBits_t bits = xEventGroupGetBits(g_event_group);
            if((bits & CONFIG_MODE_BIT) || (bits & RESET_MODE_BIT)){
                esp_restart();
            }else{
                xEventGroupSetBits(g_event_group, CONFIG_MODE_BIT);
                handle_config_mode();
            }
            break;
        case BUTTON_EVENT_LONG_PRESS:
            ESP_LOGI(TAG, "Button: Long press - In reset mode");
            xEventGroupSetBits(g_event_group, RESET_MODE_BIT);
            handle_config_mode();
        default:
            break;
    }
}

// ==================== Sensor Detection Task ====================
static void sensor_detection_task(void *param) {
    ESP_LOGI(TAG, "Sensor detection task started");
    
    while (1) {
        if (xEventGroupGetBits(g_event_group) & CONFIG_MODE_BIT) {
            vTaskDelay(pdMS_TO_TICKS(1000));
            continue;
        }

        if(wifi_is_connected()){
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
      
                // Update fill level -> thay đổi -> khi nhận diện xong 
                waste_manager_update_fill_level(ultrasonic_sensor_get_distance_for_check_full());
                
            }
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
            if (wifi_is_connected() && http_client_classify_waste(fb, &result) == ESP_OK) {
                ESP_LOGI(TAG, "Classification result: %s (%.1f%% confidence)",
                         result.description, result.confidence);
                
                // Record waste statistics
                waste_manager_record_waste(result.category);
                
                // Show LED indication
                // indicate_waste_category(result.category);
                trashlid_open(result.category);
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

    uint8_t mac[6];
    char deviceId[32] = { 0 };
    esp_read_mac(mac, ESP_MAC_BT);
    sprintf(deviceId, "%02X_%02X_%02X_%02X_%02X_%02X",
        mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
    
    while (1) {
        if (wifi_is_connected()) {
            waste_manager_update_fill_level(ultrasonic_sensor_get_distance_for_check_full());
            
            if(waste_manager_get_stats(&stats) == ESP_OK){
                http_client_send_device_data(deviceId, stats);
                ESP_LOGI(TAG, "Device data sent.");
            }
        }
        vTaskDelay(pdMS_TO_TICKS(HEARTBEAT_INTERVAL_MS));
    }
}


esp_err_t system_init(){
    // Initialize NVS
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND)
    {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);
    
    // Create event group
    g_event_group = xEventGroupCreate();
    if (!g_event_group) {
        ESP_LOGE(TAG, "Failed to create event group");
        return ESP_FAIL;
    }

    // Create queue
    g_image_queue = xQueueCreate(IMAGE_QUEUE_LENGTH, sizeof(camera_fb_t *));
    if (!g_image_queue) {
        ESP_LOGE(TAG, "Failed to create image queue");
        return ESP_FAIL;
    }
    
    xEventGroupSetBits(g_event_group, DEVICE_ACTIVE_BIT);
    ESP_ERROR_CHECK(waste_manager_init());
    ESP_ERROR_CHECK(gpio_handler_init());
    ESP_ERROR_CHECK(wifi_manager_init());
    ESP_ERROR_CHECK(trashlid_init());
    ESP_ERROR_CHECK(ultrasonic_sensor_init());
    
    if (camera_handler_init() != ESP_OK) {
        ESP_LOGE(TAG, "Failed to initialize camera - system halted");
        return ESP_FAIL;
    }

    ESP_ERROR_CHECK(esp_event_handler_instance_register(
        APP_BUTTON_EVENT, ESP_EVENT_ANY_ID, button_event_handler, NULL, NULL
    ));

    return ESP_OK;
}

static void start_sensor_tasks(void){

    //Start waste manager
    ESP_ERROR_CHECK(waste_manager_start());

    xTaskCreate(sensor_detection_task, "sensor_task", 
                    SENSOR_TASK_STACK_SIZE, NULL, 5, NULL);
    xTaskCreate(classification_task, "classify_task", 
                    HTTP_TASK_STACK_SIZE, NULL, 5, NULL);
    xTaskCreate(heartbeat_task, "heartbeat_task", 
                    HEARTBEAT_TASK_STACK_SIZE, NULL, 5, NULL);
}

static esp_err_t start_normal_mode(void){
    ESP_LOGI(TAG, "Starting normal operation mode...");
    char ssid[SSID_MAX_LEN], pass[PASSWORD_MAX_LEN];

    // Try to load WiFi config
    if (!nvs_load_wifi_config(ssid, pass)) {
        ESP_LOGW(TAG, "No WiFi config found");
        return ESP_FAIL;
    }

    ESP_LOGI(TAG, "WiFi config found - Connecting...");
    // Connect to WiFi
    if (!wifi_start_station_mode(ssid, pass)) {
        ESP_LOGE(TAG, "WiFi connection failed");
        return ESP_FAIL;
    }

    return ESP_OK;
}

void app_main(void){
    ESP_LOGI(TAG, "=== Smart Bin Monitor Starting ===");

    // Check if waking from deep sleep
    esp_sleep_wakeup_cause_t wakeup_reason = esp_sleep_get_wakeup_cause();
    if (wakeup_reason == ESP_SLEEP_WAKEUP_EXT0) {
        ESP_LOGI(TAG, "Woke up from deep sleep by button press");
    }

    ESP_ERROR_CHECK(system_init());

    start_sensor_tasks();

    if(start_normal_mode() != ESP_OK){
        ESP_LOGW(TAG, "Cannot connect to WiFi");
    }

    ESP_LOGI(TAG, "=== Initialization Complete ===");
}