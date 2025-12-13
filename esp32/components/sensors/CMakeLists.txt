idf_component_register(
    SRCS "servo.c" "pir_sensor.c" "ultrasonic_sensor.c" # Liệt kê cả 2 file sensor
    INCLUDE_DIRS "."
    REQUIRES driver esp_timer common
)