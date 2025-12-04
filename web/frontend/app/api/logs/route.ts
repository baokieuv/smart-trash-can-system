import { NextResponse } from 'next/server';
import { ActivityLog } from '@/types';

const activityLogs: ActivityLog[] = [
  { id: 1, device: 'Kitchen Master', message: 'full capacity', type: 'warning', time: '2 mins ago' },
  { id: 2, device: 'Living Room Bin', message: 'connected', type: 'success', time: '15 mins ago' },
  { id: 3, device: 'Firmware', message: 'update available', type: 'info', time: '1 hour ago' },
  { id: 4, device: 'Garden Waste', message: 'disconnected', type: 'error', time: '2 hours ago' },
  { id: 5, device: 'System', message: 'backup completed', type: 'success', time: '3 hours ago' }
];

export async function GET() {
  await new Promise((resolve) => setTimeout(resolve, 500));
  return NextResponse.json(activityLogs);
}