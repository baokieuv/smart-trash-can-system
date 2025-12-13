#pragma once
#include "driver/ledc.h"
#include "esp_err.h"
#include "config.h"

#define TIME_PER_DEGREE_MS 3

typedef struct {
    gpio_num_t pin;
    ledc_channel_t channel;
    uint16_t stop_pulse_us;   // thường ~1500
    uint16_t range_pulse_us;  // thường ~300–400
} servo360_t;

typedef struct {
    gpio_num_t pin;
    ledc_channel_t channel;
    int min_pulse_us;
    int max_pulse_us;
} servo180_t;

esp_err_t trashlid_init();

void trashlid_open(waste_category_t category);