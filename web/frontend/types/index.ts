export type Device = {
  id: string;
  name: string;
  mac: string;
  status: 'online' | 'offline';
  fillLevel: number;
  battery: number;
  recycled: number;
  nonRecycled: number;
  composable: number;
  total: number;
};

export type ActivityLog = {
  id: number;
  device: string;
  message: string;
  type: 'warning' | 'success' | 'error' | 'info';
  time: string;
};