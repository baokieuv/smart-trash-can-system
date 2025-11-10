#include "http_client.h"
#include "config.h"
#include "esp_http_client.h"
#include "esp_log.h"
#include "cJSON.h"
#include "img_converters.h"
#include <string.h>

static const char *TAG = "HTTP_CLIENT";

esp_err_t http_client_init(void) {
    ESP_LOGI(TAG, "HTTP client initialized");
    return ESP_OK;
}

static esp_err_t http_event_handler(esp_http_client_event_t *evt) {
    switch (evt->event_id) {
        case HTTP_EVENT_ERROR:
            ESP_LOGD(TAG, "HTTP_EVENT_ERROR");
            break;
        case HTTP_EVENT_ON_CONNECTED:
            ESP_LOGD(TAG, "HTTP_EVENT_ON_CONNECTED");
            break;
        case HTTP_EVENT_HEADERS_SENT:
            ESP_LOGD(TAG, "HTTP_EVENT_HEADERS_SENT");
            break;
        case HTTP_EVENT_ON_HEADER:
            ESP_LOGD(TAG, "HTTP_EVENT_ON_HEADER: %s: %s", evt->header_key, evt->header_value);
            break;
        case HTTP_EVENT_ON_DATA:
            ESP_LOGD(TAG, "HTTP_EVENT_ON_DATA: %d bytes", evt->data_len);
            break;
        case HTTP_EVENT_ON_FINISH:
            ESP_LOGD(TAG, "HTTP_EVENT_ON_FINISH");
            break;
        case HTTP_EVENT_DISCONNECTED:
            ESP_LOGD(TAG, "HTTP_EVENT_DISCONNECTED");
            break;
        default:
            break;
    }
    return ESP_OK;
}

esp_err_t http_client_classify_waste(camera_fb_t *fb, classification_result_t *result) {
    if (!fb || !result) {
        ESP_LOGE(TAG, "Invalid parameters");
        return ESP_ERR_INVALID_ARG;
    }

    ESP_LOGI(TAG, "Sending image to AI server for classification...");

    // Convert frame to JPEG if needed
    uint8_t *jpg_buf = NULL;
    size_t jpg_len = 0;
    bool converted = false;

    if (fb->format != PIXFORMAT_JPEG) {
        ESP_LOGI(TAG, "Converting image to JPEG...");
        if (!fmt2jpg(fb->buf, fb->len, fb->width, fb->height, fb->format, 90, &jpg_buf, &jpg_len)) {
            ESP_LOGE(TAG, "JPEG conversion failed");
            return ESP_FAIL;
        }
        converted = true;
        ESP_LOGI(TAG, "Converted to JPEG: %zu bytes", jpg_len);
    } else {
        jpg_buf = fb->buf;
        jpg_len = fb->len;
    }

    // Prepare multipart/form-data
    const char *boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
    char content_type[128];
    snprintf(content_type, sizeof(content_type), "multipart/form-data; boundary=%s", boundary);

    // Build request body
    char header[256];
    int header_len = snprintf(header, sizeof(header),
        "--%s\r\n"
        "Content-Disposition: form-data; name=\"image\"; filename=\"waste.jpg\"\r\n"
        "Content-Type: image/jpeg\r\n\r\n",
        boundary);

    char footer[64];
    int footer_len = snprintf(footer, sizeof(footer), "\r\n--%s--\r\n", boundary);

    int total_len = header_len + jpg_len + footer_len;

    // Configure HTTP client
    esp_http_client_config_t config = {
        .url = HTTP_SERVER_URL,
        .method = HTTP_METHOD_POST,
        .timeout_ms = HTTP_TIMEOUT_MS,
        .event_handler = http_event_handler,
        .buffer_size = 2048,
        .buffer_size_tx = 2048,
    };

    esp_http_client_handle_t client = esp_http_client_init(&config);
    if (!client) {
        ESP_LOGE(TAG, "Failed to initialize HTTP client");
        if (converted) free(jpg_buf);
        return ESP_FAIL;
    }

    esp_http_client_set_header(client, "Content-Type", content_type);

    esp_err_t err = esp_http_client_open(client, total_len);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to open HTTP connection: %s", esp_err_to_name(err));
        esp_http_client_cleanup(client);
        if (converted) free(jpg_buf);
        return err;
    }

    // Send multipart data
    esp_http_client_write(client, header, header_len);
    esp_http_client_write(client, (const char *)jpg_buf, jpg_len);
    esp_http_client_write(client, footer, footer_len);

    // Get response
    int content_length = esp_http_client_fetch_headers(client);
    int status_code = esp_http_client_get_status_code(client);

    ESP_LOGI(TAG, "HTTP Status: %d, Content-Length: %d", status_code, content_length);

    if (status_code == 200 && content_length > 0) {
        char *response_buffer = malloc(content_length + 1);
        if (response_buffer) {
            int read_len = esp_http_client_read(client, response_buffer, content_length);
            if (read_len > 0) {
                response_buffer[read_len] = '\0';
                ESP_LOGI(TAG, "Response: %s", response_buffer);

                // Parse JSON response
                cJSON *root = cJSON_Parse(response_buffer);
                if (root) {
                    const cJSON *category = cJSON_GetObjectItem(root, "category");
                    const cJSON *confidence = cJSON_GetObjectItem(root, "confidence");

                    if (cJSON_IsString(category) && cJSON_IsNumber(confidence)) {
                        // Map category string to enum
                        if (strcmp(category->valuestring, "recyclable") == 0) {
                            result->category = WASTE_RECYCLABLE;
                            strncpy(result->description, "Recyclable", sizeof(result->description) - 1);
                        } else if (strcmp(category->valuestring, "compostable") == 0) {
                            result->category = WASTE_COMPOSTABLE;
                            strncpy(result->description, "Compostable", sizeof(result->description) - 1);
                        } else {
                            result->category = WASTE_HAZARDOUS;
                            strncpy(result->description, "Hazardous", sizeof(result->description) - 1);
                        }
                        result->confidence = (float)confidence->valuedouble;

                        ESP_LOGI(TAG, "Classification: %s (%.1f%% confidence)",
                                 result->description, result->confidence);
                        err = ESP_OK;
                    } else {
                        ESP_LOGE(TAG, "Invalid JSON response format");
                        err = ESP_FAIL;
                    }

                    cJSON_Delete(root);
                } else {
                    ESP_LOGE(TAG, "Failed to parse JSON response");
                    err = ESP_FAIL;
                }
            }
            free(response_buffer);
        }
    } else {
        ESP_LOGE(TAG, "HTTP request failed with status: %d", status_code);
        err = ESP_FAIL;
    }

    esp_http_client_close(client);
    esp_http_client_cleanup(client);

    if (converted && jpg_buf) {
        free(jpg_buf);
    }

    return err;
}

esp_err_t http_client_cleanup(void) {
    ESP_LOGI(TAG, "HTTP client cleaned up");
    return ESP_OK;
}