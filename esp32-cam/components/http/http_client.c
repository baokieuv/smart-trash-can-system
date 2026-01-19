#include "http_client.h"
#include "config.h"
#include "esp_http_client.h"
#include "esp_log.h"
#include "cJSON.h"
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

static esp_err_t parse_waste_json(const char *json_str, classification_result_t *result){
    cJSON *root = cJSON_Parse(json_str);
    if (!root) return ESP_FAIL;

    esp_err_t err = ESP_FAIL;
    cJSON *cat = cJSON_GetObjectItem(root, "Category");
    cJSON *conf = cJSON_GetObjectItem(root, "Confident");

    if (cJSON_IsString(cat) && cJSON_IsNumber(conf)) {
        if (strcmp(cat->valuestring, "recyclable") == 0) result->category = WASTE_RECYCLABLE;
        else if (strcmp(cat->valuestring, "compostable") == 0) result->category = WASTE_COMPOSTABLE;
        else result->category = WASTE_NON_RECYCLABLE;
        
        strncpy(result->description, cat->valuestring, sizeof(result->description) - 1);
        result->confidence = (float)conf->valuedouble;
        err = ESP_OK;
    }
    cJSON_Delete(root);
    return err;
}

esp_err_t http_client_classify_waste(camera_fb_t *fb, classification_result_t *result, char *deviceId) {
    if (!fb || !result) {
        ESP_LOGE(TAG, "Invalid parameters");
        return ESP_ERR_INVALID_ARG;
    }

    ESP_LOGI(TAG, "Sending image to AI server for classification...");

    const char *boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
    char content_type[128], header[256], footer[64];
    
    snprintf(content_type, sizeof(content_type), "multipart/form-data; boundary=%s", boundary);
    int header_len = snprintf(header, sizeof(header),
        "--%s\r\n"                                                  
        "Content-Disposition: form-data; name=\"deviceId\"\r\n\r\n"
        "%s\r\n"
    
        "--%s\r\n"
        "Content-Disposition: form-data; name=\"image\"; filename=\"waste.jpg\"\r\n"
        "Content-Type: image/jpeg\r\n\r\n",
        boundary, deviceId, boundary);
    int footer_len = snprintf(footer, sizeof(footer), "\r\n--%s--\r\n", boundary);

    int total_len = header_len + fb->len + footer_len;

    char _url[64] = { 0 };
    sprintf(_url, "%s/classify-image", HTTP_SERVER_URL);
    // Configure HTTP client
    esp_http_client_config_t config = {
        .url = _url,
        .method = HTTP_METHOD_POST,
        .timeout_ms = HTTP_TIMEOUT_MS,
        .event_handler = http_event_handler,
        .buffer_size = 2048,
        .buffer_size_tx = 2048,
    };

    esp_http_client_handle_t client = esp_http_client_init(&config);
    if (!client) {
        ESP_LOGE(TAG, "Failed to initialize HTTP client");
        return ESP_FAIL;
    }

    esp_http_client_set_header(client, "Content-Type", content_type);

    esp_err_t err = esp_http_client_open(client, total_len);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to open HTTP connection: %s", esp_err_to_name(err));
        esp_http_client_cleanup(client);
        return err;
    }

    // Send multipart data
    esp_http_client_write(client, header, header_len);
    esp_http_client_write(client, (const char *)fb->buf, fb->len);
    esp_http_client_write(client, footer, footer_len);

    // Get response
    esp_http_client_fetch_headers(client);
    int status_code = esp_http_client_get_status_code(client);
    ESP_LOGI(TAG, "HTTP Status: %d", status_code);

    if(status_code == 200){
        char *response_buffer = malloc(4096);
        if (response_buffer) {
            int total = 0, read = 0;
            while((read = esp_http_client_read(client, response_buffer + total, 4096 - total - 1)) > 0){
                total += read;
                if (total >= 4096 - 1) break;
            }

            if (total > 0) {
                response_buffer[total] = '\0';
                ESP_LOGI(TAG, "Response: %s", response_buffer);
                err = parse_waste_json(response_buffer, result);
            }
            free(response_buffer);
        }
    } else {
        ESP_LOGE(TAG, "HTTP request failed with status: %d", status_code);
        err = ESP_FAIL;
    }

    esp_http_client_close(client);
    esp_http_client_cleanup(client);

    return err;
}

esp_err_t http_client_send_device_data(char *deviceId, waste_stats_t stats){
    char _url[64] = { 0 };
    sprintf(_url, "%s/devices/%s/data", HTTP_SERVER_URL, deviceId);

    esp_http_client_config_t conf = {
        .url = _url,
        .method = HTTP_METHOD_POST,
        .timeout_ms = HTTP_TIMEOUT_MS,
        .event_handler = http_event_handler,
        .buffer_size = 2048,
        .buffer_size_tx = 2048,
    };


    // char data[64] = { 0 };
    // sprintf(data, "");
    cJSON *root = cJSON_CreateObject();

    cJSON_AddNumberToObject(root, "recycledWasteCount", stats.recyclable_count);
    cJSON_AddNumberToObject(root, "nonRecycledWasteCount", stats.non_recyclable_count);
    cJSON_AddNumberToObject(root, "compostableWasteCount", stats.compostable_count);
    cJSON_AddNumberToObject(root, "fillLevel", stats.current_fill_level);
    cJSON_AddNumberToObject(root, "isFull", stats.is_full);    

    char *payload = cJSON_Print(root);
    cJSON_Delete(root);
    esp_http_client_handle_t client = esp_http_client_init(&conf);

    esp_http_client_set_header(client, "Content-Type", "application/json");
    esp_http_client_set_post_field(client, payload, strlen(payload));

    esp_err_t err = esp_http_client_perform(client);

    if (err == ESP_OK) {
        ESP_LOGI(TAG, "HTTP POST Status = %d, content_length = %lld",
                 esp_http_client_get_status_code(client),
                 esp_http_client_get_content_length(client));
    } else {
        ESP_LOGE(TAG, "HTTP POST request failed: %s", esp_err_to_name(err));
    }

    esp_http_client_cleanup(client);
   
    return ESP_OK;
}

esp_err_t http_client_cleanup(void) {
    ESP_LOGI(TAG, "HTTP client cleaned up");
    return ESP_OK;
}