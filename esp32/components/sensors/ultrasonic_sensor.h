#ifndef ULTRASONIC_SENSOR_H
#define ULTRASONIC_SENSOR_H

#include "esp_err.h"

/**
 * @brief Initialize ultrasonic sensor
 * @return ESP_OK on success
 */
esp_err_t ultrasonic_sensor_init(void);

/**
 * @brief Read distance from ultrasonic sensor
 * @return Distance in centimeters, -1.0 on error
 */
float ultrasonic_sensor_read_distance(void);

#endif // ULTRASONIC_SENSOR_H