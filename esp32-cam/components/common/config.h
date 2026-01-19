#ifndef CONFIG_H
#define CONFIG_H

#include "driver/gpio.h"
#include "driver/ledc.h"

// ==================== NVS Configuration ====================
#define NVS_NAMESPACE           "smart_bin"
#define NVS_KEY_SSID            "ssid"
#define NVS_KEY_PASS            "password"
#define SSID_MAX_LEN            32
#define PASSWORD_MAX_LEN        64

#define NVS_KEY_RECYCLABLE_COUNT        "recyclable"
#define NVS_KEY_COMPOSTABLE_COUNT       "composable"
#define NVS_KEY_NON_RECYCLABLE_COUNT    "non_recyclable"

#define HTTP_SERVER_URL         "http://kvbhust.site/api/v1"  // AI Server
#define HTTP_TIMEOUT_MS         10000

#define TELEMETRY_INTERVAL_MS   5000  // Send telemetry every 1 minute
#define OTA_INTERVAL_MS         60000  // Send request OTA every 1 minute
#define HEARTBEAT_INTERVAL_MS   (1 * 60 * 1000) // 1 minutes

#define BTN_CONFIG_PIN          GPIO_NUM_0 

#define LED_STATUS_PIN          GPIO_NUM_14
#define BUZZER_PIN              GPIO_NUM_21

#define SERVO_180_GPIO          GPIO_NUM_1
#define SERVO_180_CHANNEL       LEDC_CHANNEL_0

#define SERVO_360_GPIO          GPIO_NUM_2
#define SERVO_360_CHANNEL       LEDC_CHANNEL_1

#define SERVO_180_MIN_US            500
#define SERVO_180_MAX_US            2400

#define SERVO_360_STOP_US           1500
#define SERVO_360_RANGE_US          350
#define SERVO_360_TIME_PER_DEGREE   8

#define ULTRASONIC1_ECHO_PIN        GPIO_NUM_45
#define ULTRASONIC1_TRIG_PIN        GPIO_NUM_47

#define ULTRASONIC_ECHO_SHARED_PIN  GPIO_NUM_39
#define ULTRASONIC2_TRIG_PIN        GPIO_NUM_40
// #define ULTRASONIC3_TRIG_PIN        GPIO_NUM_48
// #define ULTRASONIC4_TRIG_PIN        GPIO_NUM_39

#define DISTANCE_THRESHOLD_CM   5   // Waste detected if distance < 15cm
#define BIN_HEIGHT_CM           20   // Total bin height
#define BIN_FULL_THRESHOLD      80   // Alert when 80% full

#define CAM_PIN_PWDN            -1
#define CAM_PIN_RESET           -1
#define CAM_PIN_XCLK            15
#define CAM_PIN_SIOD            4
#define CAM_PIN_SIOC            5
#define CAM_PIN_D7              16
#define CAM_PIN_D6              17
#define CAM_PIN_D5              18
#define CAM_PIN_D4              12
#define CAM_PIN_D3              10
#define CAM_PIN_D2              8
#define CAM_PIN_D1              9
#define CAM_PIN_D0              11
#define CAM_PIN_VSYNC           6
#define CAM_PIN_HREF            7
#define CAM_PIN_PCLK            13

#define CAMERA_TASK_STACK_SIZE      (5 * 1024)
#define HTTP_TASK_STACK_SIZE        (4 * 1024)
#define SENSOR_TASK_STACK_SIZE      (4 * 1024)
#define HEARTBEAT_TASK_STACK_SIZE   (3 * 1024)
#define LED_STATUS_TASK_STACK_SIZE  (1 * 1024)

#define IMAGE_QUEUE_LENGTH          2
#define TRIGGER_QUEUE_LENGTH        5
#define RESULT_QUEUE_LENGTH         5

#define WIFI_CONNECTED_BIT      BIT0
#define WIFI_FAIL_BIT           BIT1
#define CONFIG_MODE_BIT         BIT2
#define RESET_MODE_BIT          BIT3
#define BIN_FULL_BIT            BIT4
#define DEVICE_ACTIVE_BIT       BIT5

#define SENSOR_CHECK_INTERVAL_MS    2000
#define DEBOUNCE_DELAY_MS           50
#define BUTTON_SHORT_PRESS_MS       300
#define BUTTON_LONG_PRESS_MS        3000
#define LED_INDICATION_DURATION_MS  3000
#define CAMERA_WARMUP_SHOTS         3
#define CAMERA_SHOT_DELAY_MS        100

typedef enum {
    WASTE_RECYCLABLE = 0,   // Inorganic - recyclable
    WASTE_COMPOSTABLE,      // Organic - compostable
    WASTE_NON_RECYCLABLE    // Hazardous - non-recyclable
} waste_category_t;

#endif // CONFIG_H