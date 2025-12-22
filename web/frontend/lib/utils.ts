export const getStatusColor = (status: string) => {
  return status === 'online' ? 'text-green-600' : 'text-gray-400';
};

export const getStatusBg = (status: string) => {
  return status === 'online' ? 'bg-green-100' : 'bg-gray-100';
};

export const getFillLevelColor = (level: number) => {
  if (level >= 80) return 'bg-red-500';
  if (level >= 50) return 'bg-yellow-500';
  return 'bg-green-500';
};

export const getBatteryColor = (level: number) => {
  if (level >= 60) return 'text-green-600';
  if (level >= 30) return 'text-yellow-600';
  return 'text-red-600';
};

export const getLogIcon = (type: string) => {
  switch (type) {
    case 'WARNING': return '⚠️';
    case 'SUCCESS': return '✅';
    case 'ERROR': return '🔴';
    case 'INFO': return 'ℹ️';
    default: return '•';
  }
};