'use client';

import { useState, useEffect } from 'react';
import { Search, LayoutGrid, RefreshCw, Server, Wifi, AlertCircle, Clock, Activity as ActivityIcon, Zap, TrendingUp, Sparkles } from 'lucide-react';
import DeviceCard from '@/components/DeviceCard';
import DeviceDetailModal from '@/components/DeviceDetailModal';
import Loading from '@/components/Loading';
import type { Device } from '@/type/device';

// --- MOCK DATA ---
const MOCK_DEVICES: Device[] = [
  { id: '1', name: 'Living Room Bin', macAddress: '00:1A:2B:3C:4D:5E', isOnline: true },
  { id: '2', name: 'Kitchen Master', macAddress: 'A1:B2:C3:D4:E5:F6', isOnline: true },
  { id: '3', name: 'Office Paper', macAddress: '11:22:33:44:55:66', isOnline: false },
  { id: '4', name: 'Garden Waste', macAddress: 'AA:BB:CC:DD:EE:FF', isOnline: false },
  { id: '5', name: 'Bedroom Bin', macAddress: 'BB:CC:DD:EE:FF:11', isOnline: true },
  { id: '6', name: 'Garage Setup', macAddress: 'CC:DD:EE:FF:11:22', isOnline: true },
];

// Mock Activity Logs
const RECENT_ACTIVITY = [
  { id: 1, text: 'Kitchen Master full capacity', time: '2 mins ago', type: 'warning' },
  { id: 2, text: 'Living Room Bin connected', time: '15 mins ago', type: 'success' },
  { id: 3, text: 'Firmware update available', time: '1 hour ago', type: 'info' },
  { id: 4, text: 'Garden Waste disconnected', time: '2 hours ago', type: 'error' },
  { id: 5, text: 'System backup completed', time: '5 hours ago', type: 'success' },
];

export default function DashboardPage() {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedDevice, setSelectedDevice] = useState<Device | null>(null);
  const [refreshing, setRefreshing] = useState<boolean>(false);

  const fetchDevices = async (showRefreshing: boolean = false) => {
    if (showRefreshing) setRefreshing(true);
    else setLoading(true);
    await new Promise(resolve => setTimeout(resolve, 800));
    setDevices(MOCK_DEVICES);
    setLoading(false);
    setRefreshing(false);
  };

  useEffect(() => {
    fetchDevices();
  }, []);

  const filteredDevices = devices.filter(device =>
    device.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    device.macAddress.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const stats = {
    total: devices.length,
    online: devices.filter(d => d.isOnline).length,
    offline: devices.filter(d => !d.isOnline).length
  };

  if (loading) return <Loading />;

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50">
      
      {/* --- HEADER: Logo & Search --- */}
      <header className="sticky top-0 z-30 bg-white/90 backdrop-blur-xl border-b border-slate-200/60 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row md:items-center justify-between py-5 gap-4">
            
            {/* Logo với Gradient */}
            <div className="flex items-center gap-3">
              <div className="relative bg-gradient-to-br from-blue-600 to-blue-700 p-3 rounded-2xl shadow-lg shadow-blue-200/50 group hover:shadow-blue-300/60 transition-all duration-300">
                <LayoutGrid className="w-6 h-6 text-white group-hover:scale-110 transition-transform" />
                <div className="absolute inset-0 bg-gradient-to-br from-white/20 to-transparent rounded-2xl"></div>
              </div>
              <div>
                <h1 className="text-xl font-bold bg-gradient-to-r from-slate-800 to-slate-600 bg-clip-text text-transparent">Smart Bin IoT</h1>
                <p className="text-xs font-medium text-slate-500">Real-time Monitoring System</p>
              </div>
            </div>

            {/* Search Bar với Animation */}
            <div className="flex items-center gap-3">
              <div className="relative w-full max-w-md group">
                <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                  <Search className="h-4 w-4 text-slate-400 group-focus-within:text-blue-500 transition-colors" />
                </div>
                <input
                  type="text"
                  className="block w-full pl-11 pr-4 py-2.5 bg-slate-50 border border-slate-200 focus:bg-white focus:border-blue-400 focus:ring-4 focus:ring-blue-100 rounded-xl text-sm transition-all outline-none"
                  placeholder="Search devices..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>

              <button 
                onClick={() => fetchDevices(true)}
                className={`p-2.5 rounded-xl bg-slate-50 hover:bg-white hover:shadow-md border border-slate-200 text-slate-600 hover:text-blue-600 transition-all ${refreshing ? 'animate-spin text-blue-600' : ''}`}
              >
                <RefreshCw className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* --- MAIN CONTENT --- */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
        
        {/* 1. STATS SECTION với Gradient Cards */}
        <section className="space-y-4">
          <div className="flex items-center gap-3">
            <div className="h-1 w-12 bg-gradient-to-r from-blue-600 to-transparent rounded-full"></div>
            <h2 className="text-lg font-bold text-slate-700">System Overview</h2>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {/* Total Card */}
          <div className="relative bg-gradient-to-br from-blue-500 to-blue-600 p-6 rounded-2xl shadow-lg shadow-blue-200/50 overflow-hidden group hover:shadow-xl hover:shadow-blue-300/60 transition-all duration-300 hover:-translate-y-1">
            <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full blur-2xl -mr-8 -mt-8"></div>
            <div className="absolute bottom-0 left-0 w-24 h-24 bg-black/10 rounded-full blur-xl -ml-6 -mb-6"></div>
            <div className="relative z-10">
              <div className="flex items-center justify-between mb-3">
                <p className="text-sm font-semibold text-blue-100">Total Devices</p>
                <div className="p-2.5 bg-white/20 rounded-lg backdrop-blur-sm">
                  <Server className="w-5 h-5 text-white" />
                </div>
              </div>
              <h3 className="text-4xl font-bold text-white mb-1">{stats.total}</h3>
              <p className="text-xs text-blue-100 flex items-center gap-1">
                <TrendingUp className="w-3 h-3" />
                All systems tracked
              </p>
            </div>
          </div>

          {/* Online Card */}
          <div className="relative bg-gradient-to-br from-emerald-500 to-emerald-600 p-6 rounded-2xl shadow-lg shadow-emerald-200/50 overflow-hidden group hover:shadow-xl hover:shadow-emerald-300/60 transition-all duration-300 hover:-translate-y-1">
            <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full blur-2xl -mr-8 -mt-8"></div>
            <div className="absolute bottom-0 left-0 w-24 h-24 bg-black/10 rounded-full blur-xl -ml-6 -mb-6"></div>
            <div className="relative z-10">
              <div className="flex items-center justify-between mb-3">
                <p className="text-sm font-semibold text-emerald-100">Online Systems</p>
                <div className="p-2.5 bg-white/20 rounded-lg backdrop-blur-sm">
                  <Wifi className="w-5 h-5 text-white" />
                </div>
              </div>
              <h3 className="text-4xl font-bold text-white mb-1">{stats.online}</h3>
              <div className="flex items-center gap-2">
                <span className="relative flex h-2 w-2">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-white opacity-75"></span>
                  <span className="relative inline-flex rounded-full h-2 w-2 bg-white"></span>
                </span>
                <p className="text-xs text-emerald-100">Active & Connected</p>
              </div>
            </div>
          </div>

          {/* Offline Card */}
          <div className="relative bg-gradient-to-br from-slate-700 to-slate-800 p-6 rounded-2xl shadow-lg shadow-slate-300/30 overflow-hidden group hover:shadow-xl hover:shadow-slate-400/40 transition-all duration-300 hover:-translate-y-1">
            <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full blur-2xl -mr-8 -mt-8"></div>
            <div className="absolute bottom-0 left-0 w-24 h-24 bg-black/20 rounded-full blur-xl -ml-6 -mb-6"></div>
            <div className="relative z-10">
              <div className="flex items-center justify-between mb-3">
                <p className="text-sm font-semibold text-slate-300">Offline / Error</p>
                <div className="p-2.5 bg-white/10 rounded-lg backdrop-blur-sm">
                  <AlertCircle className="w-5 h-5 text-slate-200" />
                </div>
              </div>
              <h3 className="text-4xl font-bold text-white mb-1">{stats.offline}</h3>
              <p className="text-xs text-slate-300">Needs attention</p>
            </div>
          </div>
        </div>
        </section>

        {/* Divider Line */}
        <div className="relative py-4">
          <div className="absolute inset-0 flex items-center">
            <div className="w-full border-t border-slate-200"></div>
          </div>
          <div className="relative flex justify-center">
            <span className="bg-gradient-to-br from-slate-50 via-blue-50/30 to-slate-50 px-4 text-xs text-slate-400 font-medium">DEVICE MANAGEMENT</span>
          </div>
        </div>

        {/* 2. CONTENT GRID */}
        <section>
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          
          {/* LEFT: Device List */}
          <div className="lg:col-span-3">
            <div className="flex items-center justify-between mb-6">
              <div>
                <h2 className="text-xl font-bold text-slate-800 flex items-center gap-2">
                  <Zap className="w-5 h-5 text-blue-500" />
                  All Devices
                </h2>
                <p className="text-xs text-slate-500 mt-1">Manage and monitor your smart bins</p>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-xs bg-blue-50 text-blue-600 px-3 py-1.5 rounded-full font-semibold border border-blue-100">
                  {filteredDevices.length} devices
                </span>
                <span className="text-xs bg-slate-100 text-slate-600 px-3 py-1.5 rounded-full font-semibold">
                  Last updated: Just now
                </span>
              </div>
            </div>

            {filteredDevices.length === 0 ? (
              <div className="bg-white/60 backdrop-blur-sm rounded-3xl p-16 text-center border-2 border-dashed border-slate-200">
                <Search className="w-12 h-12 text-slate-300 mx-auto mb-4" />
                <p className="text-slate-500 font-medium">No devices found</p>
                <p className="text-sm text-slate-400 mt-1">Try adjusting your search</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
                {filteredDevices.map((device) => (
                  <DeviceCard
                    key={device.id}
                    device={device}
                    onClick={setSelectedDevice}
                  />
                ))}
              </div>
            )}
          </div>

          {/* RIGHT: Recent Activity */}
          <div className="lg:col-span-1">
            <div className="bg-white/60 backdrop-blur-sm rounded-2xl border border-slate-200 shadow-sm overflow-hidden sticky top-28">
              <div className="p-5 border-b border-slate-100 bg-gradient-to-r from-slate-50 to-white flex items-center gap-2">
                <div className="p-2 bg-blue-50 rounded-lg">
                  <ActivityIcon className="w-4 h-4 text-blue-600" />
                </div>
                <h3 className="font-bold text-slate-800">Activity Feed</h3>
              </div>
              <div className="p-5 space-y-6 max-h-96 overflow-y-auto">
                {RECENT_ACTIVITY.map((log, index) => (
                  <div key={log.id} className="relative pl-8 group">
                    {/* Timeline Line - chỉ hiển thị nếu không phải item cuối */}
                    {index !== RECENT_ACTIVITY.length - 1 && (
                      <div className="absolute left-2.5 top-6 bottom-0 w-0.5 bg-gradient-to-b from-slate-200 to-transparent"></div>
                    )}
                    
                    {/* Dot - căn chỉnh với dòng đầu tiên của text */}
                    <div className={`absolute left-0 top-1 w-5 h-5 rounded-full border-2 border-white shadow-sm group-hover:scale-110 transition-transform ${
                      log.type === 'warning' ? 'bg-orange-400' :
                      log.type === 'error' ? 'bg-red-500' : 
                      log.type === 'success' ? 'bg-emerald-500' : 'bg-blue-400'
                    }`}></div>
                    
                    <p className="text-sm text-slate-700 font-medium leading-snug mb-1.5">{log.text}</p>
                    <div className="flex items-center gap-1.5 text-xs text-slate-400">
                      <Clock className="w-3 h-3" />
                      {log.time}
                    </div>
                  </div>
                ))}
              </div>
              <div className="p-4 border-t border-slate-100 bg-slate-50/50 text-center">
                <button className="text-xs text-blue-600 font-bold hover:text-blue-700 transition-colors">View All Activity →</button>
              </div>
            </div>
          </div>

        </div>
        </section>
      </main>

      {selectedDevice && (
        <DeviceDetailModal
          device={selectedDevice}
          onClose={() => setSelectedDevice(null)}
        />
      )}
    </div>
  );
} 