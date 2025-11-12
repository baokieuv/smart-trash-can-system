#ifndef MQTT_CLIENT_TB_H
#define MQTT_CLIENT_TB_H

#include "esp_err.h"
#include "waste_manager.h"
#include "freertos/FreeRTOS.h"
#include "freertos/event_groups.h"

/**
 * @brief Initialize MQTT client
 * @return ESP_OK on success
 */
esp_err_t mqtt_client_init(EventGroupHandle_t g_event_group);

/**
 * @brief Start MQTT client
 * @return ESP_OK on success
 */
esp_err_t mqtt_client_start(void);

/**
 * @brief Send telemetry data to IoT platform
 * @param stats Waste statistics to send
 * @return ESP_OK on success
 */
esp_err_t mqtt_send_telemetry(const waste_stats_t *stats);

/**
 * @brief Stop MQTT client
 * @return ESP_OK on success
 */
esp_err_t mqtt_client_stop(void);

/**
 * @brief Check if MQTT is connected
 * @return true if connected
 */
bool mqtt_is_connected(void);

#endif // MQTT_CLIENT_TB_H