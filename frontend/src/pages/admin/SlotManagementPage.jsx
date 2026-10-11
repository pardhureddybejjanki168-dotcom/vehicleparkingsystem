import React, { useState, useEffect } from 'react';
import { useToast } from '../../components/Toast';
import { adminApi } from '../../api/admin';
import { slotsApi } from '../../api/slots';
import Navbar from '../../components/Navbar';
import Modal from '../../components/Modal';
import SlotGrid from '../../components/SlotGrid';
import { Plus, Sliders, RefreshCw, AlertTriangle, CheckCircle2, Trash2 } from 'lucide-react';

const SlotManagementPage = () => {
  const { addToast } = useToast();

  const [slots, setSlots] = useState([]);
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filter
  const [selectedZoneId, setSelectedZoneId] = useState('');

  // Add Slot Modal
  const [showAddModal, setShowAddModal] = useState(false);
  const [newSlotNumber, setNewSlotNumber] = useState('');
  const [newSlotZoneId, setNewSlotZoneId] = useState('');
  const [newSlotType, setNewSlotType] = useState('CAR');
  const [creating, setCreating] = useState(false);

  // Edit Slot Modal
  const [editingSlot, setEditingSlot] = useState(null);
  const [editStatus, setEditStatus] = useState('AVAILABLE');
  const [editZoneId, setEditZoneId] = useState('');
  const [editVehicleType, setEditVehicleType] = useState('CAR');
  const [updating, setUpdating] = useState(false);

  const fetchSlotsAndZones = async () => {
    try {
      const [sRes, zRes] = await Promise.all([
        slotsApi.getSlots(selectedZoneId ? { zoneId: selectedZoneId } : {}),
        slotsApi.getZones(),
      ]);

      if (sRes.success) setSlots(sRes.data || []);
      if (zRes.success) {
        setZones(zRes.data || []);
        if (zRes.data?.length > 0 && !newSlotZoneId) {
          setNewSlotZoneId(zRes.data[0].id);
        }
      }
    } catch (err) {
      addToast(err.message || 'Failed to load slots and zones', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSlotsAndZones();
  }, [selectedZoneId]);

  const handleCreateSlot = async (e) => {
    e.preventDefault();
    if (!newSlotNumber.trim() || !newSlotZoneId) {
      addToast('Slot number and zone are required', 'error');
      return;
    }

    setCreating(true);
    try {
      const res = await adminApi.createSlot({
        slotNumber: newSlotNumber.trim().toUpperCase(),
        zoneId: Number(newSlotZoneId),
        vehicleTypeAllowed: newSlotType,
      });
      addToast(res.message || 'Slot created successfully', 'success');
      setShowAddModal(false);
      setNewSlotNumber('');
      fetchSlotsAndZones();
    } catch (err) {
      addToast(err.message || 'Failed to create slot', 'error');
    } finally {
      setCreating(false);
    }
  };

  const handleOpenEdit = (slot) => {
    setEditingSlot(slot);
    setEditStatus(slot.status);
    setEditZoneId(slot.zoneId);
    setEditVehicleType(slot.vehicleTypeAllowed);
  };

  const handleUpdateSlot = async (e) => {
    e.preventDefault();
    if (!editingSlot) return;

    setUpdating(true);
    try {
      const res = await adminApi.updateSlot(editingSlot.id, {
        status: editStatus,
        zoneId: Number(editZoneId),
        vehicleTypeAllowed: editVehicleType,
      });
      addToast(res.message || 'Slot updated successfully', 'success');
      setEditingSlot(null);
      fetchSlotsAndZones();
    } catch (err) {
      addToast(err.message || 'Failed to update slot', 'error');
    } finally {
      setUpdating(false);
    }
  };

  const handleDeleteSlot = async (slotId) => {
    if (!window.confirm('Are you sure you want to delete this parking slot?')) return;
    try {
      const res = await adminApi.deleteSlot(slotId);
      addToast(res.message || 'Slot deleted', 'success');
      setEditingSlot(null);
      fetchSlotsAndZones();
    } catch (err) {
      addToast(err.message || 'Failed to delete slot', 'error');
    }
  };

  return (
    <div>
      <Navbar
        title="Campus Parking Slot Configuration"
        subtitle="Provision new parking bays, configure zones, or toggle out-of-service maintenance."
        actions={
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button onClick={() => setShowAddModal(true)} className="btn btn-primary btn-sm">
              <Plus size={16} />
              <span>Add Parking Slot</span>
            </button>
            <button onClick={fetchSlotsAndZones} className="btn btn-secondary btn-sm">
              <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
              <span>Refresh</span>
            </button>
          </div>
        }
      />

      <div className="page-wrapper">
        <div className="slots-container">
          <div className="slots-toolbar">
            <div className="filters-group">
              <select
                className="form-select"
                value={selectedZoneId}
                onChange={(e) => setSelectedZoneId(e.target.value)}
                style={{ width: '220px' }}
              >
                <option value="">All Campus Zones ({slots.length} bays)</option>
                {zones.map((z) => (
                  <option key={z.id} value={z.id}>
                    {z.name} — {z.floor}
                  </option>
                ))}
              </select>
            </div>

            <div style={{ fontSize: '0.85rem', color: '#64748b' }}>
              Click any slot to modify zone, vehicle type, or toggle Out of Service.
            </div>
          </div>

          <SlotGrid slots={slots} onSelectSlot={handleOpenEdit} readOnly={false} />
        </div>
      </div>

      {/* Add Slot Modal */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Create New Parking Slot"
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setShowAddModal(false)}>
              Cancel
            </button>
            <button className="btn btn-primary" onClick={handleCreateSlot} disabled={creating}>
              {creating ? 'Creating...' : 'Create Slot'}
            </button>
          </>
        }
      >
        <form onSubmit={handleCreateSlot}>
          <div className="form-group">
            <label className="form-label">Slot Number *</label>
            <input
              type="text"
              className="form-input"
              placeholder="e.g. A-101 or B-205"
              value={newSlotNumber}
              onChange={(e) => setNewSlotNumber(e.target.value.toUpperCase())}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Campus Zone / Floor *</label>
            <select
              className="form-select"
              value={newSlotZoneId}
              onChange={(e) => setNewSlotZoneId(e.target.value)}
              required
            >
              {zones.map((z) => (
                <option key={z.id} value={z.id}>
                  {z.name} ({z.floor})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label className="form-label">Allowed Vehicle Type *</label>
            <select
              className="form-select"
              value={newSlotType}
              onChange={(e) => setNewSlotType(e.target.value)}
            >
              <option value="CAR">Car (Standard)</option>
              <option value="BIKE">Motorcycle / Bike</option>
              <option value="EV">Electric Vehicle (EV Charging)</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
        </form>
      </Modal>

      {/* Edit Slot Modal */}
      <Modal
        isOpen={!!editingSlot}
        onClose={() => setEditingSlot(null)}
        title={`Configure Slot ${editingSlot?.slotNumber}`}
        footer={
          <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
            <button
              type="button"
              className="btn btn-danger btn-sm"
              onClick={() => handleDeleteSlot(editingSlot.id)}
            >
              <Trash2 size={14} />
              <span>Delete Slot</span>
            </button>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button className="btn btn-secondary" onClick={() => setEditingSlot(null)}>
                Cancel
              </button>
              <button className="btn btn-primary" onClick={handleUpdateSlot} disabled={updating}>
                {updating ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </div>
        }
      >
        {editingSlot && (
          <form onSubmit={handleUpdateSlot}>
            <div className="form-group">
              <label className="form-label">Operational Status</label>
              <select
                className="form-select"
                value={editStatus}
                onChange={(e) => setEditStatus(e.target.value)}
                disabled={editingSlot.status === 'OCCUPIED'}
              >
                <option value="AVAILABLE">Available (Green)</option>
                <option value="OCCUPIED" disabled>Occupied (Managed by Active Session)</option>
                <option value="OUT_OF_SERVICE">Out of Service (Gray - Maintenance)</option>
              </select>
              {editingSlot.status === 'OCCUPIED' && (
                <span style={{ fontSize: '0.75rem', color: '#dc2626' }}>
                  Cannot alter status while an active vehicle occupies this slot.
                </span>
              )}
            </div>

            <div className="form-group">
              <label className="form-label">Parking Zone</label>
              <select
                className="form-select"
                value={editZoneId}
                onChange={(e) => setEditZoneId(e.target.value)}
                disabled={editingSlot.status === 'OCCUPIED'}
              >
                {zones.map((z) => (
                  <option key={z.id} value={z.id}>
                    {z.name} ({z.floor})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Vehicle Type Allowed</label>
              <select
                className="form-select"
                value={editVehicleType}
                onChange={(e) => setEditVehicleType(e.target.value)}
              >
                <option value="CAR">Car</option>
                <option value="BIKE">Bike</option>
                <option value="EV">EV</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
};

export default SlotManagementPage;
