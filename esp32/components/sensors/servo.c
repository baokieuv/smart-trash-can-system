#include "servo.h"
#include "esp_log.h"
#include "string.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"

#define SERVO_LOG "SERVO"

#define SERVO_PWM_FREQ        50          // 50 Hz
#define SERVO_PERIOD_US       20000
#define SERVO_DUTY_RES        LEDC_TIMER_14_BIT
#define SERVO_DUTY_MAX        ((1 << 14) - 1)

// ========== Servo 360° ==========
// servo360_t sv360 = {
//     .pin = SERVO_360_GPIO,
//     .channel = SERVO_360_CHANNEL,
//     .stop_pulse_us = SERVO_360_STOP_US,
//     .range_pulse_us = SERVO_360_RANGE_US
// };

// ========== Servo 180° ==========
servo180_t sv180_1 = {
    .pin = SERVO_360_GPIO,
    .channel = SERVO_360_CHANNEL,
    .min_pulse_us = SERVO_180_MIN_US,   // SG90
    .max_pulse_us = SERVO_180_MAX_US
};

servo180_t sv180_2 = {
    .pin = SERVO_180_GPIO,
    .channel = SERVO_180_CHANNEL,
    .min_pulse_us = SERVO_180_MIN_US,   // SG90
    .max_pulse_us = SERVO_180_MAX_US
};

// =========================================
// PRIVATE: tạo xung PWM theo micro giây
// =========================================
static void _servo_write_us(ledc_channel_t ch, uint16_t us)
{
    if (us < 500)  us = 500;
    if (us > 2500) us = 2500;

    uint32_t duty = (us * SERVO_DUTY_MAX) / SERVO_PERIOD_US;

    ledc_set_duty(LEDC_LOW_SPEED_MODE, ch, duty);
    ledc_update_duty(LEDC_LOW_SPEED_MODE, ch);
}

static esp_err_t servo360_init(servo360_t *servo)
{
    ledc_timer_config_t timer = {
        .speed_mode = LEDC_LOW_SPEED_MODE,
        .duty_resolution = SERVO_DUTY_RES,
        .timer_num = LEDC_TIMER_0,
        .freq_hz = SERVO_PWM_FREQ,
        .clk_cfg = LEDC_AUTO_CLK
    };
    ledc_timer_config(&timer);

    ledc_channel_config_t channel = {
        .gpio_num = servo->pin,
        .speed_mode = LEDC_LOW_SPEED_MODE,
        .channel = servo->channel,
        .intr_type = LEDC_INTR_DISABLE,
        .timer_sel = LEDC_TIMER_0,
        .duty = 0
    };
    ledc_channel_config(&channel);

    return ESP_OK;
}

// speed = -1.0 (quay trái) → +1.0 (quay phải)
static esp_err_t servo360_set_speed(servo360_t *servo, float speed)
{
    if(speed > 1.0f) speed = 1.0f;
    if(speed < -1.0f) speed = -1.0f;

    // map speed → pulse 1100–1900us
    uint16_t pulse = servo->stop_pulse_us +
                     (int)(speed * servo->range_pulse_us);
    _servo_write_us(servo->channel, pulse);
    return ESP_OK;
}

static esp_err_t servo360_stop(servo360_t *servo)
{
    _servo_write_us(servo->channel, servo->stop_pulse_us);
    return ESP_OK;
}

static esp_err_t servo360_write_angle(servo360_t *servo, int angle){
    if (angle == 0) return ESP_OK;

    int direction = (angle > 0) ? 1 : -1;
    uint32_t duration = abs(angle) * SERVO_360_TIME_PER_DEGREE;

    servo360_set_speed(servo, direction);
    vTaskDelay(pdMS_TO_TICKS(duration));
    servo360_stop(servo);

    return ESP_OK;
}


static esp_err_t servo180_init(servo180_t *servo)
{
    ledc_timer_config_t timer = {
        .speed_mode = LEDC_LOW_SPEED_MODE,
        .duty_resolution = SERVO_DUTY_RES,
        .timer_num = LEDC_TIMER_0,
        .freq_hz = SERVO_PWM_FREQ,
        .clk_cfg = LEDC_AUTO_CLK
    };
    ledc_timer_config(&timer);

    ledc_channel_config_t channel = {
        .gpio_num = servo->pin,
        .speed_mode = LEDC_LOW_SPEED_MODE,
        .channel = servo->channel,
        .intr_type = LEDC_INTR_DISABLE,
        .timer_sel = LEDC_TIMER_0,
        .duty = 0
    };
    ledc_channel_config(&channel);

    return ESP_OK;
}

static esp_err_t servo180_write_angle(servo180_t *servo, int angle)
{
    if(angle < 0) angle = 0;
    if(angle > 180) angle = 180;

    int pulse = servo->min_pulse_us +
                (angle * (servo->max_pulse_us - servo->min_pulse_us) / 180);

    _servo_write_us(servo->channel, pulse);
    return ESP_OK;
}

esp_err_t trashlid_init(){
    esp_err_t ret = ESP_OK;

    ret = servo180_init(&sv180_1);
    if(ret != ESP_OK) return ret;

    ret = servo180_init(&sv180_2);
    if(ret != ESP_OK) return ret;
    // ret = servo360_init(&sv360);
    // if(ret != ESP_OK) return ret;

    return ret;
}

void trashlid_open(waste_category_t category){
    // if(strcmp(category, "recyclable") == 0){
    //     ESP_ERROR_CHECK(servo360_write_angle(&sv360, 60));
    //     ESP_ERROR_CHECK(servo180_write_angle(&sv180, 90));
    //     vTaskDelay(pdMS_TO_TICKS(1000));
    //     ESP_ERROR_CHECK(servo180_write_angle(&sv180, 0));
    //     ESP_ERROR_CHECK(servo360_write_angle(&sv360, -60));
    // }else if(strcmp(category, "non") == 0){
    //     ESP_ERROR_CHECK(servo360_write_angle(&sv360, 180));
    //     ESP_ERROR_CHECK(servo180_write_angle(&sv180, 90));
    //     vTaskDelay(pdMS_TO_TICKS(1000));
    //     ESP_ERROR_CHECK(servo180_write_angle(&sv180, 0));
    //     ESP_ERROR_CHECK(servo360_write_angle(&sv360, -180));
    // }else{
    //     ESP_ERROR_CHECK(servo360_write_angle(&sv360, -60));
    //     ESP_ERROR_CHECK(servo180_write_angle(&sv180, 90));
    //     vTaskDelay(pdMS_TO_TICKS(1000));
    //     ESP_ERROR_CHECK(servo180_write_angle(&sv180, 0));
    //     ESP_ERROR_CHECK(servo360_write_angle(&sv360, 60));
    // }

    if(category == WASTE_RECYCLABLE){
        ESP_ERROR_CHECK(servo180_write_angle(&sv180_2, 90));
        vTaskDelay(pdMS_TO_TICKS(1000));
        ESP_ERROR_CHECK(servo180_write_angle(&sv180_2, 0));
    }else if(category == WASTE_COMPOSTABLE){
        ESP_ERROR_CHECK(servo180_write_angle(&sv180_1, 90));
        ESP_ERROR_CHECK(servo180_write_angle(&sv180_2, 90));
        vTaskDelay(pdMS_TO_TICKS(1000));
        ESP_ERROR_CHECK(servo180_write_angle(&sv180_2, 0));
        ESP_ERROR_CHECK(servo180_write_angle(&sv180_1, 0));
    }else{
        ESP_ERROR_CHECK(servo180_write_angle(&sv180_1, 180));
        ESP_ERROR_CHECK(servo180_write_angle(&sv180_2, 90));
        vTaskDelay(pdMS_TO_TICKS(1000));
        ESP_ERROR_CHECK(servo180_write_angle(&sv180_2, 0));
        ESP_ERROR_CHECK(servo180_write_angle(&sv180_1, 0));
    }
}