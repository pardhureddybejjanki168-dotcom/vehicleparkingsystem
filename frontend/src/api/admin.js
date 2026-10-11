import api from './client';

export const adminApi = {
  getStats: async () => {
    return api.get('/admin/stats');
  },
  getUsers: async (params = {}) => {
    return api.get('/admin/users', { params });
  },
  getUserDetails: async (id) => {
    return api.get(`/admin/users/${id}`);
  },
  toggleUserStatus: async (id, enabled) => {
    return api.patch(`/admin/users/${id}/status?enabled=${enabled}`);
  },
  createSlot: async (slotData) => {
    return api.post('/admin/slots', slotData);
  },
  updateSlot: async (id, slotData) => {
    return api.put(`/admin/slots/${id}`, slotData);
  },
  deleteSlot: async (id) => {
    return api.delete(`/admin/slots/${id}`);
  },
  getActiveSessions: async () => {
    return api.get('/admin/sessions/active');
  },
  getCampusHistory: async (params = {}) => {
    return api.get('/admin/sessions/history', { params });
  },
  forceExit: async (id, notes = 'Recorded by Administrator') => {
    return api.post(`/admin/sessions/${id}/force-exit?notes=${encodeURIComponent(notes)}`);
  },
  exportCsvUrl: (fromDate, toDate) => {
    let url = '/api/v1/admin/reports/export';
    const params = [];
    if (fromDate) params.push(`fromDate=${fromDate}`);
    if (toDate) params.push(`toDate=${toDate}`);
    if (params.length > 0) url += `?${params.join('&')}`;
    return url;
  },
};
