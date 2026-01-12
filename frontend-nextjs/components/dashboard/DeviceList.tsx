import React, { useState } from 'react';
import { Trash2, Wifi, WifiOff, Edit2, Trash, X, Check } from 'lucide-react';
import { Device } from '@/types';
import { getStatusBg, getStatusColor } from '@/lib/utils';
import { updateDevice, deleteDevice } from '@/services/apiService';

interface DeviceListProps {
  devices: Device[];
  onSelectDevice: (device: Device) => void;
  onDeviceUpdated: () => void;
}

export default function DeviceList({ devices, onSelectDevice, onDeviceUpdated }: DeviceListProps) {
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editName, setEditName] = useState('');
  const [isUpdating, setIsUpdating] = useState(false);
  
  const handleStartEdit = (device: Device, e: React.MouseEvent) => {
    e.stopPropagation();
    setEditingId(device.id);
    setEditName(device.name);
  };

  const handleCancelEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    setEditingId(null);
    setEditName('');
  };

  const handleSaveEdit = async (deviceId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!editName.trim() || isUpdating) return;

    setIsUpdating(true);
    const success = await updateDevice(deviceId, editName.trim());
    setIsUpdating(false);

    if (success) {
      setEditingId(null);
      setEditName('');
      onDeviceUpdated();
    } else {
      alert('Failed to update device name');
    }
  };

  const handleDelete = async (deviceId: string, deviceName: string, e: React.MouseEvent) => {
    e.stopPropagation();
    
    if (!confirm(`Are you sure you want to delete "${deviceName}"?`)) {
      return;
    }

    setIsUpdating(true);
    const success = await deleteDevice(deviceId);
    setIsUpdating(false);

    if (success) {
      onDeviceUpdated();
    } else {
      alert('Failed to delete device');
    }
  };

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
            onClick={() => !editingId && onSelectDevice(device)}
            className="bg-slate-50 rounded-xl p-4 border-2 border-slate-200 hover:border-blue-400 hover:shadow-md transition-all cursor-pointer group relative"
          >
            {/* Action Buttons */}
            <div className="absolute top-3 right-3 flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
              {editingId === device.id ? (
                <>
                  <button
                    onClick={(e) => handleSaveEdit(device.id, e)}
                    disabled={isUpdating}
                    className="p-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors disabled:opacity-50"
                    title="Save"
                  >
                    <Check size={16} />
                  </button>
                  <button
                    onClick={handleCancelEdit}
                    disabled={isUpdating}
                    className="p-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors disabled:opacity-50"
                    title="Cancel"
                  >
                    <X size={16} />
                  </button>
                </>
              ) : (
                <>
                  <button
                    onClick={(e) => handleStartEdit(device, e)}
                    className="p-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
                    title="Edit name"
                  >
                    <Edit2 size={16} />
                  </button>
                  <button
                    onClick={(e) => handleDelete(device.id, device.name, e)}
                    disabled={isUpdating}
                    className="p-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50"
                    title="Delete device"
                  >
                    <Trash size={16} />
                  </button>
                </>
              )}
            </div>

            <div className="flex items-start justify-between mb-3 pr-20">
              <div className="flex items-center gap-3 flex-1">
                <div className={`p-3 rounded-lg ${device.status === 'ONLINE' ? 'bg-blue-100' : 'bg-slate-200'} group-hover:scale-110 transition-transform`}>
                  <Trash2 size={20} className={device.status === 'ONLINE' ? 'text-blue-600' : 'text-slate-400'} />
                </div>
                <div className="flex-1 min-w-0">
                  {editingId === device.id ? (
                    <input
                      type="text"
                      value={editName}
                      onChange={(e) => setEditName(e.target.value)}
                      onClick={(e) => e.stopPropagation()}
                      className="font-bold text-slate-800 border-2 border-blue-400 rounded px-2 py-1 w-full focus:outline-none focus:border-blue-600"
                      autoFocus
                      disabled={isUpdating}
                    />
                  ) : (
                    <h3 className="font-bold text-slate-800 group-hover:text-blue-600 transition-colors truncate">
                      {device.name}
                    </h3>
                  )}
                  <p className="text-xs text-slate-500 font-mono mt-0.5 truncate">{device.mac}</p>
                </div>
              </div>
            </div>
            
            <div className="flex items-center justify-between">
              <div className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold ${getStatusBg(device.status)}`}>
                {device.status === 'ONLINE' ? (
                  <Wifi size={12} className="text-green-600" />
                ) : (
                  <WifiOff size={12} className="text-gray-400" />
                )}
                <span className={getStatusColor(device.status)}>
                  {device.status.toUpperCase()}
                </span>
              </div>
              {device.status === 'ONLINE' && device.fillLevel >= 80 && (
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