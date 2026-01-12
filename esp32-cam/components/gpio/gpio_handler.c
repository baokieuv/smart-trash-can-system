#include "gpio_handler.h"
#include "driver/gpio.h"
#include "esp_log.h"
#include "iot_button.h"
#include "esp_event.h"

static const char *TAG = "GPIO";

ESP_EVENT_DEFINE_BASE(APP_BUTTON_EVENT);

extern EventGroupHandle_t g_event_group;
static volatile int64_t g_button_press_time = 0;

static void button_signle_click_cb(void *args, void *data){
    ESP_LOGI(TAG, "Handle single click");
    esp_event_post(APP_BUTTON_EVENT, BUTTON_EVENT_SINGLE_CLICK, NULL, 0, portMAX_DELAY); 
}

static void button_double_click_cb(void *args, void *data){
    ESP_LOGI(TAG, "Handle double click");
    esp_event_post(APP_BUTTON_EVENT, BUTTON_EVENT_DOUBLE_CLICK, NULL, 0, portMAX_DELAY); 
}

static void button_long_press_cb(void *args, void *data){
    ESP_LOGI(TAG, "Handle long press");
    esp_event_post(APP_BUTTON_EVENT, BUTTON_EVENT_LONG_PRESS, NULL, 0, portMAX_DELAY); 
}

static esp_err_t button_manager_init(gpio_num_t pin_num) {
    ESP_LOGI(TAG, "Initializing Button on GPIO %d", pin_num);

    button_config_t conf = {
        .type = BUTTON_TYPE_GPIO,
        .long_press_time = BUTTON_LONG_PRESS_MS,
        .short_press_time = BUTTON_SHORT_PRESS_MS,
        .gpio_button_config = {
            .gpio_num = pin_num,
            .active_level = 0,
            .disable_pull = false,
        },
    };

    button_handle_t btn_handler = iot_button_create(&conf);
    if(btn_handler == NULL) {
        ESP_LOGE(TAG, "Button create failed");
        return ESP_FAIL;
    }

    iot_button_register_cb(btn_handler, BUTTON_SINGLE_CLICK, button_signle_click_cb, NULL);
    iot_button_register_cb(btn_handler, BUTTON_DOUBLE_CLICK, button_double_click_cb, NULL);
    iot_button_register_cb(btn_handler, BUTTON_LONG_PRESS_START, button_long_press_cb, NULL);
    
    return ESP_OK;
}

static void led_buzzer_init(void) {
    gpio_config_t led_cfg = {
        .pin_bit_mask = (1ULL << LED_STATUS_PIN) | (1ULL << BUZZER_PIN),
        .mode = GPIO_MODE_OUTPUT,
        .pull_up_en = GPIO_PULLUP_DISABLE,
        .pull_down_en = GPIO_PULLDOWN_ENABLE,
        .intr_type = GPIO_INTR_DISABLE,
    };
    ESP_ERROR_CHECK(gpio_config(&led_cfg));
    
    gpio_set_level(LED_STATUS_PIN, 0);
    gpio_set_level(BUZZER_PIN, 0);
    
    ESP_LOGI(TAG, "LED & Buzzer initialized");
}

esp_err_t gpio_handler_init() {
    ESP_LOGI(TAG, "Initializing GPIO...");

    esp_err_t ret = ESP_OK;

    led_buzzer_init();
    ret = button_manager_init(BTN_CONFIG_PIN);

    return ret;
}

void beep_pattern(int count, int duration){
    for(int i = 0; i < count; i++){
        gpio_set_level(BUZZER_PIN, 1);
        vTaskDelay(pdMS_TO_TICKS(duration));
        gpio_set_level(BUZZER_PIN, 0);
        vTaskDelay(pdMS_TO_TICKS(duration));
    }
}

void blink_led(int times) {
    for (int i = 0; i < times; i++) {
        gpio_set_level(LED_STATUS_PIN, 1);
        vTaskDelay(pdMS_TO_TICKS(200));
        gpio_set_level(LED_STATUS_PIN, 0);
        vTaskDelay(pdMS_TO_TICKS(200));
    }
}

// void indicate_waste_category(waste_category_t category) {
    // Turn off all LEDs first
//     gpio_set_level(LED_RECYCLABLE_PIN, 0);
//     gpio_set_level(LED_COMPOSTABLE_PIN, 0);
//     gpio_set_level(LED_HAZARDOUS_PIN, 0);
    
//     // Turn on appropriate LED
//     switch (category) {
//         case WASTE_RECYCLABLE:
//             ESP_LOGI(TAG, "Indicating RECYCLABLE waste");
//             gpio_set_level(LED_RECYCLABLE_PIN, 1);
//             break;
//         case WASTE_COMPOSTABLE:
//             ESP_LOGI(TAG, "Indicating COMPOSTABLE waste");
//             gpio_set_level(LED_COMPOSTABLE_PIN, 1);
//             break;
//         case WASTE_HAZARDOUS:
//             ESP_LOGI(TAG, "Indicating HAZARDOUS waste");
//             gpio_set_level(LED_HAZARDOUS_PIN, 1);
//             break;
//         default:
//             ESP_LOGW(TAG, "Unknown waste category");
//             break;
//     }
    
//     // Keep LED on for indication duration
//     vTaskDelay(pdMS_TO_TICKS(LED_INDICATION_DURATION_MS));
    
//     // Turn off LED
//     gpio_set_level(LED_RECYCLABLE_PIN, 0);
//     gpio_set_level(LED_COMPOSTABLE_PIN, 0);
//     gpio_set_level(LED_HAZARDOUS_PIN, 0);
// }