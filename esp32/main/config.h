#ifndef CONFIG_H
#define CONFIG_H

#include "driver/gpio.h"

// ==================== WiFi Configuration ====================
#define MAXIMUM_RETRY           5
#define WIFI_CONNECT_TIMEOUT_MS 15000
#define AP_SSID                 "SmartBin_Config"
#define AP_PASSWORD             ""  // Open AP
#define AP_MAX_CONN             3

// ==================== NVS Configuration ====================
#define NVS_NAMESPACE           "smart_bin"
#define NVS_KEY_SSID            "ssid"
#define NVS_KEY_PASS            "password"
#define NVS_KEY_WASTE_COUNT     "waste_count"
#define NVS_KEY_BIN_HEIGHT      "bin_height"
#define SSID_MAX_LEN            32
#define PASSWORD_MAX_LEN        64

// ==================== HTTP Configuration ====================
#define HTTP_SERVER_URL         "http://192.168.1.100:5000/classify"  // AI Server
#define HTTP_TIMEOUT_MS         10000

// ==================== MQTT Configuration ====================
#define MQTT_BROKER             "mqtt://demo.thingsboard.io:1883"
#define MQTT_TELEMETRY_TOPIC    "v1/devices/me/telemetry"
#define MQTT_ACCESS_TOKEN       "KOB9rV5CSP0GXJsYsDya"
#define MQTT_RECONNECT_DELAY_MS 5000
#define TELEMETRY_INTERVAL_MS   5000  // Send telemetry every 1 minute
#define OTA_INTERVAL_MS         60000  // Send request OTA every 1 minute
#define HEARTBEAT_INTERVAL_MS   (20 * 60 * 1000) // 20 minutes

// ==================== GPIO Pins ====================
// Button
#define BTN_CONFIG_PIN          GPIO_NUM_10   // Boot button for config mode

// PIR Sensor
#define PIR_SENSOR_PIN          GPIO_NUM_16

// Ultrasonic Sensor
#define ULTRASONIC_TRIG_PIN     GPIO_NUM_15
#define ULTRASONIC_ECHO_PIN     GPIO_NUM_5

// LED Indicators (3 waste types)
#define LED_RECYCLABLE_PIN      GPIO_NUM_6  // Recyclable waste (blue)
#define LED_COMPOSTABLE_PIN     GPIO_NUM_7  // Compostable waste (green)
#define LED_HAZARDOUS_PIN       GPIO_NUM_4  // Hazardous waste (red)
#define LED_STATUS_PIN          GPIO_NUM_19  // System status LED

// Buzzer
#define BUZZER_PIN              GPIO_NUM_2

// ==================== Sensor Configuration ====================
#define DISTANCE_THRESHOLD_CM   15   // Waste detected if distance < 15cm
#define BIN_HEIGHT_CM           50   // Total bin height
#define BIN_FULL_THRESHOLD      80   // Alert when 80% full

// ==================== Camera Configuration ====================
#define CAM_PIN_PWDN            -1
#define CAM_PIN_RESET           9
#define CAM_PIN_XCLK            40
#define CAM_PIN_SIOD            17
#define CAM_PIN_SIOC            18
#define CAM_PIN_D7              39
#define CAM_PIN_D6              41
#define CAM_PIN_D5              42
#define CAM_PIN_D4              12
#define CAM_PIN_D3              3
#define CAM_PIN_D2              14
#define CAM_PIN_D1              47
#define CAM_PIN_D0              13
#define CAM_PIN_VSYNC           21
#define CAM_PIN_HREF            38
#define CAM_PIN_PCLK            11

// ==================== Task Configuration ====================
#define CAMERA_TASK_STACK_SIZE      (4 * 1024)
#define HTTP_TASK_STACK_SIZE        (4 * 1024)
#define SENSOR_TASK_STACK_SIZE      (3 * 1024)
#define HEARTBEAT_TASK_STACK_SIZE   (3 * 1024)

#define IMAGE_QUEUE_LENGTH          2
#define TRIGGER_QUEUE_LENGTH        5
#define RESULT_QUEUE_LENGTH         5

// ==================== Event Group Bits ====================
#define WIFI_CONNECTED_BIT      BIT0
#define WIFI_FAIL_BIT           BIT1
#define MQTT_CONNECTED_BIT      BIT2
#define CONFIG_MODE_BIT         BIT3
#define EXIT_CONFIG_MODE_BIT    BIT4
#define BIN_FULL_BIT            BIT5

// ==================== Timing Configuration ====================
#define SENSOR_CHECK_INTERVAL_MS    200
#define DEBOUNCE_DELAY_MS           50
#define BUTTON_LONG_PRESS_MS        3000
#define LED_INDICATION_DURATION_MS  3000
#define CAMERA_WARMUP_SHOTS         3
#define CAMERA_SHOT_DELAY_MS        100

// ==================== Waste Categories ====================
typedef enum {
    WASTE_RECYCLABLE = 0,   // Inorganic - recyclable
    WASTE_COMPOSTABLE,      // Organic - compostable
    WASTE_HAZARDOUS,        // Hazardous - non-recyclable
    WASTE_UNKNOWN
} waste_category_t;

#endif // CONFIG_H