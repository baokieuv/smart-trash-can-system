const rawBackendBaseUrl = (process.env.INTERNAL_API_URL || 'http://localhost:8888').replace(/\/+$/, '');

export const BACKEND_API_BASE = `${rawBackendBaseUrl}/api/v1`;
