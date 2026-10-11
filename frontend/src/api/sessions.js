import api from './client';

export const sessionsApi = {
  enterParking: async (data) => {
    return api.post('/sessions/entry', data);
  },
  exitParking: async (data) => {
    return api.post('/sessions/exit', data);
  },
  getActiveSession: async () => {
    return api.get('/sessions/active');
  },
  getHistory: async (params = {}) => {
    return api.get('/sessions/history', { params });
  },
};
