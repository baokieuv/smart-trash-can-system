import React from 'react';
import { ArrowLeft, Trash2, Wifi, WifiOff, Battery, TrendingUp } from 'lucide-react';
import { Device } from '@/types';
import { getStatusBg, getStatusColor, getFillLevelColor, getBatteryColor } from '@/lib/utils';

interface DeviceDetailProps {
  device: Device;
  onBack: () => void;
}

export default function DeviceDetail({ device, onBack }: DeviceDetailProps) {
  return (
    <div className="min-h-screen bg-linear-to-br from-slate-50 to-slate-100 p-4 md:p-8">
      <div className="max-w-4xl mx-auto">
        {/* Back Button */}
        <button
          onClick={onBack}
          className="flex items-center gap-2 mb-6 text-slate-700 hover:text-slate-900 transition-colors"
        >
          <ArrowLeft size={20} />
          <span className="font-medium">Back to Dashboard</span>
        </button>

        {/* Device Detail Card */}
        <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
          {/* Header */}
          <div className="bg-linear-to-r from-blue-600 to-blue-700 p-6 text-white">
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-4">
                <div className="bg-white/20 p-4 rounded-xl backdrop-blur-sm">
                  <Trash2 size={32} />
                </div>
                <div>
                  <h1 className="text-2xl md:text-3xl font-bold">{device.name}</h1>
                  <p className="text-blue-100 mt-1 font-mono text-sm">{device.mac}</p>
                </div>
              </div>
              <div className={`flex items-center gap-2 px-4 py-2 rounded-full ${getStatusBg(device.status)}`}>
                {device.status === 'online' ? (
                  <Wifi size={16} className="text-green-600" />
                ) : (
                  <WifiOff size={16} className="text-gray-400" />
                )}
                <span className={`font-semibold text-sm ${getStatusColor(device.status)}`}>
                  {device.status.toUpperCase()}
                </span>
              </div>
            </div>
          </div>

          {/* Stats Grid */}
          <div className="p-6 grid grid-cols-2 md:grid-cols-4 gap-4">
            {/* Fill Level */}
            <div className="bg-slate-50 rounded-xl p-4">
              <div className="text-slate-600 text-sm mb-2">Fill Level</div>
              <div className="flex items-end gap-2">
                <div className="text-3xl font-bold text-slate-800">{device.fillLevel}%</div>
              </div>
              <div className="mt-3 bg-slate-200 rounded-full h-2 overflow-hidden">
                <div 
                  className={`h-full ${getFillLevelColor(device.fillLevel)} transition-all duration-500`}
                  style={{ width: `${device.fillLevel}%` }}
                />
              </div>
            </div>

            {/* Battery */}
            <div className="bg-slate-50 rounded-xl p-4">
              <div className="text-slate-600 text-sm mb-2">Battery</div>
              <div className="flex items-center gap-2">
                <Battery size={24} className={getBatteryColor(device.battery)} />
                <div className={`text-3xl font-bold ${getBatteryColor(device.battery)}`}>
                  {device.battery}%
                </div>
              </div>
            </div>

            {/* Total Waste */}
            <div className="bg-slate-50 rounded-xl p-4">
              <div className="text-slate-600 text-sm mb-2">Total Waste</div>
              <div className="text-3xl font-bold text-slate-800">{device.total}</div>
              <div className="text-xs text-slate-500 mt-1">items</div>
            </div>

            {/* Status Indicator */}
            <div className="bg-slate-50 rounded-xl p-4">
              <div className="text-slate-600 text-sm mb-2">Status</div>
              <div className={`text-2xl font-bold ${device.status === 'online' ? 'text-green-600' : 'text-gray-400'}`}>
                {device.status === 'online' ? 'Active' : 'Inactive'}
              </div>
              <div className="text-xs text-slate-500 mt-1">
                {device.status === 'online' ? 'Connected' : 'Disconnected'}
              </div>
            </div>
          </div>

          {/* Waste Breakdown */}
          <div className="p-6 border-t border-slate-200">
            <h2 className="text-xl font-bold text-slate-800 mb-4 flex items-center gap-2">
              <TrendingUp size={20} />
              Waste Breakdown
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="bg-green-50 rounded-xl p-4 border border-green-200">
                <div className="text-green-700 font-semibold mb-1">♻️ Recycled</div>
                <div className="text-3xl font-bold text-green-800">{device.recycled}</div>
                <div className="text-xs text-green-600 mt-1">items</div>
              </div>
              <div className="bg-gray-50 rounded-xl p-4 border border-gray-200">
                <div className="text-gray-700 font-semibold mb-1">🗑️ Non-Recycled</div>
                <div className="text-3xl font-bold text-gray-800">{device.nonRecycled}</div>
                <div className="text-xs text-gray-600 mt-1">items</div>
              </div>
              <div className="bg-amber-50 rounded-xl p-4 border border-amber-200">
                <div className="text-amber-700 font-semibold mb-1">🌱 Composable</div>
                <div className="text-3xl font-bold text-amber-800">{device.composable}</div>
                <div className="text-xs text-amber-600 mt-1">items</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}