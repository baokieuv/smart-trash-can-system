#include "pir_sensor.h"
#include "config.h"
#include "driver/gpio.h"
#include "esp_log.h"

static const char *TAG = "PIR_SENSOR";

esp_err_t pir_sensor_init(void) {
    ESP_LOGI(TAG, "Initializing PIR sensor on GPIO%d", PIR_SENSOR_PIN);

    gpio_config_t pir_cfg = {
        .pin_bit_mask = (1ULL << PIR_SENSOR_PIN),
        .mode = GPIO_MODE_INPUT,
        .pull_down_en = GPIO_PULLDOWN_DISABLE,
        .pull_up_en = GPIO_PULLUP_DISABLE,
        .intr_type = GPIO_INTR_DISABLE,
    };

    esp_err_t err = gpio_config(&pir_cfg);
    if (err == ESP_OK) {
        ESP_LOGI(TAG, "PIR sensor initialized successfully");
    } else {
        ESP_LOGE(TAG, "Failed to initialize PIR sensor: %s", esp_err_to_name(err));
    }

    return err;
}

bool pir_sensor_read(void) {
    return gpio_get_level(PIR_SENSOR_PIN) == 1;
}
