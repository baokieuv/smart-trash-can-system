"use client"
import React, { useState, useEffect } from 'react';
import { Trash2 } from 'lucide-react';
import { Device, ActivityLog } from '@/types';
import { fetchDevices, fetchLogs } from '@/services/apiService';

import StatsCards from '@/components/dashboard/StatsCard';
import DeviceList from '@/components/dashboard/DeviceList';
import ActivityFeed from '@/components/dashboard/ActivityFeed';
import DeviceDetail from '@/components/dashboard/DeviceDetail';

export default function DashboardPage() {
  const [selectedDevice, setSelectedDevice] = useState<Device | null>(null);
  const [devices, setDevices] = useState<Device[]>([]);
  const [logs, setLogs] = useState<ActivityLog[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      const [devicesData, logsData] = await Promise.all([
        fetchDevices(),
        fetchLogs()
      ]);
      setDevices(devicesData);
      setLogs(logsData);

      if (selectedDevice) {
        const updatedDevice = devicesData.find(d => d.id === selectedDevice.id);
        if (updatedDevice) {
          setSelectedDevice(updatedDevice);
        } else {
          setSelectedDevice(null);
        }
      }
    } catch (error) {
      console.error("Failed to load dashboard data");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 10000);
    return () => clearInterval(interval);
  }, []);

  const handleDeviceUpdated = () => {
    loadData();
  };

  if (selectedDevice) {
    return (
      <DeviceDetail 
        device={selectedDevice} 
        onBack={() => setSelectedDevice(null)} 
        onDeviceUpdated={handleDeviceUpdated}
      />
    );
  }

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center text-slate-500">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p>Loading Smart Bin IoT System...</p>
        </div>
      </div>
    );
  }

  return (
    <>
      {/* Header */}
      <div className="bg-white border-b border-slate-200 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 md:px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="bg-blue-600 p-2 rounded-lg">
                <Trash2 className="text-white" size={24} />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-slate-800">Smart Bin IoT</h1>
                <p className="text-sm text-slate-600">Real-time Monitoring System</p>
              </div>
            </div>
            <div className="text-xs text-slate-500 hidden md:block">
              Updated: <span className="text-blue-600 font-semibold">Live</span>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 md:px-8 py-6">
        <StatsCards devices={devices} />

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2">
            <DeviceList 
              devices={devices} 
              onSelectDevice={setSelectedDevice}
              onDeviceUpdated={handleDeviceUpdated} 
            />
          </div>
          
          <div className="lg:col-span-1">
            <ActivityFeed logs={logs} />
          </div>
        </div>
      </div>
    </>
  );
}