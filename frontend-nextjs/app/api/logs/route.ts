import { NextResponse } from 'next/server';
import { ActivityLog } from '@/types';
import { BACKEND_API_BASE } from '../config';


export async function GET(req: Request) {
  const BASE_URL = `${BACKEND_API_BASE}/notifications`;

  try {
    const token = req.headers.get('authorization')?.replace('Bearer ', '');
    
    if (!token) {
      return NextResponse.json(
        { error: 'Unauthorized' },
        { status: 401 }
      );
    }

    const logResp = await fetch(BASE_URL, {
      cache: 'no-store',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (!logResp.ok) {
      if (logResp.status === 401) {
        return NextResponse.json(
          { error: 'Unauthorized' },
          { status: 401 }
        );
      }
      throw new Error(`Failed to fetch logs: ${logResp.status}`);
    }

    const logs = await logResp.json();
    return NextResponse.json(logs, { status: 200 });
  } catch (error) {
    console.error('Error in GET /logs:', error);
    return NextResponse.json(
      { error: 'Internal Server Error' },
      { status: 500 }
    );
  }
}

// const activityLogs: LogData[] = [
//   { id: 1, deviceName: 'Kitchen Master', message: 'full capacity', type: 'WARNING', timestamp: 1 },
//   { id: 2, deviceName: 'Living Room Bin', message: 'connected', type: 'SUCCESS', timestamp: 2 },
//   { id: 3, deviceName: 'Firmware', message: 'update available', type: 'INFO', timestamp: 3 },
//   { id: 4, deviceName: 'Garden Waste', message: 'disconnected', type: 'ERROR', timestamp: 4 },
//   { id: 5, deviceName: 'System', message: 'backup completed', type: 'SUCCESS', timestamp: 5 }
// ];

// interface LogData {
//   id: number;
//   deviceName: string;
//   message: string;
//   type: string;
//   timestamp: number
// }


// export async function GET() {

//   const BASE_URL = "http://localhost:8888/api/v1/logs";

//   try{
//   //   const logResp = await fetch(BASE_URL, { cache: 'no-store' });

//   //   if(!logResp.ok){
//   //     throw new Error(`Failed to fetch device list: ${logResp.status}`);
//   //   }

//   //   const logs: LogData[] = await logResp.json();

//     return NextResponse.json(activityLogs, {status: 200});
//   }catch(error){
//     console.error('Error in GET /logs:', error);
//     return NextResponse.json(
//       { error: 'Internal Server Error' },
//       { status: 500 }
//     );
//   }
// }

// // export async function GET() {
// //   await new Promise((resolve) => setTimeout(resolve, 500));
// //   return NextResponse.json(activityLogs);
// // }