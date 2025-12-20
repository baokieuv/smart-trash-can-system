#include "bluetooth_manager.h"
#include <string.h>
#include "esp_log.h"
#include "nvs_storage.h"
#include "cJSON.h"
#include "freertos/FreeRTOS.h"
#include "freertos/event_groups.h"

#define GATTS_SERVICE_UUID        0x00FF
#define GATTS_CHAR_UUID_SSID      0xFF01 
#define GATTS_CHAR_UUID_PASS      0xFF02
#define GATTS_NUM_HANDLE_TEST     6

#define TAG "BLE"
#define DEVICE_NAME "SMART_BIN_SETUP"

static char temp_ssid[33] = {0};    
static char temp_pass[65] = {0};

uint16_t ssid_char_handle = 0;
uint16_t pass_char_handle = 0;
uint16_t service_handle_saved = 0;

extern EventGroupHandle_t g_event_group;

void restart_task(void *pvParameter) {
    vTaskDelay(pdMS_TO_TICKS(2000)); 
    ESP_LOGI(TAG, "Restarting system now...");
    esp_restart();
}

void save_wifi_credentials(){
    if(strlen(temp_ssid) > 0 && strlen(temp_pass) > 0){
        ESP_LOGI(TAG, "Saving Credentials - SSID: %s", temp_ssid);
        esp_err_t err = nvs_save_wifi_config(temp_ssid, temp_pass);
        if(err == ESP_OK){
            ESP_LOGI(TAG, "Saved to NVS successfully.");
            if(xEventGroupGetBits(g_event_group) & RESET_MODE_BIT){
                waste_manager_reset_stats(); 
            }
            xTaskCreate(restart_task, "Restart task", 2048, NULL, 5, NULL);
        }else{
            ESP_LOGE(TAG, "Failed to save to NVS");
        }
    }else{
        ESP_LOGW(TAG, "Waiting for full credentials (SSID or Pass missing)");
    }
}
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

    xTaskCreate(restart_task, "Restart task", 2048, NULL, 5, NULL);
    ret = ESP_OK;
cleanup:
    if (root) cJSON_Delete(root);
    return ret;
}

static void gap_event_handler(esp_gap_ble_cb_event_t event, esp_ble_gap_cb_param_t *param){
    switch (event)
    {
    case ESP_GAP_BLE_ADV_DATA_SET_COMPLETE_EVT:
        esp_ble_gap_start_advertising(&(esp_ble_adv_params_t){
            .adv_int_min        = 0x20,
            .adv_int_max        = 0x40,
            .adv_type           = ADV_TYPE_IND,
            .own_addr_type      = BLE_ADDR_TYPE_PUBLIC,
            .channel_map        = ADV_CHNL_ALL,
            .adv_filter_policy  = ADV_FILTER_ALLOW_SCAN_ANY_CON_ANY,
        });
        break;
    case ESP_GAP_BLE_PASSKEY_NOTIF_EVT:
        ESP_LOGE(TAG, "MA GHEP DOI (PAIR CODE): %06"PRIu32, param->ble_security.key_notif.passkey);
        break;
    case ESP_GAP_BLE_AUTH_CMPL_EVT:
        if(param->ble_security.auth_cmpl.success){
            ESP_LOGI(TAG, "Devices paired successfully.");
        }else{
            ESP_LOGE(TAG, "Cannot connect to app: 0x%x", param->ble_security.auth_cmpl.fail_reason);
        }
        break;
    case ESP_GAP_BLE_SEC_REQ_EVT:
        esp_ble_gap_security_rsp(param->ble_security.ble_req.bd_addr, true);
        break;
    default:
        break;
    }
}

static void gatts_event_handler(esp_gatts_cb_event_t event, esp_gatt_if_t gatts_if, esp_ble_gatts_cb_param_t *param){
    switch (event)
    {
    case ESP_GATTS_REG_EVT:
        esp_ble_gap_set_device_name(DEVICE_NAME);

        esp_ble_gap_config_adv_data(&(esp_ble_adv_data_t){
            .set_scan_rsp = false,
            .include_name = true,
            .flag = (ESP_BLE_ADV_FLAG_GEN_DISC | ESP_BLE_ADV_FLAG_BREDR_NOT_SPT), 
        });

        esp_ble_gatts_create_service(gatts_if, &(esp_gatt_srvc_id_t){
            .is_primary = true, .id.inst_id = 0x00,
            .id.uuid.len = ESP_UUID_LEN_16, .id.uuid.uuid.uuid16 = GATTS_SERVICE_UUID
        }, GATTS_NUM_HANDLE_TEST);
        break;
    case ESP_GATTS_CREATE_EVT:
        ESP_LOGI(TAG, "Service Created, handle: %d", param->create.service_handle);
        service_handle_saved = param->create.service_handle;
        esp_ble_gatts_start_service(service_handle_saved);

        esp_ble_gatts_add_char(service_handle_saved, &(esp_bt_uuid_t){
            .len = ESP_UUID_LEN_16, .uuid.uuid16 = GATTS_CHAR_UUID_SSID
        }, ESP_GATT_PERM_WRITE_ENC_MITM, ESP_GATT_CHAR_PROP_BIT_WRITE, NULL, NULL);
        break;
    case ESP_GATTS_ADD_CHAR_EVT:
        if(param->add_char.char_uuid.uuid.uuid16 == GATTS_CHAR_UUID_SSID){
            ssid_char_handle = param->add_char.attr_handle;
            ESP_LOGI(TAG, "SSID Handle: %d.", ssid_char_handle);
            esp_ble_gatts_add_char(param->add_char.service_handle, &(esp_bt_uuid_t){
                .len = ESP_UUID_LEN_16, .uuid.uuid16 = GATTS_CHAR_UUID_PASS
            }, ESP_GATT_PERM_WRITE_ENC_MITM, ESP_GATT_CHAR_PROP_BIT_WRITE, NULL, NULL);
        } else if(param->add_char.char_uuid.uuid.uuid16 == GATTS_CHAR_UUID_PASS){
            pass_char_handle = param->add_char.attr_handle;
            ESP_LOGI(TAG, "Pass Handle: %d. Ready.", pass_char_handle);
        }
        break;
    case ESP_GATTS_CONNECT_EVT:
        ESP_LOGI(TAG, "Device connected");
        esp_ble_set_encryption(param->connect.remote_bda, ESP_BLE_SEC_ENCRYPT_MITM);
        break;
    case ESP_GATTS_DISCONNECT_EVT:
        ESP_LOGI(TAG, "Disconected, advertising...");
        esp_ble_gap_start_advertising(&(esp_ble_adv_params_t){
            .adv_int_min = 0x20, .adv_int_max = 0x40, .adv_type = ADV_TYPE_IND, 
            .own_addr_type = BLE_ADDR_TYPE_PUBLIC, .channel_map = ADV_CHNL_ALL, 
            .adv_filter_policy = ADV_FILTER_ALLOW_SCAN_ANY_CON_ANY
        });
        break;
    case ESP_GATTS_WRITE_EVT:
        ESP_LOGI(TAG, "Data Write: Handle %d, Len %d", param->write.handle, param->write.len);
        if(param->write.handle == ssid_char_handle) {
            memset(temp_ssid, 0, sizeof(temp_ssid));
            memcpy(temp_ssid, param->write.value, param->write.len);
            temp_ssid[param->write.len + 1] = '\0';
            ESP_LOGI(TAG, "Received SSID: %s", temp_ssid);
        }else if(param->write.handle == pass_char_handle){
            memset(temp_pass, 0, sizeof(temp_pass));
            memcpy(temp_pass, param->write.value, param->write.len);
            temp_pass[param->write.len + 1] = '\0';
            ESP_LOGI(TAG, "Received Pass: %s", temp_pass);

            save_wifi_credentials();
        }

        if(param->write.need_rsp){
            esp_ble_gatts_send_response(gatts_if, param->write.conn_id, param->write.trans_id, ESP_GATT_OK, NULL);
        }
        break;
    default:
        break;
    }
}

esp_err_t bluetooth_manager_init(){
    ESP_ERROR_CHECK(esp_bt_controller_mem_release(ESP_BT_MODE_CLASSIC_BT));

    esp_bt_controller_config_t bt_cfg = BT_CONTROLLER_INIT_CONFIG_DEFAULT();
    ESP_ERROR_CHECK(esp_bt_controller_init(&bt_cfg));
    ESP_ERROR_CHECK(esp_bt_controller_enable(ESP_BT_MODE_BLE));
    ESP_ERROR_CHECK(esp_bluedroid_init());
    ESP_ERROR_CHECK(esp_bluedroid_enable());

    esp_ble_io_cap_t iocap = ESP_IO_CAP_OUT; 
    esp_ble_gap_set_security_param(ESP_BLE_SM_IOCAP_MODE, &iocap, sizeof(uint8_t));

    esp_ble_auth_req_t auth_req = ESP_LE_AUTH_REQ_SC_MITM_BOND;
    esp_ble_gap_set_security_param(ESP_BLE_SM_AUTHEN_REQ_MODE, &auth_req, sizeof(uint8_t));

    uint32_t passkey = 123456; // <--- Đặt mã PIN mong muốn (6 chữ số)
    esp_ble_gap_set_security_param(ESP_BLE_SM_SET_STATIC_PASSKEY, &passkey, sizeof(uint32_t));

    uint8_t key_size = 16;
    uint8_t init_key = ESP_BLE_ENC_KEY_MASK | ESP_BLE_ID_KEY_MASK;
    uint8_t rsp_key = ESP_BLE_ENC_KEY_MASK | ESP_BLE_ID_KEY_MASK;
    esp_ble_gap_set_security_param(ESP_BLE_SM_MAX_KEY_SIZE, &key_size, sizeof(uint8_t));
    esp_ble_gap_set_security_param(ESP_BLE_SM_SET_INIT_KEY, &init_key, sizeof(uint8_t));
    esp_ble_gap_set_security_param(ESP_BLE_SM_SET_RSP_KEY, &rsp_key, sizeof(uint8_t));

    esp_ble_gap_register_callback(gap_event_handler);
    esp_ble_gatts_register_callback(gatts_event_handler);
    esp_ble_gatts_app_register(0);
    return ESP_OK;
}