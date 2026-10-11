import api from './client';

export const usersApi = {
  getStudentDashboard: async () => {
    return api.get('/users/dashboard');
  },
  getProfile: async () => {
    return api.get('/users/profile');
  },
};
