#ifndef ULTRASONIC_SENSOR_H
#define ULTRASONIC_SENSOR_H

#include "esp_err.h"

#define SOUND_SPEED     343.0
#define ECHO_TIMEOUT    300000

#define ULTRASONIC_MIN_DISTANCE     0
#define ULTRASONIC_MAX_DISTANCE     200

/**
 * @brief Initialize ultrasonic sensor
 * @return ESP_OK on success
 */
esp_err_t ultrasonic_sensor_init();

// /**
//  * @brief Read distance from ultrasonic sensor
//  * @return Distance in centimeters, -1.0 on error
//  */
// float ultrasonic_sensor_read_distance(ultrasonic_senser_t ultrasonic_sensor);

float ultrasonic_sensor_get_distance_for_detect();

float ultrasonic_sensor_get_distance_for_check_full();

#endif // ULTRASONIC_SENSOR_H