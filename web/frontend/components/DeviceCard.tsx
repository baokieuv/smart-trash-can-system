import { Trash2, Wifi, WifiOff, MoreVertical, Zap } from 'lucide-react';
import type { Device } from '@/type/device';

interface DeviceCardProps {
  device: Device;
  onClick: (device: Device) => void;
}

export default function DeviceCard({ device, onClick }: DeviceCardProps) {
  const isOnline = device.isOnline;

  return (
    <div
      onClick={() => onClick(device)}
      className={`
        relative group cursor-pointer rounded-[1.75rem] p-6 transition-all duration-500
        border-2 bg-white/70 backdrop-blur-sm overflow-hidden
        ${isOnline 
          ? 'border-slate-100 shadow-[0_8px_30px_rgb(0,0,0,0.06)] hover:shadow-[0_20px_50px_rgb(59,130,246,0.15)] hover:border-blue-200 hover:-translate-y-2 hover:scale-[1.02]' 
          : 'border-slate-100 opacity-70 hover:opacity-100 hover:shadow-lg'
        }
      `}
    >
      {/* Background Gradient Effect */}
      <div className={`absolute inset-0 transition-opacity duration-500 ${isOnline ? 'opacity-0 group-hover:opacity-100' : 'opacity-0'}`}>
        <div className="absolute top-0 right-0 w-40 h-40 bg-blue-400/10 rounded-full blur-3xl -mr-10 -mt-10"></div>
        <div className="absolute bottom-0 left-0 w-32 h-32 bg-purple-400/10 rounded-full blur-2xl -ml-8 -mb-8"></div>
      </div>

      {/* Content */}
      <div className="relative z-10">
        {/* Top Action Button */}
        <button 
          onClick={(e) => e.stopPropagation()}
          className="absolute top-0 right-0 text-slate-300 hover:text-slate-600 hover:bg-slate-50 p-2 rounded-lg transition-all"
        >
          <MoreVertical className="w-4 h-4" />
        </button>

        {/* Icon Container with Animated Border */}
        <div className="relative w-16 h-16 mb-5">
          {isOnline && (
            <div className="absolute inset-0 bg-gradient-to-br from-blue-400 to-blue-600 rounded-2xl blur-md opacity-30 group-hover:opacity-50 transition-opacity"></div>
          )}
          <div className={`
            relative w-16 h-16 rounded-2xl flex items-center justify-center transition-all duration-500
            ${isOnline 
              ? 'bg-gradient-to-br from-blue-50 to-blue-100 text-blue-600 group-hover:from-blue-600 group-hover:to-blue-700 group-hover:text-white group-hover:scale-110 group-hover:rotate-3' 
              : 'bg-slate-100 text-slate-400'
            }
          `}>
            <Trash2 className="w-7 h-7" />
          </div>
        </div>

        {/* Device Info */}
        <div className="mb-5">
          <h3 className="text-lg font-bold text-slate-800 leading-tight mb-2 group-hover:text-blue-600 transition-colors">
            {device.name}
          </h3>
          <p className="text-xs font-mono text-slate-400 truncate bg-slate-50 px-2 py-1 rounded-md inline-block">
            {device.macAddress}
          </p>
        </div>

        {/* Footer Status */}
        <div className="flex items-center justify-between pt-4 border-t border-slate-100">
          <div className="flex items-center gap-2">
            <span className={`text-xs font-bold uppercase tracking-wider ${isOnline ? 'text-emerald-600' : 'text-slate-400'}`}>
              {isOnline ? 'Online' : 'Offline'}
            </span>
            {isOnline && (
              <Zap className="w-3.5 h-3.5 text-yellow-500 fill-yellow-500 animate-pulse" />
            )}
          </div>
          
          {isOnline ? (
            <div className="flex items-center gap-2">
              <span className="relative flex h-2.5 w-2.5">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-2.5 w-2.5 bg-emerald-500 shadow-sm shadow-emerald-300"></span>
              </span>
              <span className="text-xs text-emerald-600 font-semibold">Active</span>
            </div>
          ) : (
            <div className="flex items-center gap-1.5">
              <WifiOff className="w-4 h-4 text-slate-300" />
              <span className="text-xs text-slate-400 font-medium">Disconnected</span>
            </div>
          )}
        </div>
      </div>

      {/* Shine Effect on Hover */}
      {isOnline && (
        <div className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500 pointer-events-none">
          <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent -translate-x-full group-hover:translate-x-full transition-transform duration-1000"></div>
        </div>
      )}
    </div>
  );
}