import axios from 'axios';

const API_BASE_URL = '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Her istekte JWT token'ı ekle
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 401 hatalarında otomatik logout
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
};

// Hasta API
export const hastaAPI = {
  getAll: (page, size) => {
    if (page !== undefined && page >= 0) {
      return api.get(`/hastalar?page=${page}&size=${size || 10}`);
    }
    return api.get('/hastalar');
  },
  getById: (id) => api.get(`/hastalar/${id}`),
  getByTc: (tc) => api.get(`/hastalar/tc/${tc}`),
  search: (q) => api.get(`/hastalar/ara?q=${q}`),
  create: (data) => api.post('/hastalar', data),
  update: (id, data) => api.put(`/hastalar/${id}`, data),
  delete: (id) => api.delete(`/hastalar/${id}`),
  checkDebt: (id) => api.get(`/hastalar/${id}/borc`),
};

// Randevu API
export const randevuAPI = {
  getAll: () => api.get('/randevular'),
  getById: (id) => api.get(`/randevular/${id}`),
  getByHasta: (hastaId) => api.get(`/randevular/hasta/${hastaId}`),
  getByDoktor: (doktorId) => api.get(`/randevular/doktor/${doktorId}`),
  getBekleyen: (doktorId) => api.get(`/randevular/doktor/${doktorId}/bekleyen`),
  getMusaitSaatler: (doktorId, tarih) => api.get(`/randevular/musait-saatler?doktorId=${doktorId}&tarih=${tarih}`),
  getKlinikMusaitSaatler: (klinikId, tarih) => api.get(`/randevular/musait-saatler/klinik?klinikId=${klinikId}&tarih=${tarih}`),
  getAlternatifTarihler: (klinikId, baslangic, gun = 7) => api.get(`/randevular/alternatif-tarihler?klinikId=${klinikId}&baslangic=${baslangic}&gun=${gun}`),
  create: (data) => api.post('/randevular', data),
  update: (id, data) => api.put(`/randevular/${id}`, data),
  cancel: (id) => api.put(`/randevular/${id}/iptal`),
};

// Muayene API
export const muayeneAPI = {
  getAll: () => api.get('/muayeneler'),
  getById: (id) => api.get(`/muayeneler/${id}`),
  getByRandevu: (randevuId) => api.get(`/muayeneler/randevu/${randevuId}`),
  getByHasta: (hastaId) => api.get(`/muayeneler/hasta/${hastaId}`),
  getByDoktor: (doktorId) => api.get(`/muayeneler/doktor/${doktorId}`),
  create: (data) => api.post('/muayeneler', data),
  update: (id, data) => api.put(`/muayeneler/${id}`, data),
};

// Ödeme API
export const odemeAPI = {
  getAll: () => api.get('/odemeler'),
  getById: (id) => api.get(`/odemeler/${id}`),
  getByHasta: (hastaId) => api.get(`/odemeler/hasta/${hastaId}`),
  getBekleyen: () => api.get('/odemeler/bekleyen'),
  create: (muayeneId) => api.post(`/odemeler/muayene/${muayeneId}`),
  tahsilat: (id, odemeTipi) => api.put(`/odemeler/${id}/tahsilat`, { odemeTipi }),
  sgkSorgula: (id) => api.post(`/odemeler/${id}/sgk-sorgula`),
  cancel: (id) => api.put(`/odemeler/${id}/iptal`),
};

// Klinik & Doktor API
export const klinikAPI = {
  getAll: () => api.get('/klinikler'),
  getById: (id) => api.get(`/klinikler/${id}`),
};

export const doktorAPI = {
  getAll: () => api.get('/doktorlar'),
  getByKlinik: (klinikId) => api.get(`/doktorlar/klinik/${klinikId}`),
  getById: (id) => api.get(`/doktorlar/${id}`),
};

// Yönetici API
export const yoneticiAPI = {
  getStats: () => api.get('/yonetici/istatistikler'),
  getKlinikler: () => api.get('/yonetici/klinikler'),
  getDoktorlar: () => api.get('/yonetici/doktorlar'),
  getKullanicilar: () => api.get('/yonetici/kullanicilar'),
  addKlinik: (data) => api.post('/yonetici/klinikler', data),
  updateKlinik: (id, data) => api.put(`/yonetici/klinikler/${id}`, data),
  deleteKlinik: (id) => api.delete(`/yonetici/klinikler/${id}`),
  addDoktor: (data) => api.post('/yonetici/doktorlar', data),
  updateDoktor: (id, data) => api.put(`/yonetici/doktorlar/${id}`, data),
  deleteDoktor: (id) => api.delete(`/yonetici/doktorlar/${id}`),
  toggleDoktorMusaitlik: (id) => api.put(`/yonetici/doktorlar/${id}/musaitlik`),
  addGorevli: (data) => api.post('/yonetici/kullanicilar', data),
  updateGorevli: (id, data) => api.put(`/yonetici/kullanicilar/${id}`, data),
  deleteGorevli: (id) => api.delete(`/yonetici/kullanicilar/${id}`),
};

export default api;
