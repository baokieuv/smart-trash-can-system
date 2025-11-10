#include "ultrasonic_sensor.h"
#include "config.h"
#include "driver/gpio.h"
#include "esp_timer.h"
#include "esp_log.h"
#include "esp_rom_sys.h"

static const char *TAG = "ULTRASONIC";

esp_err_t ultrasonic_sensor_init(void) {
    ESP_LOGI(TAG, "Initializing ultrasonic sensor...");
    ESP_LOGI(TAG, "TRIG: GPIO%d, ECHO: GPIO%d", ULTRASONIC_TRIG_PIN, ULTRASONIC_ECHO_PIN);

    // Configure trigger pin as output
    gpio_config_t trig_cfg = {
        .pin_bit_mask = (1ULL << ULTRASONIC_TRIG_PIN),
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
        .pin_bit_mask = (1ULL << ULTRASONIC_ECHO_PIN),
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
    gpio_set_level(ULTRASONIC_TRIG_PIN, 0);

    ESP_LOGI(TAG, "Ultrasonic sensor initialized successfully");
    return ESP_OK;
}

float ultrasonic_sensor_read_distance(void) {
    // Send 10us pulse to trigger
    gpio_set_level(ULTRASONIC_TRIG_PIN, 0);
    esp_rom_delay_us(2);
    gpio_set_level(ULTRASONIC_TRIG_PIN, 1);
    esp_rom_delay_us(10);
    gpio_set_level(ULTRASONIC_TRIG_PIN, 0);

    // Wait for echo to go HIGH (with timeout)
    int timeout = 0;
    while (gpio_get_level(ULTRASONIC_ECHO_PIN) == 0) {
        esp_rom_delay_us(1);
        if (++timeout > 10000) {
            ESP_LOGW(TAG, "Timeout waiting for echo HIGH");
            return -1.0f;
        }
    }

    // Measure echo HIGH duration
    int64_t start_time = esp_timer_get_time();
    timeout = 0;
    while (gpio_get_level(ULTRASONIC_ECHO_PIN) == 1) {
        esp_rom_delay_us(1);
        if (++timeout > 30000) {
            ESP_LOGW(TAG, "Timeout waiting for echo LOW");
            return -1.0f;
        }
    }
    int64_t end_time = esp_timer_get_time();

    // Calculate distance: distance = (duration * speed_of_sound) / 2
    // Speed of sound = 343 m/s = 0.0343 cm/us
    float duration_us = (float)(end_time - start_time);
    float distance_cm = (duration_us * 0.0343f) / 2.0f;

    ESP_LOGD(TAG, "Distance: %.2f cm (duration: %.2f us)", distance_cm, duration_us);

    return distance_cm;
}