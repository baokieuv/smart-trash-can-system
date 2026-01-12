"use client"

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/contexts/AuthContext';
import { Trash2 } from 'lucide-react';

export default function HomePage() {
  const router = useRouter();
  const { isAuthenticated, loading } = useAuth();

  useEffect(() => {
    if (!loading) {
      if (isAuthenticated) {
        router.push('/dashboard');
      } else {
        router.push('/login');
      }
    }
  }, [isAuthenticated, loading, router]);

  return (
    <div className="flex h-screen items-center justify-center bg-gradient-to-br from-blue-50 to-slate-100">
      <div className="text-center">
        <div className="bg-blue-600 p-6 rounded-2xl inline-block mb-4">
          <Trash2 className="text-white" size={64} />
        </div>
        <h1 className="text-4xl font-bold text-slate-800 mb-2">Smart Bin IoT</h1>
        <p className="text-slate-600 mb-6">Loading...</p>
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
      </div>
    </div>
  );
}

// "use client"
// import React, { useState, useEffect } from 'react';
// import { Trash2 } from 'lucide-react';
// import { Device, ActivityLog } from '@/types';
// import { fetchDevices, fetchLogs } from '@/services/apiService';

// import StatsCards from '@/components/dashboard/StatsCard';
// import DeviceList from '@/components/dashboard/DeviceList';
// import ActivityFeed from '@/components/dashboard/ActivityFeed';
// import DeviceDetail from '@/components/dashboard/DeviceDetail';

// export default function SmartBinDashboard() {
//   const [selectedDevice, setSelectedDevice] = useState<Device | null>(null);
//   const [devices, setDevices] = useState<Device[]>([]);
//   const [logs, setLogs] = useState<ActivityLog[]>([]);
//   const [loading, setLoading] = useState(true);

//     const loadData = async () => {
//         try{
//             const [devicesData, logsData] = await Promise.all([
//                 fetchDevices(),
//                 fetchLogs()
//             ]);
//             setDevices(devicesData);
//             setLogs(logsData);

//             // Cập nhật selectedDevice nếu đang xem chi tiết
//             if (selectedDevice) {
//                 const updatedDevice = devicesData.find(d => d.id === selectedDevice.id);
//                 if (updatedDevice) {
//                 setSelectedDevice(updatedDevice);
//                 } else {
//                 // Device đã bị xóa, quay về dashboard
//                 setSelectedDevice(null);
//                 }
//             }
//         }catch(error){
//             console.error("Failed to load dashboard data");
//         }finally {
//             setLoading(false);
//         }
//     }

//   useEffect(() => {
//     loadData();
    
//     // Tùy chọn: Auto refresh mỗi 30 giây
//     const interval = setInterval(loadData, 10000);
//     return () => clearInterval(interval);
//   }, []);

//     // Callback khi device được cập nhật hoặc xóa
//   const handleDeviceUpdated = () => {
//     loadData();
//   };

//   // Nếu người dùng chọn xem chi tiết 1 device
//   if (selectedDevice) {
//     return (
//       <DeviceDetail 
//         device={selectedDevice} 
//         onBack={() => setSelectedDevice(null)} 
//         onDeviceUpdated={handleDeviceUpdated}
//       />
//     );
//   }

//   // Màn hình loading khi mới vào
//   if (loading) {
//     return (
//       <div className="flex h-screen items-center justify-center text-slate-500">
//         Loading Smart Bin IoT System...
//       </div>
//     );
//   }

//   // Màn hình Dashboard chính
//   return (
//     <div className="min-h-screen bg-linear-to-br from-slate-50 to-slate-100">
//       {/* Header */}
//       <div className="bg-white border-b border-slate-200 sticky top-0 z-10 shadow-sm">
//         <div className="max-w-7xl mx-auto px-4 md:px-8 py-4">
//           <div className="flex items-center justify-between">
//             <div className="flex items-center gap-3">
//               <div className="bg-blue-600 p-2 rounded-lg">
//                 <Trash2 className="text-white" size={24} />
//               </div>
//               <div>
//                 <h1 className="text-2xl font-bold text-slate-800">Smart Bin IoT</h1>
//                 <p className="text-sm text-slate-600">Real-time Monitoring System</p>
//               </div>
//             </div>
//             <div className="text-xs text-slate-500 hidden md:block">
//                Updated: <span className="text-blue-600 font-semibold">Live</span>
//             </div>
//           </div>
//         </div>
//       </div>

//       <div className="max-w-7xl mx-auto px-4 md:px-8 py-6">
//         {/* Phần thống kê tổng quan */}
//         <StatsCards devices={devices} />

//         <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
//           {/* Danh sách thiết bị (Chiếm 2/3 màn hình lớn) */}
//           <div className="lg:col-span-2">
//             <DeviceList 
//                 devices={devices} 
//                 onSelectDevice={setSelectedDevice}
//                 onDeviceUpdated={handleDeviceUpdated} 
//             />
//           </div>
          
//           {/* Lịch sử hoạt động (Chiếm 1/3 màn hình lớn) */}
//           <div className="lg:col-span-1">
//             <ActivityFeed logs={logs} />
//           </div>
//         </div>
//       </div>
//     </div>
//   );
// }

