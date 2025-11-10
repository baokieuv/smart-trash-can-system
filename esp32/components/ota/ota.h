#ifndef OTA_H
#define OTA_H

#include "esp_https_ota.h"
#include "esp_http_client.h"

static esp_err_t _http_client_init_cb(esp_http_client_handle_t http_client);

esp_err_t ota_handle(void *param);
#endif