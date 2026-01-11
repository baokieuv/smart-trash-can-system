import { Device, ActivityLog } from '@/types';

// Helper function to get auth headers
const getAuthHeaders = () => {
  const token = localStorage.getItem('accessToken');
  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };
};

// Helper function to handle auth errors
const handleAuthError = (error: any) => {
  if (error.message?.includes('401') || error.message?.includes('Unauthorized')) {
    // Clear tokens and redirect to login
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    window.location.href = '/login';
  }
};

// Fetch devices with authentication
export const fetchDevices = async (): Promise<Device[]> => {
  try {
    const res = await fetch('/api/devices', {
      cache: 'no-store',
      headers: getAuthHeaders()
    });

    if (res.status === 401) {
      handleAuthError(new Error('Unauthorized'));
      return [];
    }

    if (!res.ok) throw new Error('Failed to fetch devices');
    return res.json();
  } catch (error) {
    console.error("Error fetching devices:", error);
    handleAuthError(error);
    return [];
  }
};

// Fetch logs with authentication
export const fetchLogs = async (): Promise<ActivityLog[]> => {
  try {
    const res = await fetch('/api/logs', {
      cache: 'no-store',
      headers: getAuthHeaders()
    });

    if (res.status === 401) {
      handleAuthError(new Error('Unauthorized'));
      return [];
    }

    if (!res.ok) throw new Error('Failed to fetch logs');
    return res.json();
  } catch (error) {
    console.error("Error fetching logs:", error);
    handleAuthError(error);
    return [];
  }
};

// Update device with authentication
export const updateDevice = async (deviceId: string, newName: string): Promise<boolean> => {
  try {
    const res = await fetch(`/api/devices/${deviceId}`, {
      method: "PUT",
      headers: getAuthHeaders(),
      body: JSON.stringify({ name: newName })
    });

    if (res.status === 401) {
      handleAuthError(new Error('Unauthorized'));
      return false;
    }

    if (!res.ok) throw new Error('Failed to update device name');
    return true;
  } catch (error) {
    console.error("Error updating device:", error);
    handleAuthError(error);
    return false;
  }
};

// Delete device with authentication
export const deleteDevice = async (deviceId: string): Promise<boolean> => {
  try {
    const res = await fetch(`/api/devices/${deviceId}`, {
      method: "DELETE",
      headers: getAuthHeaders()
    });

    if (res.status === 401) {
      handleAuthError(new Error('Unauthorized'));
      return false;
    }

    if (!res.ok) throw new Error('Failed to delete device');
    return true;
  } catch (error) {
    console.error("Error deleting device:", error);
    handleAuthError(error);
    return false;
  }
};