#include "gpio_handler.h"
#include "driver/gpio.h"
#include "esp_log.h"
#include "esp_timer.h"
#include <string.h>

static const char *TAG = "GPIO";
static EventGroupHandle_t s_event_group = NULL;
static volatile int64_t g_button_press_time = 0;

static void IRAM_ATTR button_isr_handler(void* arg) {
    int64_t now = esp_timer_get_time() / 1000;  // Convert to ms
    
    if (gpio_get_level(BTN_CONFIG_PIN) == 0) {
        // Button pressed
        g_button_press_time = now;
    } else {
        // Button released
        int64_t press_duration = now - g_button_press_time;
        
        if (press_duration >= BUTTON_LONG_PRESS_MS) {
            // Long press: enter config mode
            BaseType_t xHigherPriorityTaskWoken = pdFALSE;
            EventBits_t bits = xEventGroupGetBitsFromISR(s_event_group);
            if(bits & CONFIG_MODE_BIT) xEventGroupSetBitsFromISR(s_event_group, EXIT_CONFIG_MODE_BIT, &xHigherPriorityTaskWoken);
            else xEventGroupSetBitsFromISR(s_event_group, CONFIG_MODE_BIT, &xHigherPriorityTaskWoken);
            portYIELD_FROM_ISR(xHigherPriorityTaskWoken);
        }
        
        g_button_press_time = 0;
    }
}

static void led_buzzer_init(void) {
    gpio_config_t led_cfg = {
        .pin_bit_mask = (1ULL << LED_RECYCLABLE_PIN) | 
                       (1ULL << LED_COMPOSTABLE_PIN) | 
                       (1ULL << LED_HAZARDOUS_PIN) |
                       (1ULL << LED_STATUS_PIN) |
                       (1ULL << BUZZER_PIN),
        .mode = GPIO_MODE_OUTPUT,
        .pull_up_en = GPIO_PULLUP_DISABLE,
        .pull_down_en = GPIO_PULLDOWN_ENABLE,
        .intr_type = GPIO_INTR_DISABLE,
    };
    ESP_ERROR_CHECK(gpio_config(&led_cfg));
    
    // Turn off all LEDs
    gpio_set_level(LED_RECYCLABLE_PIN, 0);
    gpio_set_level(LED_COMPOSTABLE_PIN, 0);
    gpio_set_level(LED_HAZARDOUS_PIN, 0);
    gpio_set_level(LED_STATUS_PIN, 0);
    gpio_set_level(BUZZER_PIN, 0);
    
    ESP_LOGI(TAG, "LED & Buzzer initialized");
}

static void button_init(void) {
    gpio_config_t btn_cfg = {
        .pin_bit_mask = (1ULL << BTN_CONFIG_PIN),
        .mode = GPIO_MODE_INPUT,
        .pull_up_en = GPIO_PULLUP_ENABLE,
        .pull_down_en = GPIO_PULLDOWN_DISABLE,
        .intr_type = GPIO_INTR_ANYEDGE,
    };
    ESP_ERROR_CHECK(gpio_config(&btn_cfg));
    
    ESP_ERROR_CHECK(gpio_install_isr_service(0));
    ESP_ERROR_CHECK(gpio_isr_handler_add(BTN_CONFIG_PIN, button_isr_handler, NULL));
    
    ESP_LOGI(TAG, "Button initialized (GPIO%d)", BTN_CONFIG_PIN);
}

esp_err_t gpio_handler_init(EventGroupHandle_t g_event_group) {
    ESP_LOGI(TAG, "Initializing GPIO...");

    s_event_group = g_event_group;

    led_buzzer_init();
    button_init();

    return ESP_OK;
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

void indicate_waste_category(waste_category_t category) {
    // Turn off all LEDs first
    gpio_set_level(LED_RECYCLABLE_PIN, 0);
    gpio_set_level(LED_COMPOSTABLE_PIN, 0);
    gpio_set_level(LED_HAZARDOUS_PIN, 0);
    
    // Turn on appropriate LED
    switch (category) {
        case WASTE_RECYCLABLE:
            ESP_LOGI(TAG, "Indicating RECYCLABLE waste");
            gpio_set_level(LED_RECYCLABLE_PIN, 1);
            break;
        case WASTE_COMPOSTABLE:
            ESP_LOGI(TAG, "Indicating COMPOSTABLE waste");
            gpio_set_level(LED_COMPOSTABLE_PIN, 1);
            break;
        case WASTE_HAZARDOUS:
            ESP_LOGI(TAG, "Indicating HAZARDOUS waste");
            gpio_set_level(LED_HAZARDOUS_PIN, 1);
            break;
        default:
            ESP_LOGW(TAG, "Unknown waste category");
            break;
    }
    
    // Keep LED on for indication duration
    vTaskDelay(pdMS_TO_TICKS(LED_INDICATION_DURATION_MS));
    
    // Turn off LED
    gpio_set_level(LED_RECYCLABLE_PIN, 0);
    gpio_set_level(LED_COMPOSTABLE_PIN, 0);
    gpio_set_level(LED_HAZARDOUS_PIN, 0);
}