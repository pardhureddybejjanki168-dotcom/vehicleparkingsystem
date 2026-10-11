import api from './client';

export const slotsApi = {
  getSlots: async (params = {}) => {
    return api.get('/slots', { params });
  },
  getZones: async () => {
    return api.get('/slots/zones');
  },
  getSlotById: async (id) => {
    return api.get(`/slots/${id}`);
  },
};
