#ifndef NVS_STORAGE_H
#define NVS_STORAGE_H

#include <stdbool.h>
#include "esp_err.h"
#include "waste_manager.h"

/**
 * @brief Save WiFi configuration to NVS
 */
esp_err_t nvs_save_wifi_config(const char *ssid, const char *pass);

/**
 * @brief Load WiFi configuration from NVS
 * @return true if successful, false otherwise
 */
bool nvs_load_wifi_config(char *ssid, char *pass);

esp_err_t nvs_save_stats_info(waste_stats_t stats);

bool nvs_load_stats_info(waste_stats_t* stats);

/**
 * @brief Clear all stored configuration
 */
esp_err_t nvs_clear_config(void);

#endif // NVS_STORAGE_H