#include "mqtt_client_tb.h"
#include "config.h"
#include "mqtt_client.h"
#include "esp_log.h"
#include "cJSON.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include <string.h>

static const char *TAG = "MQTT";
static esp_mqtt_client_handle_t g_mqtt_client = NULL;
static bool g_mqtt_connected = false;
static SemaphoreHandle_t g_mqtt_mutex = NULL;

static void mqtt_event_handler(void *handler_args, esp_event_base_t base,
                               int32_t event_id, void *event_data) {
    esp_mqtt_event_handle_t event = event_data;

    switch ((esp_mqtt_event_id_t)event_id) {
        case MQTT_EVENT_CONNECTED:
            ESP_LOGI(TAG, "MQTT Connected to broker");
            g_mqtt_connected = true;
            break;

        case MQTT_EVENT_DISCONNECTED:
            ESP_LOGW(TAG, "MQTT Disconnected from broker");
            g_mqtt_connected = false;
            break;

        case MQTT_EVENT_PUBLISHED:
            ESP_LOGD(TAG, "Message published, msg_id=%d", event->msg_id);
            break;

        case MQTT_EVENT_DATA:
            ESP_LOGI(TAG, "MQTT Data received - Topic: %.*s",
                     event->topic_len, event->topic);
            ESP_LOGI(TAG, "Data: %.*s", event->data_len, event->data);
            break;

        case MQTT_EVENT_ERROR:
            ESP_LOGE(TAG, "MQTT Error occurred");
            if (event->error_handle->error_type == MQTT_ERROR_TYPE_TCP_TRANSPORT) {
                ESP_LOGE(TAG, "Last error code: 0x%x", event->error_handle->esp_transport_sock_errno);
            }
            break;

        case MQTT_EVENT_BEFORE_CONNECT:
            ESP_LOGI(TAG, "MQTT Connecting...");
            break;

        default:
            ESP_LOGD(TAG, "MQTT event: %ld", event_id);
            break;
    }
}

esp_err_t mqtt_client_init(void) {
    if (g_mqtt_client != NULL) {
        ESP_LOGW(TAG, "MQTT client already initialized");
        return ESP_OK;
    }

    ESP_LOGI(TAG, "Initializing MQTT client...");

    // Create mutex
    g_mqtt_mutex = xSemaphoreCreateMutex();
    if (!g_mqtt_mutex) {
        ESP_LOGE(TAG, "Failed to create mutex");
        return ESP_ERR_NO_MEM;
    }

    // Configure MQTT client
    esp_mqtt_client_config_t mqtt_cfg = {
        .broker.address.uri = MQTT_BROKER,
        .credentials.username = MQTT_ACCESS_TOKEN,
        .session.keepalive = 60,
        .network.timeout_ms = 10000,
        .network.reconnect_timeout_ms = MQTT_RECONNECT_DELAY_MS,
    };

    g_mqtt_client = esp_mqtt_client_init(&mqtt_cfg);
    if (!g_mqtt_client) {
        ESP_LOGE(TAG, "Failed to initialize MQTT client");
        vSemaphoreDelete(g_mqtt_mutex);
        g_mqtt_mutex = NULL;
        return ESP_FAIL;
    }

    // Register event handler
    esp_err_t err = esp_mqtt_client_register_event(g_mqtt_client, ESP_EVENT_ANY_ID,
                                                    mqtt_event_handler, NULL);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to register MQTT event handler");
        esp_mqtt_client_destroy(g_mqtt_client);
        g_mqtt_client = NULL;
        vSemaphoreDelete(g_mqtt_mutex);
        g_mqtt_mutex = NULL;
        return err;
    }

    ESP_LOGI(TAG, "MQTT client initialized successfully");
    return ESP_OK;
}

esp_err_t mqtt_client_start(void) {
    if (!g_mqtt_client) {
        ESP_LOGE(TAG, "MQTT client not initialized");
        return ESP_ERR_INVALID_STATE;
    }

    ESP_LOGI(TAG, "Starting MQTT client...");
    esp_err_t err = esp_mqtt_client_start(g_mqtt_client);
    
    if (err == ESP_OK) {
        ESP_LOGI(TAG, "MQTT client started successfully");
    } else {
        ESP_LOGE(TAG, "Failed to start MQTT client: %s", esp_err_to_name(err));
    }

    return err;
}

esp_err_t mqtt_send_telemetry(const waste_stats_t *stats) {
    if (!g_mqtt_client) {
        ESP_LOGE(TAG, "MQTT client not initialized");
        return ESP_ERR_INVALID_STATE;
    }

    if (!g_mqtt_connected) {
        ESP_LOGW(TAG, "MQTT not connected, skipping telemetry");
        return ESP_ERR_INVALID_STATE;
    }

    if (!stats) {
        ESP_LOGE(TAG, "Invalid stats pointer");
        return ESP_ERR_INVALID_ARG;
    }

    // Build JSON payload
    cJSON *root = cJSON_CreateObject();
    if (!root) {
        ESP_LOGE(TAG, "Failed to create JSON object");
        return ESP_ERR_NO_MEM;
    }

    cJSON_AddNumberToObject(root, "totalWaste", stats->total_count);
    cJSON_AddNumberToObject(root, "recyclable", stats->recyclable_count);
    cJSON_AddNumberToObject(root, "compostable", stats->compostable_count);
    cJSON_AddNumberToObject(root, "hazardous", stats->hazardous_count);
    cJSON_AddNumberToObject(root, "fillLevel", stats->current_fill_level);
    cJSON_AddBoolToObject(root, "isFull", stats->is_full);

    char *payload = cJSON_PrintUnformatted(root);
    cJSON_Delete(root);

    if (!payload) {
        ESP_LOGE(TAG, "Failed to serialize JSON");
        return ESP_ERR_NO_MEM;
    }

    ESP_LOGI(TAG, "Publishing telemetry: %s", payload);

    // Publish message
    int msg_id = esp_mqtt_client_publish(g_mqtt_client, MQTT_TELEMETRY_TOPIC,
                                         payload, 0, 1, 0);

    free(payload);

    if (msg_id < 0) {
        ESP_LOGE(TAG, "Failed to publish message");
        return ESP_FAIL;
    }

    ESP_LOGI(TAG, "Telemetry published successfully (msg_id: %d)", msg_id);
    return ESP_OK;
}

esp_err_t mqtt_client_stop(void) {
    if (!g_mqtt_client) {
        return ESP_OK;
    }

    ESP_LOGI(TAG, "Stopping MQTT client...");
    
    esp_err_t err = esp_mqtt_client_stop(g_mqtt_client);
    if (err == ESP_OK) {
        esp_mqtt_client_destroy(g_mqtt_client);
        g_mqtt_client = NULL;
        g_mqtt_connected = false;
        
        if (g_mqtt_mutex) {
            vSemaphoreDelete(g_mqtt_mutex);
            g_mqtt_mutex = NULL;
        }
        
        ESP_LOGI(TAG, "MQTT client stopped");
    } else {
        ESP_LOGE(TAG, "Failed to stop MQTT client: %s", esp_err_to_name(err));
    }

    return err;
}

bool mqtt_is_connected(void) {
    return g_mqtt_connected;
}