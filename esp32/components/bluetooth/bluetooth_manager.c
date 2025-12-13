#include "bluetooth_manager.h"
#include <string.h>
#include "esp_log.h"
#include "nvs_storage.h"
#include "cJSON.h"
#include "freertos/FreeRTOS.h"
#include "freertos/event_groups.h"

#define SERVICE_UUID           0x0001
#define CHAR_RX_UUID           0x0002 // Điện thoại Gửi -> ESP32 Nhận
#define CHAR_TX_UUID           0x0003 // ESP32 Gửi -> Điện thoại Nhận (Notify)

#define TAG "BLE"
#define DEVICE_NAME "SMART_BIN_SETUP"

extern EventGroupHandle_t g_event_group;

// Biến toàn cục cho BLE
static uint16_t connection_handle = 0;
// static uint16_t char_tx_handle = 0;

static void gatts_profile_event_handler(esp_gatts_cb_event_t event, esp_gatt_if_t gatts_if, esp_ble_gatts_cb_param_t *param);

static esp_err_t extract_wifi_info(char *data, int length){
    cJSON *root = cJSON_Parse(data);
    esp_err_t ret = ESP_FAIL;
    if(!root){
        ESP_LOGE(TAG, "Failed to parse JSON");
        goto cleanup;
    }

    // Extract fields
    const cJSON *ssid = cJSON_GetObjectItem(root, "ssid");
    const cJSON *pass = cJSON_GetObjectItem(root, "password");

    // Validate fields
    if (!ssid || !pass || !cJSON_IsString(ssid) || !cJSON_IsString(pass)) {
        ESP_LOGE(TAG, "Invalid or missing fields in JSON");
        goto cleanup;
    }

    ESP_LOGI(TAG, "Received config - SSID: %s, Password: %s", 
             ssid->valuestring, pass->valuestring);

    // Save WiFi configuration
    esp_err_t err = nvs_save_wifi_config(ssid->valuestring, pass->valuestring);
    if (err != ESP_OK) {
        ESP_LOGE(TAG, "Failed to save WiFi config");
        goto cleanup;
    }

    if(xEventGroupGetBits(g_event_group) & RESET_MODE_BIT){
        waste_manager_reset_stats();
    }
    ret = ESP_OK;
cleanup:
    if (root) cJSON_Delete(root);
    return ret;
}

static struct gatts_profile_inst {
    esp_gatts_cb_t gatts_cb;
    uint16_t gatts_if;
    uint16_t app_id;
    uint16_t service_handle;
    esp_gatt_srvc_id_t service_id;
    uint16_t char_handle;
    esp_bt_uuid_t char_uuid;
    esp_gatt_perm_t perm;
    esp_gatt_char_prop_t property;
} gl_profile = {
    .gatts_cb = gatts_profile_event_handler,
    .gatts_if = ESP_GATT_IF_NONE,
};


static void gap_event_handler(esp_gap_ble_cb_event_t event, esp_ble_gap_cb_param_t *param) {
    switch (event) {
    case ESP_GAP_BLE_ADV_DATA_SET_COMPLETE_EVT:
        esp_ble_gap_start_advertising(&(esp_ble_adv_params_t){
            .adv_int_min = 0x20,
            .adv_int_max = 0x40,
            .adv_type = ADV_TYPE_IND,
            .own_addr_type = BLE_ADDR_TYPE_PUBLIC,
            .channel_map = ADV_CHNL_ALL,
            .adv_filter_policy = ADV_FILTER_ALLOW_SCAN_ANY_CON_ANY,
        });
        break;
    default: break;
    }
}

static void gatts_profile_event_handler(esp_gatts_cb_event_t event, esp_gatt_if_t gatts_if, esp_ble_gatts_cb_param_t *param) {
    switch (event) {
    case ESP_GATTS_REG_EVT: {
        esp_ble_gap_set_device_name(DEVICE_NAME);
        esp_ble_gap_config_adv_data(&(esp_ble_adv_data_t){
            .set_scan_rsp = false,
            .include_name = true,
            .include_txpower = true,
            .min_interval = 0x0006,
            .max_interval = 0x0010,
            .appearance = 0x00,
            .manufacturer_len = 0,
            .p_manufacturer_data = NULL,
            .service_data_len = 0,
            .p_service_data = NULL,
            .service_uuid_len = 0,
            .p_service_uuid = NULL,
            .flag = (ESP_BLE_ADV_FLAG_GEN_DISC | ESP_BLE_ADV_FLAG_BREDR_NOT_SPT),
        });
        
        // Tạo Service
        esp_gatt_srvc_id_t service_id = {
            .is_primary = true,
            .id.inst_id = 0x00,
            .id.uuid.len = ESP_UUID_LEN_128,
        };
        // UUID Giả lập UART (Nordic)
        uint8_t service_uuid128[16] = {0x9E, 0xCA, 0xDC, 0x24, 0x0E, 0xE5, 0xA9, 0xE0, 0x93, 0xF3, 0xA3, 0xB5, 0x01, 0x00, 0x40, 0x6E};
        memcpy(service_id.id.uuid.uuid.uuid128, service_uuid128, 16);
        
        esp_ble_gatts_create_service(gatts_if, &service_id, 4);
        break;
    }
    case ESP_GATTS_CREATE_EVT: {
        gl_profile.service_handle = param->create.service_handle;
        
        // Add RX Characteristic (Để nhận Wifi từ điện thoại)
        esp_bt_uuid_t char_rx_uuid = { .len = ESP_UUID_LEN_128 };
        uint8_t rx_uuid128[16] = {0x9E, 0xCA, 0xDC, 0x24, 0x0E, 0xE5, 0xA9, 0xE0, 0x93, 0xF3, 0xA3, 0xB5, 0x02, 0x00, 0x40, 0x6E};
        memcpy(char_rx_uuid.uuid.uuid128, rx_uuid128, 16);

        esp_ble_gatts_add_char(gl_profile.service_handle, &char_rx_uuid,
                               ESP_GATT_PERM_READ | ESP_GATT_PERM_WRITE,
                               ESP_GATT_CHAR_PROP_BIT_WRITE, 
                               NULL, NULL);
        break;
    }
    case ESP_GATTS_ADD_CHAR_EVT: {
        // Sau khi add RX xong thì add tiếp cái khác nếu cần, hoặc Start Service
        if (param->add_char.char_uuid.uuid.uuid128[12] == 0x02) { // Nếu là RX Char
             esp_ble_gatts_start_service(gl_profile.service_handle);
        }
        break;
    }
    case ESP_GATTS_CONNECT_EVT:
        connection_handle = param->connect.conn_id;
        ESP_LOGI(TAG, "Device Connected");
        break;
    case ESP_GATTS_DISCONNECT_EVT:
        ESP_LOGI(TAG, "Device Disconnected");
        esp_ble_gap_start_advertising(&(esp_ble_adv_params_t){
            .adv_int_min = 0x20, .adv_int_max = 0x40, .adv_type = ADV_TYPE_IND,
            .own_addr_type = BLE_ADDR_TYPE_PUBLIC, .channel_map = ADV_CHNL_ALL,
            .adv_filter_policy = ADV_FILTER_ALLOW_SCAN_ANY_CON_ANY,
        });
        break;
    case ESP_GATTS_WRITE_EVT: {
        // KHI NHẬN ĐƯỢC DỮ LIỆU TỪ ĐIỆN THOẠI
        if (param->write.len > 0) {
            char *data = (char *)malloc(param->write.len + 1);
            memcpy(data, param->write.value, param->write.len);
            data[param->write.len] = 0; // Null terminate
            
            ESP_LOGI(TAG, "Received BLE Data: %s", data);
            
            extract_wifi_info(data, param->write.len);
            free(data);

            esp_restart();
        }
        break;
    }
    default: break;
    }
}

esp_err_t bluetooth_manager_init(){
    ESP_ERROR_CHECK(esp_bt_controller_mem_release(ESP_BT_MODE_CLASSIC_BT));

    esp_bt_controller_config_t bt_cfg = BT_CONTROLLER_INIT_CONFIG_DEFAULT();
    esp_err_t ret = esp_bt_controller_init(&bt_cfg);
    if (ret) { ESP_LOGE(TAG, "BT controller init failed"); return ret; }

    ret = esp_bt_controller_enable(ESP_BT_MODE_BLE);
    if (ret) { ESP_LOGE(TAG, "BT enable failed"); return ret; }

    ret = esp_bluedroid_init();
    if (ret) { ESP_LOGE(TAG, "Bluedroid init failed"); return ret; }

    ret = esp_bluedroid_enable();
    if (ret) { ESP_LOGE(TAG, "Bluedroid enable failed"); return ret; }

    // 5. Register Callback
    esp_ble_gatts_register_callback(gatts_profile_event_handler);
    esp_ble_gap_register_callback(gap_event_handler);
    esp_ble_gatts_app_register(0);

    return ESP_OK;
}