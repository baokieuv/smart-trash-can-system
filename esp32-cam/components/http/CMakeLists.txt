idf_component_register(
    SRCS "http_client.c"
    INCLUDE_DIRS "."
    REQUIRES 
        esp_http_client 
        esp32-camera 
        json 
        waste_manager 
        common
        mbedtls
)