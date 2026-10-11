import React, { useState, useEffect } from 'react';
import { useToast } from '../../components/Toast';
import { vehiclesApi } from '../../api/vehicles';
import Navbar from '../../components/Navbar';
import Modal from '../../components/Modal';
import { Car, Bike, Zap, Plus, Trash2, ShieldCheck, AlertCircle } from 'lucide-react';

const VehiclesPage = () => {
  const { addToast } = useToast();

  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);

  // Add Vehicle Modal
  const [showAddModal, setShowAddModal] = useState(false);
  const [regNumber, setRegNumber] = useState('');
  const [vehicleType, setVehicleType] = useState('CAR');
  const [makeModel, setMakeModel] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Delete Modal
  const [vehicleToDelete, setVehicleToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const fetchVehicles = async () => {
    try {
      const res = await vehiclesApi.getMyVehicles();
      if (res.success) {
        setVehicles(res.data || []);
      }
    } catch (err) {
      addToast(err.message || 'Failed to fetch vehicles', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVehicles();
  }, []);

  const handleAddVehicle = async (e) => {
    e.preventDefault();
    if (!regNumber.trim()) {
      addToast('Registration number is required', 'error');
      return;
    }

    setSubmitting(true);
    try {
      const res = await vehiclesApi.registerVehicle({
        registrationNumber: regNumber.trim(),
        vehicleType,
        makeModel: makeModel.trim() || null,
      });
      addToast(res.message || 'Vehicle registered successfully!', 'success');
      setShowAddModal(false);
      setRegNumber('');
      setMakeModel('');
      fetchVehicles();
    } catch (err) {
      addToast(err.message || 'Failed to register vehicle', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteVehicle = async () => {
    if (!vehicleToDelete) return;
    setDeleting(true);
    try {
      const res = await vehiclesApi.deleteVehicle(vehicleToDelete.id);
      addToast(res.message || 'Vehicle deleted', 'success');
      setVehicleToDelete(null);
      fetchVehicles();
    } catch (err) {
      addToast(err.message || 'Failed to delete vehicle', 'error');
    } finally {
      setDeleting(false);
    }
  };

  const getVehicleIcon = (type) => {
    switch (type) {
      case 'EV':
        return <Zap size={20} color="#10b981" />;
      case 'BIKE':
        return <Bike size={20} color="#3b82f6" />;
      default:
        return <Car size={20} color="#2563eb" />;
    }
  };

  return (
    <div>
      <Navbar
        title="My Registered Vehicles"
        subtitle="Manage the vehicles you use for campus parking access."
        actions={
          <button onClick={() => setShowAddModal(true)} className="btn btn-primary btn-sm">
            <Plus size={16} />
            <span>Add Vehicle</span>
          </button>
        }
      />

      <div className="page-wrapper">
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Registered Vehicles ({vehicles.length})</h3>
          </div>

          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Vehicle</th>
                  <th>Registration Plate</th>
                  <th>Type</th>
                  <th>Make / Model</th>
                  <th>Registration Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {vehicles.length > 0 ? (
                  vehicles.map((v) => (
                    <tr key={v.id}>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem' }}>
                          {getVehicleIcon(v.vehicleType)}
                          <span style={{ fontWeight: 600 }}>{v.makeModel || v.vehicleType}</span>
                        </div>
                      </td>
                      <td>
                        <code
                          style={{
                            background: '#f1f5f9',
                            padding: '0.2rem 0.5rem',
                            borderRadius: '4px',
                            fontWeight: 700,
                            letterSpacing: '0.05em',
                          }}
                        >
                          {v.registrationNumber}
                        </code>
                      </td>
                      <td>
                        <span className="badge badge-blue">{v.vehicleType}</span>
                      </td>
                      <td>{v.makeModel || '—'}</td>
                      <td>{new Date(v.createdAt).toLocaleDateString()}</td>
                      <td>
                        <button
                          onClick={() => setVehicleToDelete(v)}
                          className="btn btn-secondary btn-sm"
                          style={{ color: '#ef4444' }}
                          title="Delete Vehicle"
                        >
                          <Trash2 size={14} />
                          <span>Delete</span>
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
                      No vehicles registered yet. Click "Add Vehicle" above to register your car, bike, or EV.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Add Vehicle Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Register New Vehicle"
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setShowAddModal(false)}>
              Cancel
            </button>
            <button className="btn btn-primary" onClick={handleAddVehicle} disabled={submitting}>
              {submitting ? 'Registering...' : 'Save Vehicle'}
            </button>
          </>
        }
      >
        <form onSubmit={handleAddVehicle}>
          <div className="form-group">
            <label className="form-label">Registration Number / Plate *</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. KA-01-AB-1234 or DL123456"
              value={regNumber}
              onChange={(e) => setRegNumber(e.target.value.toUpperCase())}
              required
            />
            <span style={{ fontSize: '0.75rem', color: '#64748b' }}>
              Numbers will be automatically normalized and capitalized.
            </span>
          </div>

          <div className="form-group">
            <label className="form-label">Vehicle Type *</label>
            <select
              className="form-select"
              value={vehicleType}
              onChange={(e) => setVehicleType(e.target.value)}
            >
              <option value="CAR">Car (Standard)</option>
              <option value="BIKE">Motorcycle / Two-Wheeler</option>
              <option value="EV">Electric Vehicle (EV)</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Make & Model (Optional)</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. Toyota Camry or Honda Civic"
              value={makeModel}
              onChange={(e) => setMakeModel(e.target.value)}
            />
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={!!vehicleToDelete}
        onClose={() => setVehicleToDelete(null)}
        title="Confirm Vehicle Deletion"
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setVehicleToDelete(null)}>
              Cancel
            </button>
            <button className="btn btn-danger" onClick={handleDeleteVehicle} disabled={deleting}>
              {deleting ? 'Deleting...' : 'Delete Vehicle'}
            </button>
          </>
        }
      >
        <p style={{ color: '#334155' }}>
          Are you sure you want to remove vehicle{' '}
          <strong>{vehicleToDelete?.registrationNumber}</strong>?
        </p>
        <p style={{ fontSize: '0.85rem', color: '#64748b', marginTop: '0.5rem' }}>
          Note: Vehicles currently associated with an active parking session cannot be deleted.
        </p>
      </Modal>
    </div>
  );
};

export default VehiclesPage;
