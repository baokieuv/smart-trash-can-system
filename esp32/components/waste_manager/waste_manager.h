#ifndef WASTE_MANAGER_H
#define WASTE_MANAGER_H

#include "esp_err.h"
#include "config.h"
#include "freertos/FreeRTOS.h"
#include "freertos/event_groups.h"

// ==================== Waste Statistics ====================
typedef struct {
    uint32_t total_count;           // Total waste throws
    uint32_t recyclable_count;      // Recyclable waste count
    uint32_t compostable_count;     // Compostable waste count
    uint32_t hazardous_count;       // Hazardous waste count
    float current_fill_level;       // Current fill level (%)
    bool is_full;                   // Is bin full?
} waste_stats_t;

// ==================== Classification Result ====================
typedef struct {
    waste_category_t category;
    float confidence;
    char description[64];
} classification_result_t;

/**
 * @brief Initialize waste management system
 * @return ESP_OK on success
 */
esp_err_t waste_manager_init(EventGroupHandle_t g_event_group);

/**
 * @brief Start waste management system
 * @return ESP_OK on success
 */
esp_err_t waste_manager_start(void);

/**
 * @brief Stop waste management system
 * @return ESP_OK on success
 */
esp_err_t waste_manager_stop(void);

/**
 * @brief Get current waste statistics
 * @param stats Pointer to store statistics
 * @return ESP_OK on success
 */
esp_err_t waste_manager_get_stats(waste_stats_t *stats);

/**
 * @brief Reset waste statistics
 * @return ESP_OK on success
 */
esp_err_t waste_manager_reset_stats(void);

/**
 * @brief Update bin fill level
 * @param distance_cm Current distance from ultrasonic sensor
 * @return ESP_OK on success
 */
esp_err_t waste_manager_update_fill_level(float distance_cm);

/**
 * @brief Record a waste throw event
 * @param category Waste category
 * @return ESP_OK on success
 */
esp_err_t waste_manager_record_waste(waste_category_t category);

/**
 * @brief Check if bin is full
 * @return true if full, false otherwise
 */
bool waste_manager_is_bin_full(void);

/**
 * @brief Get fill level percentage
 * @return Fill level (0-100%)
 */
float waste_manager_get_fill_level(void);

#endif // WASTE_MANAGER_H