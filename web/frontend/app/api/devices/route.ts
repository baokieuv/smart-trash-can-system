import { NextResponse } from 'next/server';
import { Device } from '@/types';

const devices: Device[] = [
  {
    id: 1, name: 'Living Room Bin', mac: '00:1A:2B:3C:4D:5E', status: 'online',
    fillLevel: 45, battery: 85, recycled: 23, nonRecycled: 15, composable: 8, total: 46
  },
  {
    id: 2, name: 'Kitchen Master', mac: 'A1:B2:C3:D4:E5:F6', status: 'online',
    fillLevel: 92, battery: 60, recycled: 45, nonRecycled: 38, composable: 12, total: 95
  },
  {
    id: 3, name: 'Office Paper', mac: '11:22:33:44:55:66', status: 'offline',
    fillLevel: 15, battery: 20, recycled: 8, nonRecycled: 2, composable: 0, total: 10
  },
  {
    id: 4, name: 'Garden Waste', mac: 'AA:BB:CC:DD:EE:FF', status: 'offline',
    fillLevel: 0, battery: 0, recycled: 0, nonRecycled: 0, composable: 0, total: 0
  },
  {
    id: 5, name: 'Bedroom Bin', mac: 'BB:CC:DD:EE:FF:11', status: 'online',
    fillLevel: 28, battery: 95, recycled: 12, nonRecycled: 8, composable: 3, total: 23
  },
  {
    id: 6, name: 'Garage Setup', mac: 'CC:DD:EE:FF:11:22', status: 'online',
    fillLevel: 67, battery: 75, recycled: 34, nonRecycled: 28, composable: 6, total: 68
  }
];

export async function GET() {
  // Giả lập delay mạng 500ms
  await new Promise((resolve) => setTimeout(resolve, 500));
  return NextResponse.json(devices);
}