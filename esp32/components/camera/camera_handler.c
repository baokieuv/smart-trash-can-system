#include "camera_handler.h"
#include "config.h"
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"

static const char *TAG = "CAMERA";
static bool g_camera_initialized = false;

esp_err_t camera_handler_init(void) {
    if (g_camera_initialized) {
        ESP_LOGW(TAG, "Camera already initialized");
        return ESP_OK;
    }

    ESP_LOGI(TAG, "Initializing camera...");

    camera_config_t camera_cfg = {
        .pin_pwdn = CAM_PIN_PWDN,
        .pin_reset = CAM_PIN_RESET,
        .pin_xclk = CAM_PIN_XCLK,
        .pin_sccb_sda = CAM_PIN_SIOD,
        .pin_sccb_scl = CAM_PIN_SIOC,

        .pin_d7 = CAM_PIN_D7,
        .pin_d6 = CAM_PIN_D6,
        .pin_d5 = CAM_PIN_D5,
        .pin_d4 = CAM_PIN_D4,
        .pin_d3 = CAM_PIN_D3,
        .pin_d2 = CAM_PIN_D2,
        .pin_d1 = CAM_PIN_D1,
        .pin_d0 = CAM_PIN_D0,
        .pin_vsync = CAM_PIN_VSYNC,
        .pin_href = CAM_PIN_HREF,
        .pin_pclk = CAM_PIN_PCLK,

        .xclk_freq_hz = 20000000,
        .ledc_timer = LEDC_TIMER_0,
        .ledc_channel = LEDC_CHANNEL_0,

        .pixel_format = PIXFORMAT_JPEG,
        .frame_size = FRAMESIZE_VGA,  // 320x240

        .jpeg_quality = 10,
        .fb_count = 2,
        .fb_location = CAMERA_FB_IN_PSRAM,
        .grab_mode = CAMERA_GRAB_LATEST,

    };

    esp_err_t err = esp_camera_init(&camera_cfg);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Camera init failed: %s", esp_err_to_name(err));
        return err;
    }

    g_camera_initialized = true;
    ESP_LOGI(TAG, "Camera initialized successfully");

    return ESP_OK;
}

esp_err_t camera_handler_capture(camera_fb_t **fb) {
    if (!g_camera_initialized) {
        ESP_LOGE(TAG, "Camera not initialized");
        return ESP_ERR_INVALID_STATE;
    }

    if (!fb) {
        ESP_LOGE(TAG, "Invalid frame buffer pointer");
        return ESP_ERR_INVALID_ARG;
    }

    ESP_LOGI(TAG, "Warming up camera...");
    
    // Take a few dummy shots to warm up camera and get proper exposure
    for (int i = 0; i < CAMERA_WARMUP_SHOTS; i++) {
        camera_fb_t *dummy_fb = esp_camera_fb_get();
        if (dummy_fb) {
            esp_camera_fb_return(dummy_fb);
        }
        vTaskDelay(pdMS_TO_TICKS(CAMERA_SHOT_DELAY_MS));
    }

    // Capture actual image
    ESP_LOGI(TAG, "Capturing image...");
    *fb = esp_camera_fb_get();

    if (!(*fb)) {
        ESP_LOGE(TAG, "Camera capture failed");
        return ESP_FAIL;
    }

    ESP_LOGI(TAG, "Image captured successfully - Size: %zu bytes, Format: %d, Width: %d, Height: %d",
             (*fb)->len, (*fb)->format, (*fb)->width, (*fb)->height);

    return ESP_OK;
}

void camera_handler_return_fb(camera_fb_t *fb) {
    if (fb) {
        esp_camera_fb_return(fb);
        ESP_LOGD(TAG, "Frame buffer returned");
    }
}

esp_err_t camera_handler_deinit(void) {
    if (!g_camera_initialized) {
        return ESP_OK;
    }

    ESP_LOGI(TAG, "Deinitializing camera...");
    esp_err_t err = esp_camera_deinit();
    
    if (err == ESP_OK) {
        g_camera_initialized = false;
        ESP_LOGI(TAG, "Camera deinitialized");
    } else {
        ESP_LOGE(TAG, "Failed to deinitialize camera: %s", esp_err_to_name(err));
    }

    return err;
}