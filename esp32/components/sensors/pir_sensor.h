#ifndef PIR_SENSOR_H
#define PIR_SENSOR_H

#include "esp_err.h"
#include <stdbool.h>

/**
 * @brief Initialize PIR sensor
 * @return ESP_OK on success
 */
esp_err_t pir_sensor_init(void);

/**
 * @brief Read PIR sensor state
 * @return true if motion detected, false otherwise
 */
bool pir_sensor_read(void);

#endif // PIR_SENSOR_H