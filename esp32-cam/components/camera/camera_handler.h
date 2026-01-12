#ifndef CAMERA_HANDLER_H
#define CAMERA_HANDLER_H

#include "esp_err.h"
#include "esp_camera.h"

/**
 * @brief Initialize camera
 * @return ESP_OK on success
 */
esp_err_t camera_handler_init(void);

/**
 * @brief Capture image from camera
 * @param fb Pointer to store frame buffer
 * @return ESP_OK on success
 */
esp_err_t camera_handler_capture(camera_fb_t **fb);

/**
 * @brief Return frame buffer to camera
 * @param fb Frame buffer to return
 */
void camera_handler_return_fb(camera_fb_t *fb);

/**
 * @brief Deinitialize camera
 * @return ESP_OK on success
 */
esp_err_t camera_handler_deinit(void);

#endif // CAMERA_HANDLER_H