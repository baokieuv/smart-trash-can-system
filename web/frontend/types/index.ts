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

// Auth Types
export type User = {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  emailVerified: boolean;
  createdAt: number;
};

export type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
  user: User;
};

export type LoginCredentials = {
  email: string;
  password: string;
};

export type RegisterData = {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
};