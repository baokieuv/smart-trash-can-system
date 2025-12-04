import { Trash2, X, BarChart3, Signal, Battery, Activity, Wifi, Clock, MapPin } from 'lucide-react';
import type { Device } from '@/type/device';

interface DeviceDetailModalProps {
  device: Device | null;
  onClose: () => void;
}

export default function DeviceDetailModal({ device, onClose }: DeviceDetailModalProps) {
  if (!device) return null;

  const isOnline = device.isOnline;

  const sensorData = {
    fillLevel: isOnline ? 45 : 0,
    temperature: isOnline ? 24 : '--',
    battery: isOnline ? 88 : '--',
    signal: isOnline ? 'Excellent' : '--',
  };

  return (
    <div 
      className="fixed inset-0 bg-slate-900/70 backdrop-blur-md flex items-center justify-center p-4 z-[100] animate-fadeIn"
      onClick={onClose}
    >
      <div 
        className="bg-white rounded-[2rem] shadow-2xl w-full max-w-2xl overflow-hidden relative animate-fadeIn"
        onClick={(e) => e.stopPropagation()}
      >
        {/* --- HEADER với Gradient Động --- */}
        <div className={`relative h-48 w-full overflow-hidden ${
          isOnline 
            ? 'bg-gradient-to-br from-blue-600 via-blue-700 to-purple-600' 
            : 'bg-gradient-to-br from-slate-600 via-slate-700 to-slate-800'
        }`}>
           
           {/* Animated Background Shapes */}
           <div className="absolute top-0 right-0 w-72 h-72 bg-white/10 rounded-full blur-3xl -mr-20 -mt-20 animate-pulse"></div>
           <div className="absolute bottom-0 left-0 w-56 h-56 bg-black/10 rounded-full blur-2xl -ml-16 -mb-16 animate-pulse" style={{ animationDelay: '1s' }}></div>
           <div className="absolute top-1/2 left-1/2 w-40 h-40 bg-white/5 rounded-full blur-xl animate-pulse" style={{ animationDelay: '2s' }}></div>
           
           {/* Close Button */}
           <button
              onClick={onClose}
              className="absolute top-5 right-5 bg-white/20 hover:bg-white/30 backdrop-blur-sm text-white rounded-full p-2.5 transition-all z-20 hover:rotate-90 duration-300"
            >
              <X className="w-5 h-5" />
            </button>

            {/* Status Badge */}
            <div className="absolute top-5 left-5 z-10">
              <div className={`inline-flex items-center gap-2 px-4 py-2 rounded-full backdrop-blur-md ${
                isOnline 
                  ? 'bg-emerald-500/20 border border-emerald-400/30' 
                  : 'bg-slate-500/20 border border-slate-400/30'
              }`}>
                <div className={`w-2 h-2 rounded-full ${
                  isOnline ? 'bg-emerald-400 shadow-[0_0_8px_rgba(74,222,128,0.8)]' : 'bg-gray-400'
                }`}></div>
                <span className="text-white/90 text-sm font-semibold">
                  {isOnline ? 'Online & Monitoring' : 'Connection Lost'}
                </span>
              </div>
            </div>

            {/* Device Name */}
            <div className="absolute bottom-5 left-5 z-10">
              <h2 className="text-white text-2xl font-bold mb-1">{device.name}</h2>
              <p className="text-white/70 text-sm flex items-center gap-1.5">
                <MapPin className="w-3.5 h-3.5" />
                Living Room Area
              </p>
            </div>
        </div>

        {/* --- BODY --- */}
        <div className="px-8 pb-8 pt-20 relative">
            
            {/* --- FLOATING ICON (Căn chỉnh tuyệt đối) --- */}
            <div className="absolute top-0 left-8 transform -translate-y-1/2 z-20">
              <div className="relative">
                {/* Glow Effect */}
                {isOnline && (
                  <div className="absolute inset-0 bg-blue-500 rounded-2xl blur-xl opacity-30"></div>
                )}
                {/* Icon Container */}
                <div className="relative shadow-2xl rounded-2xl bg-white p-2">
                  <div className={`h-24 w-24 rounded-xl flex items-center justify-center border-4 transition-all duration-300 ${
                    isOnline 
                      ? 'bg-gradient-to-br from-blue-50 to-blue-100 border-blue-200 text-blue-600' 
                      : 'bg-slate-100 border-slate-200 text-slate-400'
                  }`}>
                    <Trash2 className="w-12 h-12" />
                  </div>
                </div>
              </div>
            </div>

            {/* --- INFO SECTION --- */}
            <div className="flex justify-between items-start mb-8 pt-2">
               <div className="ml-32">
                 <div className="flex items-center gap-2 mb-2">
                   <Wifi className={`w-4 h-4 ${isOnline ? 'text-emerald-500' : 'text-slate-400'}`} />
                   <span className="text-sm font-semibold text-slate-700">
                     {isOnline ? 'Connected' : 'Disconnected'}
                   </span>
                 </div>
                 <div className="flex items-center gap-2 text-xs text-slate-500">
                   <Clock className="w-3.5 h-3.5" />
                   Last sync: 2 minutes ago
                 </div>
               </div>
               <div className="text-right">
                  <p className="text-xs text-slate-400 font-semibold uppercase tracking-wider mb-1.5">Device ID</p>
                  <p className="font-mono text-slate-700 bg-slate-100 px-3 py-1.5 rounded-lg text-sm border border-slate-200">
                    {device.macAddress}
                  </p>
               </div>
            </div>

            {/* --- SENSOR GRID với Gradient --- */}
            <div className="grid grid-cols-4 gap-4 mb-8">
                {/* Capacity */}
                <div className="p-5 rounded-2xl bg-gradient-to-br from-blue-50 to-blue-100 border border-blue-200 flex flex-col items-center gap-3 group hover:shadow-lg hover:scale-105 transition-all duration-300">
                    <div className="p-3 bg-white rounded-xl shadow-sm">
                      <BarChart3 className="w-6 h-6 text-blue-600" />
                    </div>
                    <span className="text-3xl font-bold text-slate-800">{sensorData.fillLevel}%</span>
                    <span className="text-xs text-slate-600 font-bold uppercase tracking-wide">Fill Level</span>
                    <div className="w-full bg-white/50 rounded-full h-1.5 mt-1">
                      <div 
                        className="bg-blue-500 h-1.5 rounded-full transition-all duration-500"
                        style={{ width: `${sensorData.fillLevel}%` }}
                      ></div>
                    </div>
                </div>

                {/* Signal */}
                <div className="p-5 rounded-2xl bg-gradient-to-br from-purple-50 to-purple-100 border border-purple-200 flex flex-col items-center gap-3 group hover:shadow-lg hover:scale-105 transition-all duration-300">
                    <div className="p-3 bg-white rounded-xl shadow-sm">
                      <Signal className="w-6 h-6 text-purple-600" />
                    </div>
                    <span className="text-xl font-bold text-slate-800">{sensorData.signal}</span>
                    <span className="text-xs text-slate-600 font-bold uppercase tracking-wide">Signal</span>
                </div>

                {/* Battery */}
                <div className="p-5 rounded-2xl bg-gradient-to-br from-emerald-50 to-emerald-100 border border-emerald-200 flex flex-col items-center gap-3 group hover:shadow-lg hover:scale-105 transition-all duration-300">
                    <div className="p-3 bg-white rounded-xl shadow-sm">
                      <Battery className="w-6 h-6 text-emerald-600" />
                    </div>
                    <span className="text-3xl font-bold text-slate-800">{sensorData.battery}%</span>
                    <span className="text-xs text-slate-600 font-bold uppercase tracking-wide">Battery</span>
                    <div className="w-full bg-white/50 rounded-full h-1.5 mt-1">
                      <div 
                        className="bg-emerald-500 h-1.5 rounded-full transition-all duration-500"
                        style={{ width: `${sensorData.battery}%` }}
                      ></div>
                    </div>
                </div>

                {/* Temperature */}
                <div className="p-5 rounded-2xl bg-gradient-to-br from-orange-50 to-orange-100 border border-orange-200 flex flex-col items-center gap-3 group hover:shadow-lg hover:scale-105 transition-all duration-300">
                    <div className="p-3 bg-white rounded-xl shadow-sm">
                      <Activity className="w-6 h-6 text-orange-600" />
                    </div>
                    <span className="text-3xl font-bold text-slate-800">{sensorData.temperature}°C</span>
                    <span className="text-xs text-slate-600 font-bold uppercase tracking-wide">Temp</span>
                </div>
            </div>

            {/* --- ACTION BUTTONS Grid --- */}
            <div className="grid grid-cols-2 gap-4 pt-2">
              <button className={`py-4 rounded-xl font-bold text-sm shadow-lg hover:shadow-xl transition-all flex items-center justify-center gap-2 ${
                  isOnline 
                  ? 'bg-gradient-to-r from-blue-600 to-blue-700 text-white hover:from-blue-700 hover:to-blue-800 transform hover:scale-[1.02] active:scale-[0.98]' 
                  : 'bg-slate-200 text-slate-400 cursor-not-allowed shadow-none'
              }`}>
                  <Activity className="w-4 h-4" />
                  Control Panel
              </button>

              <button className="py-4 rounded-xl font-bold text-sm border-2 border-slate-200 bg-white text-slate-700 hover:bg-slate-50 hover:border-slate-300 transition-all flex items-center justify-center gap-2 transform hover:scale-[1.02] active:scale-[0.98]">
                  <BarChart3 className="w-4 h-4" />
                  View Analytics
              </button>
            </div>
        </div>
      </div>
    </div>
  );
}