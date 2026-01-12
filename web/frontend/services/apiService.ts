import { Device, ActivityLog } from '@/types';

let isRefreshing = false;
let failedQueue: Array<{ resolve: Function; reject: Function }> = [];

const processQueue = (error: any = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve();
    }
  });
  failedQueue = [];
};

// Helper to refresh token
const refreshToken = async (): Promise<boolean> => {
  if (isRefreshing) {
    return new Promise((resolve, reject) => {
      failedQueue.push({ resolve, reject });
    });
  }

  isRefreshing = true;
  const refreshTokenStr = localStorage.getItem('refreshToken');

  if (!refreshTokenStr) {
    isRefreshing = false;
    return false;
  }

  try {
    const response = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: refreshTokenStr }),
    });

    if (!response.ok) {
      throw new Error('Refresh failed');
    }

    const data = await response.json();

    // Save new tokens
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('user', JSON.stringify(data.user));
    
    const expiryTime = Date.now() + (data.expiresIn * 1000) - 60000;
    localStorage.setItem('tokenExpiry', expiryTime.toString());

    processQueue();
    isRefreshing = false;
    return true;
  } catch (error) {
    processQueue(error);
    isRefreshing = false;
    
    // Clear tokens and redirect
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    localStorage.removeItem('tokenExpiry');
    window.location.href = '/login';
    
    return false;
  }
};

// Helper function to get auth headers
const getAuthHeaders = () => {
  const token = localStorage.getItem('accessToken');
  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };
};

// Enhanced fetch with auto-retry on 401
const fetchWithAuth = async (url: string, options: RequestInit = {}) => {
  const headers = getAuthHeaders();
  
  let response = await fetch(url, {
    ...options,
    headers: { ...headers, ...options.headers },
  });

  // If unauthorized, try to refresh token and retry
  if (response.status === 401 && !isRefreshing) {
    const refreshed = await refreshToken();
    
    if (refreshed) {
      // Retry with new token
      const newHeaders = getAuthHeaders();
      response = await fetch(url, {
        ...options,
        headers: { ...newHeaders, ...options.headers },
      });
    }
  }

  return response;
};

// Fetch devices with authentication and auto-refresh
export const fetchDevices = async (): Promise<Device[]> => {
  try {
    const res = await fetchWithAuth('/api/devices', { cache: 'no-store' });

    if (!res.ok) throw new Error('Failed to fetch devices');
    return res.json();
  } catch (error) {
    console.error("Error fetching devices:", error);
    return [];
  }
};

// Fetch logs with authentication and auto-refresh
export const fetchLogs = async (): Promise<ActivityLog[]> => {
  try {
    const res = await fetchWithAuth('/api/logs', { cache: 'no-store' });

    if (!res.ok) throw new Error('Failed to fetch logs');
    return res.json();
  } catch (error) {
    console.error("Error fetching logs:", error);
    return [];
  }
};

// Update device with authentication and auto-refresh
export const updateDevice = async (deviceId: string, newName: string): Promise<boolean> => {
  try {
    const res = await fetchWithAuth(`/api/devices/${deviceId}`, {
      method: "PUT",
      body: JSON.stringify({ name: newName })
    });

    if (!res.ok) throw new Error('Failed to update device name');
    return true;
  } catch (error) {
    console.error("Error updating device:", error);
    return false;
  }
};

// Delete device with authentication and auto-refresh
export const deleteDevice = async (deviceId: string): Promise<boolean> => {
  try {
    const res = await fetchWithAuth(`/api/devices/${deviceId}`, {
      method: "DELETE"
    });

    if (!res.ok) throw new Error('Failed to delete device');
    return true;
  } catch (error) {
    console.error("Error deleting device:", error);
    return false;
  }
};