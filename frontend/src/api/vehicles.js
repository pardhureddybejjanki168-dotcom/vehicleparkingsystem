import api from './client';

export const vehiclesApi = {
  getMyVehicles: async () => {
    return api.get('/vehicles');
  },
  registerVehicle: async (vehicleData) => {
    return api.post('/vehicles', vehicleData);
  },
  deleteVehicle: async (id) => {
    return api.delete(`/vehicles/${id}`);
  },
};
