#include "waste_manager.h"
#include "nvs_storage.h"
#include "nvs_flash.h"
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/semphr.h"
#include <string.h>

static const char *TAG = "WASTE_MGR";

extern EventGroupHandle_t g_event_group;

// Global state
static waste_stats_t g_stats = {0};
static SemaphoreHandle_t g_stats_mutex = NULL;
// static EventGroupHandle_t g_event_group = NULL;
static bool g_initialized = false;

// ==================== Private Functions ====================

static void load_stats_from_nvs(void) {
    // Load persistent statistics from NVS
    nvs_handle_t nvs;
    esp_err_t err = nvs_open(NVS_NAMESPACE, NVS_READONLY, &nvs);
    if (err == ESP_OK) {
        nvs_get_u32(nvs, NVS_KEY_WASTE_COUNT, &g_stats.total_count);
        nvs_close(nvs);
        ESP_LOGI(TAG, "Loaded stats - Total count: %lu", g_stats.total_count);
    }
}

static void save_stats_to_nvs(void) {
    // Save statistics to NVS for persistence
    nvs_handle_t nvs;
    esp_err_t err = nvs_open(NVS_NAMESPACE, NVS_READWRITE, &nvs);
    if (err == ESP_OK) {
        nvs_set_u32(nvs, NVS_KEY_WASTE_COUNT, g_stats.total_count);
        nvs_commit(nvs);
        nvs_close(nvs);
        ESP_LOGD(TAG, "Stats saved to NVS");
    }
}

static void update_bin_full_status(void) {
    bool was_full = g_stats.is_full;
    g_stats.is_full = (g_stats.current_fill_level >= BIN_FULL_THRESHOLD);
    
    if (g_stats.is_full && !was_full) {
        ESP_LOGW(TAG, "BIN IS FULL! Fill level: %.1f%%", g_stats.current_fill_level);
        if (g_event_group) {
            xEventGroupSetBits(g_event_group, BIN_FULL_BIT);
        }
    } else if (!g_stats.is_full && was_full) {
        ESP_LOGI(TAG, "Bin no longer full");
        if (g_event_group) {
            xEventGroupClearBits(g_event_group, BIN_FULL_BIT);
        }
    }
}

// ==================== Public Functions ====================

esp_err_t waste_manager_init(EventGroupHandle_t g_event_group) {
    if (g_initialized) {
        ESP_LOGW(TAG, "Already initialized");
        return ESP_OK;
    }

    ESP_LOGI(TAG, "Initializing waste manager...");

    // Create mutex for thread-safe access
    g_stats_mutex = xSemaphoreCreateMutex();
    if (!g_stats_mutex) {
        ESP_LOGE(TAG, "Failed to create mutex");
        return ESP_ERR_NO_MEM;
    }

    // Create event group
    g_event_group = g_event_group;
    if (!g_event_group) {
        ESP_LOGE(TAG, "Failed to create event group");
        vSemaphoreDelete(g_stats_mutex);
        return ESP_ERR_NO_MEM;
    }

    // Initialize statistics
    memset(&g_stats, 0, sizeof(waste_stats_t));
    load_stats_from_nvs();

    g_initialized = true;
    ESP_LOGI(TAG, "Waste manager initialized successfully");
    return ESP_OK;
}

esp_err_t waste_manager_start(void) {
    if (!g_initialized) {
        ESP_LOGE(TAG, "Not initialized");
        return ESP_ERR_INVALID_STATE;
    }

    ESP_LOGI(TAG, "Starting waste manager");
    xEventGroupClearBits(g_event_group, CONFIG_MODE_BIT);
    return ESP_OK;
}

//chưa dùng 
esp_err_t waste_manager_stop(void) {
    if (!g_initialized) {
        return ESP_OK;
    }

    ESP_LOGI(TAG, "Stopping waste manager");
    save_stats_to_nvs();
    return ESP_OK;
}

esp_err_t waste_manager_get_stats(waste_stats_t *stats) {
    if (!g_initialized || !stats) {
        return ESP_ERR_INVALID_ARG;
    }

    if (xSemaphoreTake(g_stats_mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
        memcpy(stats, &g_stats, sizeof(waste_stats_t));
        xSemaphoreGive(g_stats_mutex);
        return ESP_OK;
    }

    return ESP_ERR_TIMEOUT;
}

//Chưa dùng
esp_err_t waste_manager_reset_stats(void) {
    if (!g_initialized) {
        return ESP_ERR_INVALID_STATE;
    }

    ESP_LOGI(TAG, "Resetting statistics");

    if (xSemaphoreTake(g_stats_mutex, pdMS_TO_TICKS(1000)) == pdTRUE) {
        g_stats.total_count = 0;
        g_stats.recyclable_count = 0;
        g_stats.compostable_count = 0;
        g_stats.hazardous_count = 0;
        xSemaphoreGive(g_stats_mutex);
        
        save_stats_to_nvs();
        return ESP_OK;
    }

    return ESP_ERR_TIMEOUT;
}

esp_err_t waste_manager_update_fill_level(float distance_cm) {
    if (!g_initialized) {
        return ESP_ERR_INVALID_STATE;
    }

    if (distance_cm < 0 || distance_cm > BIN_HEIGHT_CM) {
        return ESP_ERR_INVALID_ARG;
    }

    if (xSemaphoreTake(g_stats_mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
        // Calculate fill level: closer distance = more full
        float used_height = BIN_HEIGHT_CM - distance_cm;
        g_stats.current_fill_level = (used_height / BIN_HEIGHT_CM) * 100.0f;
        
        // Ensure within bounds
        if (g_stats.current_fill_level < 0) g_stats.current_fill_level = 0;
        if (g_stats.current_fill_level > 100) g_stats.current_fill_level = 100;
        
        update_bin_full_status();
        xSemaphoreGive(g_stats_mutex);
        
        ESP_LOGD(TAG, "Fill level updated: %.1f%% (distance: %.1fcm)", 
                 g_stats.current_fill_level, distance_cm);
        return ESP_OK;
    }

    return ESP_ERR_TIMEOUT;
}

esp_err_t waste_manager_record_waste(waste_category_t category) {
    if (!g_initialized) {
        return ESP_ERR_INVALID_STATE;
    }

    if (xSemaphoreTake(g_stats_mutex, pdMS_TO_TICKS(1000)) == pdTRUE) {
        g_stats.total_count++;
        
        switch (category) {
            case WASTE_RECYCLABLE:
                g_stats.recyclable_count++;
                ESP_LOGI(TAG, "Recyclable waste recorded");
                break;
            case WASTE_COMPOSTABLE:
                g_stats.compostable_count++;
                ESP_LOGI(TAG, "Compostable waste recorded");
                break;
            case WASTE_HAZARDOUS:
                g_stats.hazardous_count++;
                ESP_LOGI(TAG, "Hazardous waste recorded");
                break;
            default:
                ESP_LOGW(TAG, "Unknown waste category");
                break;
        }
        
        ESP_LOGI(TAG, "Total waste count: %lu", g_stats.total_count);
        xSemaphoreGive(g_stats_mutex);
        
        // Save periodically (every 10 throws)
        if (g_stats.total_count % 10 == 0) {
            save_stats_to_nvs();
        }
        
        return ESP_OK;
    }

    return ESP_ERR_TIMEOUT;
}

//chưa dùng
bool waste_manager_is_bin_full(void) {
    if (!g_initialized) {
        return false;
    }

    bool is_full = false;
    if (xSemaphoreTake(g_stats_mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
        is_full = g_stats.is_full;
        xSemaphoreGive(g_stats_mutex);
    }
    return is_full;
}

//chưa dùng 
float waste_manager_get_fill_level(void) {
    if (!g_initialized) {
        return 0.0f;
    }

    float level = 0.0f;
    if (xSemaphoreTake(g_stats_mutex, pdMS_TO_TICKS(100)) == pdTRUE) {
        level = g_stats.current_fill_level;
        xSemaphoreGive(g_stats_mutex);
    }
    return level;
}
