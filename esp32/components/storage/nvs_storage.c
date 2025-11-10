#include "nvs_storage.h"
#include "nvs_flash.h"
#include "esp_log.h"
#include "config.h"
#include <string.h>

static const char *TAG = "NVS_STORAGE";

esp_err_t nvs_save_wifi_config(const char *ssid, const char *pass) {
    if (!ssid || !pass) {
        ESP_LOGE(TAG, "Invalid parameters");
        return ESP_ERR_INVALID_ARG;
    }

    nvs_handle_t nvs;
    esp_err_t err = nvs_open(NVS_NAMESPACE, NVS_READWRITE, &nvs);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to open NVS: %s", esp_err_to_name(err));
        return err;
    }

    err = nvs_set_str(nvs, NVS_KEY_SSID, ssid);
    if (err == ESP_OK) {
        err = nvs_set_str(nvs, NVS_KEY_PASS, pass);
    }

    if (err == ESP_OK) {
        err = nvs_commit(nvs);
    }

    nvs_close(nvs);

    if (err == ESP_OK) {
        ESP_LOGI(TAG, "WiFi config saved successfully");
    } else {
        ESP_LOGE(TAG, "Failed to save WiFi config: %s", esp_err_to_name(err));
    }

    return err;
}

bool nvs_load_wifi_config(char *ssid, char *pass) {
    if (!ssid || !pass) {
        ESP_LOGE(TAG, "Invalid parameters");
        return false;
    }

    nvs_handle_t nvs;
    esp_err_t err = nvs_open(NVS_NAMESPACE, NVS_READONLY, &nvs);
    if (err != ESP_OK) {
        ESP_LOGW(TAG, "NVS not found or cannot open");
        return false;
    }

    size_t ssid_len = SSID_MAX_LEN;
    size_t pass_len = PASSWORD_MAX_LEN;

    bool success = true;
    
    if (nvs_get_str(nvs, NVS_KEY_SSID, ssid, &ssid_len) != ESP_OK) {
        success = false;
    }
    if (success && nvs_get_str(nvs, NVS_KEY_PASS, pass, &pass_len) != ESP_OK) {
        success = false;
    }

    nvs_close(nvs);

    if (success) {
        ESP_LOGI(TAG, "Config loaded: SSID=%s", ssid);
    } else {
        ESP_LOGW(TAG, "Failed to load complete configuration");
    }

    return success;
}

esp_err_t nvs_clear_config(void) {
    nvs_handle_t nvs;
    esp_err_t err = nvs_open(NVS_NAMESPACE, NVS_READWRITE, &nvs);
    if (err != ESP_OK) {
        return err;
    }

    err = nvs_erase_all(nvs);
    if (err == ESP_OK) {
        err = nvs_commit(nvs);
    }

    nvs_close(nvs);
    ESP_LOGI(TAG, "Configuration cleared");
    return err;
}