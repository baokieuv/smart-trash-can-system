export const cn = (...classes: (string | undefined | null | false)[]): string => {
  return classes.filter(Boolean).join(' ');
};

export const formatMacAddress = (mac: string): string => {
  if (!mac) return '';
  return mac.toUpperCase().match(/.{1,2}/g)?.join(':') || mac;
};

export const getDeviceStatus = (isOnline: boolean) => {
  return {
    label: isOnline ? 'Online' : 'Offline',
    color: isOnline ? 'green' : 'gray',
    icon: isOnline ? 'wifi' : 'wifi-off',
  };
};