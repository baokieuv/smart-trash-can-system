#ifndef GPIO_HANDLER_H
#define GPIO_HANDLER_H

#include "esp_err.h"
#include "freertos/FreeRTOS.h"
#include "freertos/event_groups.h"
#include "config.h"
#include "esp_event.h"

ESP_EVENT_DECLARE_BASE(APP_BUTTON_EVENT);

typedef enum
{
    BUTTON_EVENT_SINGLE_CLICK, // Bấm 1 lần (Chuyển AP <-> STA)
    BUTTON_EVENT_DOUBLE_CLICK, // Bấm 2 lần
    BUTTON_EVENT_LONG_PRESS    // Bấm giữ (Chuyển HTTP View)
} app_button_event_id_t;

/**
 * @brief Initialize GPIO pins and button handler
 * @param callback Function to call when button is pressed
 */
esp_err_t gpio_handler_init();

void beep_pattern(int count, int duration);

void blink_led(int times);

void led_status(void *param);
//void indicate_waste_category(waste_category_t category);

#endif // GPIO_HANDLER_H