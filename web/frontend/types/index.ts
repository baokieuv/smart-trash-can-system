export type Device = {
  id: string;
  name: string;
  mac: string;
  status: string;
  fillLevel: number;
  battery: number;
  recycled: number;
  nonRecycled: number;
  composable: number;
  total: number;
};

export type ActivityLog = {
  id: number;
  deviceName: string;
  message: string;
  type: string;
  timestamp: number;
};