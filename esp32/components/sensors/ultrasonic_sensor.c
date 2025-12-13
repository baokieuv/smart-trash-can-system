#include "ultrasonic_sensor.h"
#include "config.h"
#include "driver/gpio.h"
#include "esp_timer.h"
#include "esp_log.h"
#include "esp_rom_sys.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"

static const char *TAG = "ULTRASONIC";

esp_err_t ultrasonic_sensor_init() {
    ESP_LOGI(TAG, "Initializing ultrasonic sensor...");
    // ESP_LOGI(TAG, "TRIG: GPIO%d, ECHO: GPIO%d", ULTRASONIC_TRIG_PIN, ULTRASONIC_ECHO_PIN);

    // Configure trigger pin as output
    gpio_config_t trig_cfg = {
        .pin_bit_mask = (1ULL << ULTRASONIC1_TRIG_PIN) | (1ULL << ULTRASONIC2_TRIG_PIN),
        .mode = GPIO_MODE_OUTPUT,
        .pull_up_en = GPIO_PULLUP_DISABLE,
        .pull_down_en = GPIO_PULLDOWN_DISABLE,
        .intr_type = GPIO_INTR_DISABLE,
    };

    esp_err_t err = gpio_config(&trig_cfg);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to configure TRIG pin: %s", esp_err_to_name(err));
        return err;
    }

    // Configure echo pin as input
    gpio_config_t echo_cfg = {
        .pin_bit_mask = (1ULL << ULTRASONIC1_ECHO_PIN) | (1ULL << ULTRASONIC_ECHO_SHARED_PIN),
        .mode = GPIO_MODE_INPUT,
        .pull_up_en = GPIO_PULLUP_DISABLE,
        .pull_down_en = GPIO_PULLDOWN_DISABLE,
        .intr_type = GPIO_INTR_DISABLE,
    };

    err = gpio_config(&echo_cfg);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to configure ECHO pin: %s", esp_err_to_name(err));
        return err;
    }

    // Set trigger to LOW initially
    gpio_set_level(ULTRASONIC1_TRIG_PIN, 0);
    gpio_set_level(ULTRASONIC2_TRIG_PIN, 0);

    ESP_LOGI(TAG, "Ultrasonic sensor initialized successfully");
    return ESP_OK;
}

static float ultrasonic_sensor_read(uint8_t trig_pin, uint8_t echo_pin){
    // Send 10us pulse to trigger
    gpio_set_level(trig_pin, 0);
    esp_rom_delay_us(2);
    gpio_set_level(trig_pin, 1);
    esp_rom_delay_us(10);
    gpio_set_level(trig_pin, 0);

    int64_t start_time = esp_timer_get_time();
    int64_t prev_time = start_time;
    // Wait for echo to go HIGH (with timeout)
    while (gpio_get_level(echo_pin) == 0) {
        start_time = esp_timer_get_time();
        if(start_time - prev_time > ECHO_TIMEOUT){
            ESP_LOGW(TAG, "Timeout 1");
            return ESP_ERR_TIMEOUT;
        }
    }

    // Measure echo HIGH duration
    int64_t end_time = start_time;
    while (gpio_get_level(echo_pin) == 1) {
        end_time = esp_timer_get_time();
        if(end_time - start_time > ECHO_TIMEOUT){
            ESP_LOGW(TAG, "Timeout 2");
            return ESP_ERR_TIMEOUT;
        }
    }

    gpio_set_level(trig_pin, 1);
    int64_t duration = end_time - start_time;
    float distance = (duration / 2.0) * (SOUND_SPEED / 10000.0);

    return distance;
}

static float ultrasonic_sensor_read_distance(uint8_t trig_pin, uint8_t echo_pin) {
    float readings[5] = { 0 };
    int valid_count = 0;

    for(int i = 0; i < 5; i++){
        float distance = ultrasonic_sensor_read(trig_pin, echo_pin);

        if(distance >= ULTRASONIC_MIN_DISTANCE && distance <= ULTRASONIC_MAX_DISTANCE){
            readings[valid_count++] = distance;
        }

        vTaskDelay(pdMS_TO_TICKS(50));
    }

    if (valid_count == 0) {
        ESP_LOGE(TAG, "No valid readings obtained");
        return -1.0f;
    }

    // Sort readings for median
    for (int i = 0; i < valid_count - 1; i++) {
        for (int j = i + 1; j < valid_count; j++) {
            if (readings[i] > readings[j]) {
                float temp = readings[i];
                readings[i] = readings[j];
                readings[j] = temp;
            }
        }
    }
    
    // Return median
    float median = readings[valid_count / 2];
    ESP_LOGD(TAG, "Distance: %.2f cm (from %d valid readings)", median, valid_count);
    
    return median;
}

float ultrasonic_sensor_get_distance_for_detect(){
    return ultrasonic_sensor_read_distance(ULTRASONIC1_TRIG_PIN, ULTRASONIC1_ECHO_PIN);
}

float ultrasonic_sensor_get_distance_for_check_full(){
    return ultrasonic_sensor_read_distance(ULTRASONIC2_TRIG_PIN, ULTRASONIC_ECHO_SHARED_PIN);
}