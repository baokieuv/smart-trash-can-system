idf_component_register(
    SRCS "bluetooth_manager.c"
    INCLUDE_DIRS "."

    REQUIRES 
        bt 
        nvs_flash 
        storage 
        common
        json
)
