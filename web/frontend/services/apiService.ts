import { Device, ActivityLog } from '@/types';

// Hàm lấy danh sách thiết bị
export const fetchDevices = async (): Promise<Device[]> => {
  try {
  
    const res = await fetch('/api/devices', { cache: 'no-store' });
    if (!res.ok) throw new Error('Failed to fetch devices');
    return res.json();

  } catch (error) {
    console.error("Error fetching devices:", error);
    return []; // Trả về mảng rỗng nếu lỗi để app không crash
  }
};

// Hàm lấy log hoạt động
export const fetchLogs = async (): Promise<ActivityLog[]> => {
  try {
    const res = await fetch('/api/logs', { cache: 'no-store' });
    if (!res.ok) throw new Error('Failed to fetch logs');
    return res.json();
  } catch (error) {
    console.error("Error fetching logs:", error);
    return [];
  }
}; 

export const updateDevice = async (deviceId: string, newName: string): Promise<boolean> => {
    try{
        const res = await fetch(`/api/devices/${deviceId}`, {
            method: "PUT",
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ name: newName })
        });

        if (!res.ok) throw new Error('Failed to update device name');
        return true;
    }catch(error){
        console.error("Error updating device: ", error);
        return false;
    }
}

export const deleteDevice = async (deviceId: string): Promise<boolean> => {
    try{
        const res = await fetch(`/api/devices/${deviceId}`, {
            method: "DELETE"
        });

        if (!res.ok) throw new Error('Failed to delete device');
        return true;
    }catch(error){
        console.error("Error deleting device: ", error);
        return false;
    }
}