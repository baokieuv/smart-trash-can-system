idf_component_register(
    SRCS "main.c"
    INCLUDE_DIRS "."
    REQUIRES 
        # Thư viện hệ thống
        nvs_flash 
        driver 
        esp_event 
        esp_netif
        
        # Các Component của bạn (Tên folder trong components/)
        bluetooth
        camera
        gpio
        http
        ota
        sensors
        storage
        waste_manager
        wifi
        common
)