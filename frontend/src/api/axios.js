import axios from 'axios';
import { getToken, setToken, notifyUnauthorized } from './client';

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const http = axios.create({ baseURL: BASE_URL });

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

http.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401) {
      const hadToken = Boolean(getToken());
      setToken(null);
      if (hadToken) notifyUnauthorized();
    }
    return Promise.reject(error);
  },
);

export function apiErrorMessage(error, fallback = 'Error de conexión con el servidor') {
  return error.response?.data?.message || error.message || fallback;
}
