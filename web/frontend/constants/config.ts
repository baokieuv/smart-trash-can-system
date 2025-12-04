export const API_CONFIG = {
  BASE_URL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8888',
  ENDPOINTS: {
    DEVICES: '/api/v1/devices',
    DEVICE_BY_ID: (id: string) => `/api/v1/devices/${id}`,
  },
  TIMEOUT: 10000,
} as const;

export const APP_CONFIG = {
  NAME: 'Smart Bin Dashboard',
  DESCRIPTION: 'Monitor and manage your smart devices',
  REFRESH_INTERVAL: 30000, // 30 seconds
} as const;