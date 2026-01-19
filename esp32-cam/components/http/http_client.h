#ifndef HTTP_CLIENT_H
#define HTTP_CLIENT_H

#include "esp_err.h"
#include "esp_camera.h"
#include "waste_manager.h"

/**
 * @brief Initialize HTTP client
 * @return ESP_OK on success
 */
esp_err_t http_client_init(void);

/**
 * @brief Send image to AI server for classification
 * @param fb Frame buffer containing image
 * @param result Pointer to store classification result
 * @return ESP_OK on success
 */
esp_err_t http_client_classify_waste(camera_fb_t *fb, classification_result_t *result, char *deviceId);

esp_err_t http_client_send_device_data(char *deviceId, waste_stats_t stats);

/**
 * @brief Cleanup HTTP client
 * @return ESP_OK on success
 */
esp_err_t http_client_cleanup(void);

#endif // HTTP_CLIENT_H