import api from './client';

export const authApi = {
  login: async (credentials) => {
    return api.post('/auth/login', credentials);
  },
  register: async (data) => {
    return api.post('/auth/register', data);
  },
  getCurrentUser: async () => {
    return api.get('/auth/me');
  },
};
