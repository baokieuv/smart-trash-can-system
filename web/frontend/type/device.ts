export interface Device {
  id: string;
  name: string;
  macAddress: string;
  // imageUrl?: string; // Đã xóa thuộc tính này
  isOnline: boolean;
}

export interface DeviceStats {
  total: number;
  online: number;
  offline: number;
}

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
}