import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests if available
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
};

// Patient API
export const patientAPI = {
  getAll: () => api.get('/patients'),
  getById: (id) => api.get(`/patients/${id}`),
  getByUserId: (userId) => api.get(`/patients/user/${userId}`),
  update: (id, data) => api.put(`/patients/${id}`, data),
  delete: (id) => api.delete(`/patients/${id}`),
};

// Practitioner API
export const practitionerAPI = {
  getAll: () => api.get('/practitioners'),
  getById: (id) => api.get(`/practitioners/${id}`),
  getByUserId: (userId) => api.get(`/practitioners/user/${userId}`),
  update: (id, data) => api.put(`/practitioners/${id}`, data),
  delete: (id) => api.delete(`/practitioners/${id}`),
};

// Encounter API
export const encounterAPI = {
  getAll: () => api.get('/encounters'),
  getById: (id) => api.get(`/encounters/${id}`),
  getByPatientId: (patientId) => api.get(`/encounters/patient/${patientId}`),
  create: (data) => api.post('/encounters', data),
  update: (id, data) => api.put(`/encounters/${id}`, data),
  delete: (id) => api.delete(`/encounters/${id}`),
};

// Observation API
export const observationAPI = {
  getAll: () => api.get('/observations'),
  getById: (id) => api.get(`/observations/${id}`),
  getByPatientId: (patientId) => api.get(`/observations/patient/${patientId}`),
  create: (data) => api.post('/observations', data),
  update: (id, data) => api.put(`/observations/${id}`, data),
  delete: (id) => api.delete(`/observations/${id}`),
};

// Condition API
export const conditionAPI = {
  getAll: () => api.get('/conditions'),
  getById: (id) => api.get(`/conditions/${id}`),
  getByPatientId: (patientId) => api.get(`/conditions/patient/${patientId}`),
  create: (data) => api.post('/conditions', data),
  update: (id, data) => api.put(`/conditions/${id}`, data),
  delete: (id) => api.delete(`/conditions/${id}`),
};

// Message API
export const messageAPI = {
  getByUserId: (userId) => api.get(`/messages/user/${userId}`),
  getReceived: (userId) => api.get(`/messages/received/${userId}`),
  getSent: (userId) => api.get(`/messages/sent/${userId}`),
  getUnread: (userId) => api.get(`/messages/unread/${userId}`),
  getById: (id) => api.get(`/messages/${id}`),
  getReplies: (id) => api.get(`/messages/${id}/replies`),
  create: (data) => api.post('/messages', data),
  markAsRead: (id) => api.put(`/messages/${id}/read`),
  delete: (id) => api.delete(`/messages/${id}`),
};

export default api;
