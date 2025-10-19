#include <driver/gpio.h>
#include <stdlib.h>
#include "esp_timer.h"
#include "esp_log.h"
#include "nvs_flash.h"
#include "esp_camera.h"

#include "esp_mac.h"
#include "esp_wifi.h"
#include "esp_event.h"
#include "esp_http_client.h"

#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/event_groups.h"
#include "freertos/semphr.h"
#include "freertos/queue.h"

#include "cJSON.h"

#define TAG             "IOT"
#define WIFI_SSID       "TP-Link_E276"
#define WIFI_PASS       "56445466"
#define SERVER_URL      "http://192.168.56.1:3001/api/detect"
#define MAXIMUM_RETRY   5

#define CAM_TASK_STACK_SIZE         (4 * 1024)
#define HTTP_TASK_STACK_SIZE        (4 * 1024)
#define IMAGE_CAPTURE_INTERVAL_MS   1000 
#define IMAGE_QUEUE_LENGTH          1
#define BTN_QUEUE_LENGTH            1

#define BUTTON_PIN          GPIO_NUM_12
#define RECYCLABLE_LED      GPIO_NUM_13
#define COMPOSTABLE_LED     GPIO_NUM_14
#define NONRECYCLABLE_LED   GPIO_NUM_15

#define CAM_PIN_PWDN     32
#define CAM_PIN_RESET    -1 //software reset will be performed
#define CAM_PIN_XCLK     0
#define CAM_PIN_SIOD     26
#define CAM_PIN_SIOC     27
#define CAM_PIN_D7       35
#define CAM_PIN_D6       34
#define CAM_PIN_D5       39
#define CAM_PIN_D4       36
#define CAM_PIN_D3       21
#define CAM_PIN_D2       19
#define CAM_PIN_D1       18
#define CAM_PIN_D0       5
#define CAM_PIN_VSYNC    25
#define CAM_PIN_HREF     23
#define CAM_PIN_PCLK     22

#define WIFI_CONNECTED_BIT  BIT0
#define WIFI_FAIL_BIT       BIT1

static EventGroupHandle_t event_group;
static int s_retry_num = 0;
static QueueHandle_t s_image_queue;
static QueueHandle_t btn_queue;

esp_err_t init_cam(void){
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

        .pixel_format = PIXFORMAT_JPEG, //YUV422,GRAYSCALE,RGB565,JPEG
        .frame_size = FRAMESIZE_QVGA,     //QQVGA-UXGA, For ESP32, do not use sizes above QVGA when not JPEG. The performance of the ESP32-S series has improved a lot, but JPEG mode always gives better frame rates.

        .jpeg_quality = 10, //0-63, for OV series camera sensors, lower number means higher quality
        .fb_count = 2,      //When jpeg mode is used, if fb_count more than one, the driver will work in continuous mode.
        .fb_location = CAMERA_FB_IN_PSRAM,
        .grab_mode = CAMERA_GRAB_LATEST,
    };

    esp_err_t err = esp_camera_init(&camera_cfg);
    if(err != ESP_OK){
        ESP_LOGE(TAG, "Camera init failed");
    }else{
        ESP_LOGI(TAG, "Init camera done");
    }
    return err;
}

static void event_handler(void* arg, esp_event_base_t event_base, int32_t event_id, void* event_data)
{
    if (event_base == WIFI_EVENT && event_id == WIFI_EVENT_STA_START) {
        esp_wifi_connect();
    } else if (event_base == WIFI_EVENT && event_id == WIFI_EVENT_STA_DISCONNECTED) {
        if (s_retry_num < MAXIMUM_RETRY) {
            esp_wifi_connect();
            s_retry_num++;
            ESP_LOGI(TAG, "retry to connect to the AP");
        } else {
            xEventGroupSetBits(event_group, WIFI_FAIL_BIT);
            ESP_LOGI(TAG,"connect to the AP fail");   
        }
    } else if (event_base == IP_EVENT && event_id == IP_EVENT_STA_GOT_IP) {
        ip_event_got_ip_t* event = (ip_event_got_ip_t*) event_data;
        ESP_LOGI(TAG, "got ip:" IPSTR, IP2STR(&event->ip_info.ip));
        s_retry_num = 0;
        xEventGroupSetBits(event_group, WIFI_CONNECTED_BIT);
    }
}

static void wifi_init_sta(void)
{
    ESP_ERROR_CHECK(esp_netif_init());
    ESP_ERROR_CHECK(esp_event_loop_create_default());
    esp_netif_create_default_wifi_sta();

    ESP_ERROR_CHECK(esp_event_handler_instance_register(WIFI_EVENT, ESP_EVENT_ANY_ID, &event_handler, NULL, NULL));
    ESP_ERROR_CHECK(esp_event_handler_instance_register(IP_EVENT, IP_EVENT_STA_GOT_IP, &event_handler, NULL, NULL));

    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
    ESP_ERROR_CHECK(esp_wifi_init(&cfg));

    wifi_config_t wifi_config = {
        .sta = {
            .ssid = WIFI_SSID,
            .password = WIFI_PASS,
        },
    };
    ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_STA) );
    ESP_ERROR_CHECK(esp_wifi_set_config(WIFI_IF_STA, &wifi_config) );
    ESP_ERROR_CHECK(esp_wifi_start());

    ESP_LOGI(TAG, "wifi_init_sta finished.");
}

void led_task(void *param){
    cJSON *root = cJSON_Parse((char*)param);
    free(param);
    if (!root) {
        ESP_LOGE("JSON", "Failed to parse JSON");
        vTaskDelete(NULL);
        return;
    }
    
    const cJSON *category = cJSON_GetObjectItem(root, "category");
    const cJSON *conf  = cJSON_GetObjectItem(root, "conf");

    char led = 'n';
    if(cJSON_IsNumber(conf) && conf->valuedouble > 50.0){
        if(strcmp(category->valuestring, "recyclable") == 0){
            led = 'r';
        }else if(strcmp(category->valuestring, "compostable") == 0){
            led = 'c';
        }
    }
    cJSON_Delete(root);

    if(led == 'r') ESP_ERROR_CHECK(gpio_set_level(RECYCLABLE_LED, 1));
    else if(led == 'c') ESP_ERROR_CHECK(gpio_set_level(COMPOSTABLE_LED, 1));
    else ESP_ERROR_CHECK(gpio_set_level(NONRECYCLABLE_LED, 1));

    vTaskDelay(pdMS_TO_TICKS(2000));

    ESP_ERROR_CHECK(gpio_set_level(RECYCLABLE_LED, 0));
    ESP_ERROR_CHECK(gpio_set_level(COMPOSTABLE_LED, 0));
    ESP_ERROR_CHECK(gpio_set_level(NONRECYCLABLE_LED, 0));

    vTaskDelete(NULL);
}

esp_err_t http_event_handler(esp_http_client_event_t *evt){
    switch (evt->event_id)
    {
    case HTTP_EVENT_ERROR:
        ESP_LOGI(TAG, "HTTP_EVENT_ERROR");
        break;
    case HTTP_EVENT_ON_CONNECTED:
        ESP_LOGI(TAG, "HTTP_EVENT_ON_CONNECTED");
        break;
    case HTTP_EVENT_ON_DATA:
        ESP_LOGI(TAG, "HTTP_EVENT_ON_DATA: %d", evt->data_len);
        char *data = (char*) malloc(evt->data_len * sizeof(char));
        memcpy(data, evt->data, evt->data_len);
        xTaskCreate(led_task, "led_task", 2048, (void*)data, 5, NULL);
        break;
    case HTTP_EVENT_DISCONNECTED:
        ESP_LOGI(TAG, "HTTP_EVENT_DISCONNECTED");
        break;
    default:
        break;
    }
    return ESP_OK;
}

void http_send_task(void *param){
    camera_fb_t *pic = NULL;
    const char *boundary = "----myboundary";

    esp_http_client_config_t config = {
        .url = SERVER_URL,
        .event_handler = http_event_handler,
        .method = HTTP_METHOD_POST,
        .timeout_ms = 10000,
    };

    esp_http_client_handle_t client = esp_http_client_init(&config);
    
    char content_type[64];
    snprintf(content_type, sizeof(content_type), "multipart/form-data; boundary=%s", boundary);
    esp_http_client_set_header(client, "Content-Type", content_type);

    ESP_LOGI(TAG, "HTTP Send Task started. Waiting for images...");

    while(1){
        if(xQueueReceive(s_image_queue, &pic, portMAX_DELAY)){
            if(!pic){
                ESP_LOGE(TAG, "Received NULL frame from queue.");
                continue;
            }

            char header[256];
            int header_len = snprintf(header, sizeof(header),
                "--%s\r\n"
                "Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n"
                "Content-Type: image/jpeg\r\n\r\n",
                boundary);

            char footer[64];
            int footer_len = snprintf(footer, sizeof(footer), "\r\n--%s--\r\n", boundary);

            int total_len = header_len + pic->len + footer_len;

            esp_err_t err = esp_http_client_open(client, total_len);
            if(err != ESP_OK){
                ESP_LOGE(TAG, "Failed to open HTTP connection: %s", esp_err_to_name(err));
            }else{
                esp_http_client_write(client, header, header_len);
                esp_http_client_write(client, (const char *)pic->buf, pic->len);
                esp_http_client_write(client, footer, footer_len);
            
                err = esp_http_client_perform(client);
                if (err == ESP_OK) {
                    ESP_LOGI(TAG, "HTTP POST Status = %d", esp_http_client_get_status_code(client));
                } else {
                    ESP_LOGE(TAG, "HTTP POST request failed: %s", esp_err_to_name(err));
                }
            }

            esp_http_client_close(client); 
            esp_camera_fb_return(pic);
            pic = NULL;
        }
    }

    esp_http_client_cleanup(client);
    vTaskDelete(NULL);
}

void camera_task(void *param){
    camera_fb_t *pic = NULL;
    while(1){
        char c = 'x';
        if(xQueueReceive(btn_queue, &c, portMAX_DELAY)){
            for(int i = 0; i < 5; i++){
                pic = esp_camera_fb_get();
                esp_camera_fb_return(pic);
                pic = NULL;
                vTaskDelay(pdMS_TO_TICKS(100));
            }

            pic = esp_camera_fb_get();

            if(!pic){
                ESP_LOGE(TAG, "Camera capture failed");
                vTaskDelay(pdMS_TO_TICKS(1000));
                continue;
            }

            if(xQueueSend(s_image_queue, &pic, (TickType_t)0) != pdPASS){
                ESP_LOGW(TAG, "Image queue full. Discarding frame.");
                esp_camera_fb_return(pic);
                pic = NULL;
            }
        }
    }
}

void IRAM_ATTR btn_handler(void *param){
    static uint64_t last_time = 0;
    char c = 'a';
    if(esp_timer_get_time() - last_time > 200000){
        last_time = esp_timer_get_time();
        BaseType_t xHigherPriorityTaskWoken = pdFALSE;
        xQueueSendFromISR(btn_queue, &c, &xHigherPriorityTaskWoken);
        if (xHigherPriorityTaskWoken) {
            portYIELD_FROM_ISR();
        }
    }
}

void init_gpio(void){
    gpio_config_t io_cfg = {
        .pin_bit_mask = (1ULL << BUTTON_PIN),
        .mode = GPIO_MODE_INPUT,
        .pull_down_en = 1,
        .pull_up_en = 0,
        .intr_type = GPIO_INTR_POSEDGE,
    };

    ESP_ERROR_CHECK(gpio_config(&io_cfg));
    ESP_ERROR_CHECK(gpio_isr_handler_add(BUTTON_PIN, btn_handler, NULL));

    gpio_config_t led_cfg = {
        .pin_bit_mask = (1ULL << RECYCLABLE_LED) | (1ULL << COMPOSTABLE_LED) | (1ULL << NONRECYCLABLE_LED),
        .mode = GPIO_MODE_OUTPUT,
        .pull_up_en = 0,
        .pull_down_en = 1,
        .intr_type = GPIO_INTR_DISABLE,
    };
    ESP_ERROR_CHECK(gpio_config(&led_cfg));

    ESP_ERROR_CHECK(gpio_set_level(RECYCLABLE_LED, 0));
    ESP_ERROR_CHECK(gpio_set_level(COMPOSTABLE_LED, 0));
    ESP_ERROR_CHECK(gpio_set_level(NONRECYCLABLE_LED, 0));
}

void app_main(void)
{
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
      ESP_ERROR_CHECK(nvs_flash_erase());
      ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);

    event_group = xEventGroupCreate();

    wifi_init_sta();
    if(init_cam() != ESP_OK){
        ESP_LOGE(TAG, "Failed to initalize camera");
        return;
    }
 
    s_image_queue = xQueueCreate(IMAGE_QUEUE_LENGTH, sizeof(camera_fb_t *));
    btn_queue = xQueueCreate(BTN_QUEUE_LENGTH, sizeof(char));

    if (s_image_queue == NULL || btn_queue == NULL) {
        ESP_LOGE(TAG, "Failed to create queues");
        return;
    }

    init_gpio();

    EventBits_t bits = xEventGroupWaitBits(event_group,
                                        WIFI_CONNECTED_BIT | WIFI_FAIL_BIT,
                                        pdFALSE,
                                        pdFALSE,
                                        portMAX_DELAY);

    if (bits & WIFI_CONNECTED_BIT) {
        ESP_LOGI(TAG, "Connected to Wi-Fi successfully");
        xTaskCreate(camera_task, "cam_task", CAM_TASK_STACK_SIZE, NULL, 5, NULL);
        xTaskCreate(http_send_task, "http_task", HTTP_TASK_STACK_SIZE, NULL, 5, NULL);
    } else {
        ESP_LOGE(TAG, "Failed to connect to Wi-Fi");
    }
}
