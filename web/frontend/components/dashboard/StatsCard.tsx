import React from 'react';
import { Trash2, Wifi, WifiOff } from 'lucide-react';
import { Device } from '@/types';

interface StatsCardsProps {
  devices: Device[];
}

export default function StatsCards({ devices }: StatsCardsProps) {
  const onlineDevices = devices.filter(d => d.status === 'ONLINE').length;
  const offlineDevices = devices.filter(d => d.status === 'OFFLINE').length;

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 md:gap-6 mb-6">
      <div className="bg-linear-to-br from-blue-500 to-blue-600 rounded-2xl p-6 text-white shadow-lg">
        <div className="flex items-center justify-between mb-2">
          <div className="text-blue-100 text-sm font-medium">Total Devices</div>
          <Trash2 size={20} className="text-blue-200" />
        </div>
        <div className="text-4xl font-bold">{devices.length}</div>
        <div className="text-blue-100 text-xs mt-2">All systems tracked</div>
      </div>

      <div className="bg-linear-to-br from-green-500 to-green-600 rounded-2xl p-6 text-white shadow-lg">
        <div className="flex items-center justify-between mb-2">
          <div className="text-green-100 text-sm font-medium">Online Systems</div>
          <Wifi size={20} className="text-green-200" />
        </div>
        <div className="text-4xl font-bold">{onlineDevices}</div>
        <div className="text-green-100 text-xs mt-2">Active & Connected</div>
      </div>

      <div className="bg-linear-to-br from-slate-500 to-slate-600 rounded-2xl p-6 text-white shadow-lg">
        <div className="flex items-center justify-between mb-2">
          <div className="text-slate-100 text-sm font-medium">Offline / Error</div>
          <WifiOff size={20} className="text-slate-200" />
        </div>
        <div className="text-4xl font-bold">{offlineDevices}</div>
        <div className="text-slate-100 text-xs mt-2">Needs attention</div>
      </div>
    </div>
  );
}