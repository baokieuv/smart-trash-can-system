import { Device, ActivityLog } from '@/types';

// Hàm lấy danh sách thiết bị
export const fetchDevices = async (): Promise<Device[]> => {
  try {
    // GỌI API LOCAL (Next.js API Routes)
    const res = await fetch('/api/devices', { cache: 'no-store' });
    if (!res.ok) throw new Error('Failed to fetch devices');
    return res.json();

    /* // --- DÀNH CHO TƯƠNG LAI (BACKEND SPRING BOOT/IOT) ---
    // Khi bạn có backend thật, hãy mở comment này và sửa URL:
    
    const res = await fetch('http://localhost:8080/api/v1/smart-bins', {
       method: 'GET',
       headers: {
         'Authorization': 'Bearer <token_neu_co>',
         'Content-Type': 'application/json'
       }
    });
    return res.json();
    */
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