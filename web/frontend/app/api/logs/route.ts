import { NextResponse } from 'next/server';
import { ActivityLog } from '@/types';

// const activityLogs: ActivityLog[] = [
//   { id: 1, device: 'Kitchen Master', message: 'full capacity', type: 'warning', time: '2 mins ago' },
//   { id: 2, device: 'Living Room Bin', message: 'connected', type: 'success', time: '15 mins ago' },
//   { id: 3, device: 'Firmware', message: 'update available', type: 'info', time: '1 hour ago' },
//   { id: 4, device: 'Garden Waste', message: 'disconnected', type: 'error', time: '2 hours ago' },
//   { id: 5, device: 'System', message: 'backup completed', type: 'success', time: '3 hours ago' }
// ];

interface LogData {
  id: number;
  deviceName: string;
  message: string;
  type: string;
  timestamp: number
}


export async function GET() {

  const BASE_URL = "http://localhost:8888/api/v1/logs";

  try{
    const logResp = await fetch(BASE_URL, { cache: 'no-store' });

    if(!logResp.ok){
      throw new Error(`Failed to fetch device list: ${logResp.status}`);
    }

    const logs: LogData[] = await logResp.json();

    return NextResponse.json(logs, {status: 200});
  }catch(error){
    console.error('Error in GET /logs:', error);
    return NextResponse.json(
      { error: 'Internal Server Error' },
      { status: 500 }
    );
  }
}

// export async function GET() {
//   await new Promise((resolve) => setTimeout(resolve, 500));
//   return NextResponse.json(activityLogs);
// }