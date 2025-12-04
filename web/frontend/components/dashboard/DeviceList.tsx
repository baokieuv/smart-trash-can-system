import React from 'react';
import { Trash2, Wifi, WifiOff } from 'lucide-react';
import { Device } from '@/types';
import { getStatusBg, getStatusColor } from '@/lib/utils';

interface DeviceListProps {
  devices: Device[];
  onSelectDevice: (device: Device) => void;
}

export default function DeviceList({ devices, onSelectDevice }: DeviceListProps) {
  return (
    <div className="bg-white rounded-2xl shadow-lg p-6">
      <h2 className="text-xl font-bold text-slate-800 mb-4 flex items-center gap-2">
        <Trash2 size={20} />
        All Devices
      </h2>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {devices.map(device => (
          <div
            key={device.id}
            onClick={() => onSelectDevice(device)}
            className="bg-slate-50 rounded-xl p-4 border-2 border-slate-200 hover:border-blue-400 hover:shadow-md transition-all cursor-pointer group"
          >
            <div className="flex items-start justify-between mb-3">
              <div className="flex items-center gap-3">
                <div className={`p-3 rounded-lg ${device.status === 'online' ? 'bg-blue-100' : 'bg-slate-200'} group-hover:scale-110 transition-transform`}>
                  <Trash2 size={20} className={device.status === 'online' ? 'text-blue-600' : 'text-slate-400'} />
                </div>
                <div>
                  <h3 className="font-bold text-slate-800 group-hover:text-blue-600 transition-colors">
                    {device.name}
                  </h3>
                  <p className="text-xs text-slate-500 font-mono mt-0.5">{device.mac}</p>
                </div>
              </div>
            </div>
            
            <div className="flex items-center justify-between">
              <div className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold ${getStatusBg(device.status)}`}>
                {device.status === 'online' ? (
                  <Wifi size={12} className="text-green-600" />
                ) : (
                  <WifiOff size={12} className="text-gray-400" />
                )}
                <span className={getStatusColor(device.status)}>
                  {device.status.toUpperCase()}
                </span>
              </div>
              {device.status === 'online' && device.fillLevel >= 80 && (
                <div className="flex items-center gap-1 text-xs text-amber-600 font-semibold">
                  ⚠️ {device.fillLevel}%
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}