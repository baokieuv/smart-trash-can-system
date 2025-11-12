#ifndef GPIO_HANDLER_H
#define GPIO_HANDLER_H

#include "esp_err.h"
#include "freertos/FreeRTOS.h"
#include "freertos/event_groups.h"
#include "config.h"

/**
 * @brief Initialize GPIO pins and button handler
 * @param callback Function to call when button is pressed
 */
esp_err_t gpio_handler_init(EventGroupHandle_t g_event_group);

void beep_pattern(int count, int duration);

void blink_led(int times);

void indicate_waste_category(waste_category_t category);

#endif // GPIO_HANDLER_H