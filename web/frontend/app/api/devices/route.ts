import { NextResponse } from 'next/server';
import { Device } from '@/types';
import { cookies } from 'next/headers';

interface DeviceBasic {
  id: string;
  name: string;
  status: string;
}

interface DeviceData {
  fillLevel: number;
  recycledWasteCount: number;
  nonRecycledWasteCount: number;
  compostableWasteCount: number;
}

function getAuthToken(): string | null {
  if (typeof window !== 'undefined') {
    return localStorage.getItem('accessToken');
  }
  return null;
}

export async function GET(req: Request) {
  const BASE_URL = "http://localhost:8888/api/v1/devices";

  try {
    // Get token from request headers
    const token = req.headers.get('authorization')?.replace('Bearer ', '');
    
    if (!token) {
      return NextResponse.json(
        { error: 'Unauthorized' },
        { status: 401 }
      );
    }

    const devicesRes = await fetch(BASE_URL, {
      cache: 'no-store',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (!devicesRes.ok) {
      if (devicesRes.status === 401) {
        return NextResponse.json(
          { error: 'Unauthorized' },
          { status: 401 }
        );
      }
      throw new Error(`Failed to fetch device list: ${devicesRes.status}`);
    }

    const basicDevices: DeviceBasic[] = await devicesRes.json();

    const fullDevicesData = await Promise.all(
      basicDevices.map(async (device) => {
        try {
          const dataRes = await fetch(`${BASE_URL}/${device.id}/data`, {
            cache: 'no-store',
            headers: {
              'Authorization': `Bearer ${token}`
            }
          });

          const detailData: DeviceData = dataRes.ok
            ? await dataRes.json()
            : { fillLevel: 0, recycledWasteCount: 0, nonRecycledWasteCount: 0, compostableWasteCount: 0 };

          const totalWaste = (detailData.recycledWasteCount || 0) + 
                            (detailData.nonRecycledWasteCount || 0) + 
                            (detailData.compostableWasteCount || 0);

          const mappedDevice: Device = {
            id: device.id as any,
            name: device.name,
            mac: device.id.replaceAll("_", ":"),
            status: device.status,
            fillLevel: detailData.fillLevel,
            battery: 50,
            recycled: detailData.recycledWasteCount,
            nonRecycled: detailData.nonRecycledWasteCount,
            composable: detailData.compostableWasteCount,
            total: totalWaste
          };

          return mappedDevice;
        } catch (error) {
          console.error(`Error fetching data for device MAC ${device.id}`, error);
          return {
            id: device.id as any,
            name: device.name,
            mac: device.id.replaceAll("_", ":"),
            status: 'offline',
            fillLevel: 0, battery: 0, recycled: 0, nonRecycled: 0, composable: 0, total: 0
          } as Device;
        }
      })
    );

    return NextResponse.json(fullDevicesData);
  } catch (error) {
    console.error('Error in GET /devices:', error);
    return NextResponse.json(
      { error: 'Internal Server Error' },
      { status: 500 }
    );
  }
}