idf_component_register(
    SRCS "waste_manager.c"
    INCLUDE_DIRS "."
    REQUIRES nvs_flash storage common
)