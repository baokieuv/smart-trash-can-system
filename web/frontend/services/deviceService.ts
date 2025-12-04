import { API_CONFIG } from "@/constants/config";
import type { Device, ApiResponse } from "@/type/device"

export const deviceService = {
    async fetchDevices(): Promise<ApiResponse<Device[]>> {
        try{
            const response = await fetch(
                `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.DEVICES}`,
                {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                }
            );

            if(!response.ok){
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data: Device[] = await response.json();
            return { success: true, data };
        }catch(error){
            console.error('Error fetching devices: ', error);
            return {
                success: false,
                error: error instanceof Error ? error.message : 'Unknown error'
            };
        }
    },

    async fetchDeviceById(id: string): Promise<ApiResponse<Device>> {
        try{
             const response = await fetch(
                `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.DEVICE_BY_ID(id)}`,
                {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const data: Device = await response.json();
            return { success: true, data };
        }catch(error){
            console.error('Error fetching device:', error);
            return { 
                success: false, 
                error: error instanceof Error ? error.message : 'Unknown error' 
            };
        }
    },
};