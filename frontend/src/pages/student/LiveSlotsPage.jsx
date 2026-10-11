import React, { useState, useEffect } from 'react';
import { useToast } from '../../components/Toast';
import { slotsApi } from '../../api/slots';
import { vehiclesApi } from '../../api/vehicles';
import { sessionsApi } from '../../api/sessions';
import Navbar from '../../components/Navbar';
import SlotGrid from '../../components/SlotGrid';
import Modal from '../../components/Modal';
import { usersApi } from '../../api/users';
import { RefreshCw, Search, Filter, Car, CheckCircle2, AlertCircle, LogOut } from 'lucide-react';

const LiveSlotsPage = () => {
  const { addToast } = useToast();

  const [slots, setSlots] = useState([]);
  const [zones, setZones] = useState([]);
  const [activeSession, setActiveSession] = useState(null);
  const [loading, setLoading] = useState(true);

  // Filters
  const [selectedZoneId, setSelectedZoneId] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  // Park Modal
  const [selectedSlotForPark, setSelectedSlotForPark] = useState(null);
  const [userVehicles, setUserVehicles] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState('');
  const [submittingEntry, setSubmittingEntry] = useState(false);
  const [submittingExit, setSubmittingExit] = useState(false);

  const fetchSlots = async () => {
    try {
      const params = {};
      if (selectedZoneId) params.zoneId = selectedZoneId;
      if (statusFilter) params.status = statusFilter;
      if (searchTerm) params.search = searchTerm;

      const [slotsRes, zonesRes, dashRes] = await Promise.all([
        slotsApi.getSlots(params),
        slotsApi.getZones(),
        usersApi.getStudentDashboard().catch(() => ({ data: null })),
      ]);

      if (slotsRes.success) {
        let loadedSlots = slotsRes.data || [];
        if (typeFilter) {
          loadedSlots = loadedSlots.filter(s => s.vehicleTypeAllowed === typeFilter || (s.status === 'OCCUPIED' && s.occupiedVehicleType === typeFilter));
        }
        setSlots(loadedSlots);
      }
      if (zonesRes.success) setZones(zonesRes.data || []);
      if (dashRes?.data?.activeSession) {
        setActiveSession(dashRes.data.activeSession);
      } else {
        setActiveSession(null);
      }
    } catch (err) {
      addToast(err.message || 'Failed to fetch parking slots', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSlots();
    // Live update via periodic polling
    const interval = setInterval(fetchSlots, 10000);
    return () => clearInterval(interval);
  }, [selectedZoneId, statusFilter, typeFilter, searchTerm]);

  const handleSlotClick = async (slot) => {
    if (slot.status !== 'AVAILABLE') return;
    try {
      const vRes = await vehiclesApi.getMyVehicles();
      const userVehs = vRes.data || [];
      setUserVehicles(userVehs);
      
      // Auto-select a matching vehicle for this bay type if possible
      const matchingVeh = userVehs.find(v => v.vehicleType === slot.vehicleTypeAllowed) || userVehs[0];
      if (matchingVeh) {
        setSelectedVehicleId(matchingVeh.id);
      }
      setSelectedSlotForPark(slot);
    } catch (err) {
      addToast(err.message || 'Error fetching vehicle details', 'error');
    }
  };

  const handleExitCurrentSession = async () => {
    if (!activeSession) return;
    setSubmittingExit(true);
    try {
      const res = await sessionsApi.exitParking({ sessionId: activeSession.id });
      addToast(res.message || `Slot ${activeSession.slotNumber} has been released!`, 'success');
      setActiveSession(null);
      fetchSlots();
    } catch (err) {
      addToast(err.message || 'Failed to exit parking session', 'error');
    } finally {
      setSubmittingExit(false);
    }
  };

  const handleConfirmPark = async (e) => {
    e.preventDefault();
    if (!selectedVehicleId || !selectedSlotForPark) {
      addToast('Please select a registered vehicle', 'error');
      return;
    }

    setSubmittingEntry(true);
    try {
      const res = await sessionsApi.enterParking({
        vehicleId: Number(selectedVehicleId),
        slotId: selectedSlotForPark.id,
      });
      addToast(res.message || `Vehicle parked in slot ${selectedSlotForPark.slotNumber}`, 'success');
      setSelectedSlotForPark(null);
      fetchSlots();
    } catch (err) {
      addToast(err.message || 'Failed to claim parking slot', 'error');
    } finally {
      setSubmittingEntry(false);
    }
  };

  return (
    <div>
      <Navbar
        title="Live Parking Slot Availability"
        subtitle="Real-time occupancy status across campus zones. Click an available slot to park."
        actions={
          <button onClick={fetchSlots} className="btn btn-secondary btn-sm" title="Refresh live status">
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
            <span>Refresh</span>
          </button>
        }
      />

      <div className="page-wrapper">
        {/* Active Session Notification / Release Bar */}
        {activeSession && (
          <div
            style={{
              backgroundColor: '#eff6ff',
              border: '1px solid #bfdbfe',
              borderRadius: '10px',
              padding: '1rem 1.25rem',
              marginBottom: '1.25rem',
              display: 'flex',
              flexWrap: 'wrap',
              alignItems: 'center',
              justifyContent: 'space-between',
              gap: '1rem',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <div style={{ background: '#2563eb', color: '#fff', borderRadius: '50%', width: '32px', height: '32px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <Car size={18} />
              </div>
              <div>
                <div style={{ fontWeight: 700, color: '#1e3a8a', fontSize: '0.95rem' }}>
                  Currently Parked in Slot {activeSession.slotNumber} ({activeSession.vehicleRegistrationNumber})
                </div>
                <div style={{ fontSize: '0.8rem', color: '#3b82f6' }}>
                  Entered: {new Date(activeSession.entryTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} • Duration: {activeSession.formattedDuration}
                </div>
              </div>
            </div>
            <button
              onClick={handleExitCurrentSession}
              disabled={submittingExit}
              className="btn btn-danger btn-sm"
              style={{ fontWeight: 600 }}
            >
              <LogOut size={14} />
              <span>{submittingExit ? 'Releasing...' : 'Exit & Release Slot'}</span>
            </button>
          </div>
        )}

        <div className="slots-container">
          {/* Toolbar */}
          <div className="slots-toolbar">
            <div className="filters-group">
              {/* Search */}
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  className="form-input"
                  placeholder="Search slot (e.g. A-001)..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  style={{ width: '220px', paddingLeft: '2rem' }}
                />
                <Search size={16} color="#94a3b8" style={{ position: 'absolute', left: '0.65rem', top: '50%', transform: 'translateY(-50%)' }} />
              </div>

              {/* Zone Filter */}
              <select
                className="form-select"
                value={selectedZoneId}
                onChange={(e) => setSelectedZoneId(e.target.value)}
                style={{ width: '180px' }}
              >
                <option value="">All Parking Zones</option>
                {zones.map((z) => (
                  <option key={z.id} value={z.id}>
                    {z.name} ({z.floor})
                  </option>
                ))}
              </select>

              {/* Status Filter */}
              <select
                className="form-select"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                style={{ width: '160px' }}
              >
                <option value="">All Statuses</option>
                <option value="AVAILABLE">Available Only</option>
                <option value="OCCUPIED">Occupied Only</option>
                <option value="OUT_OF_SERVICE">Out of Service</option>
              </select>

              {/* Vehicle Type Filter */}
              <select
                className="form-select"
                value={typeFilter}
                onChange={(e) => setTypeFilter(e.target.value)}
                style={{ width: '160px' }}
              >
                <option value="">All Vehicle Types</option>
                <option value="CAR">Car</option>
                <option value="BIKE">Motorcycle / Bike</option>
                <option value="EV">Electric Vehicle (EV)</option>
              </select>
            </div>

            {/* Accessible Legend */}
            <div className="legend-group">
              <div className="legend-item">
                <span className="legend-dot dot-available"></span>
                <span>AVAILABLE (Green)</span>
              </div>
              <div className="legend-item">
                <span className="legend-dot dot-occupied"></span>
                <span>OCCUPIED (Red)</span>
              </div>
              <div className="legend-item">
                <span className="legend-dot dot-out"></span>
                <span>OUT OF SERVICE (Gray)</span>
              </div>
            </div>
          </div>

          {/* Parking Grid */}
          <SlotGrid slots={slots} onSelectSlot={handleSlotClick} />
        </div>
      </div>

      {/* Claim Slot Modal */}
      <Modal
        isOpen={!!selectedSlotForPark}
        onClose={() => setSelectedSlotForPark(null)}
        title={`Claim Slot ${selectedSlotForPark?.slotNumber}`}
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setSelectedSlotForPark(null)}>
              Cancel
            </button>
            <button
              className="btn btn-primary"
              onClick={handleConfirmPark}
              disabled={submittingEntry || userVehicles.length === 0}
            >
              {submittingEntry ? 'Confirming...' : 'Confirm Parking'}
            </button>
          </>
        }
      >
        <div style={{ marginBottom: '1.25rem', padding: '0.85rem', background: '#f8fafc', borderRadius: '8px' }}>
          <div><strong>Location:</strong> {selectedSlotForPark?.zoneName} — {selectedSlotForPark?.zoneFloor}</div>
          <div><strong>Bay Type:</strong> {selectedSlotForPark?.vehicleTypeAllowed}</div>
        </div>

        {userVehicles.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '1rem' }}>
            <AlertCircle size={32} color="#f59e0b" style={{ margin: '0 auto 0.5rem' }} />
            <p style={{ fontWeight: 600 }}>No Registered Vehicles</p>
            <p style={{ fontSize: '0.85rem', color: '#64748b' }}>
              Please register a vehicle before parking in this slot.
            </p>
          </div>
        ) : (
          <form onSubmit={handleConfirmPark}>
            <div className="form-group">
              <label className="form-label">Select Your Vehicle *</label>
              <select
                className="form-select"
                value={selectedVehicleId}
                onChange={(e) => setSelectedVehicleId(e.target.value)}
                required
              >
                {userVehicles.map((v) => (
                  <option key={v.id} value={v.id}>
                    {v.registrationNumber} ({v.vehicleType} {v.makeModel ? `- ${v.makeModel}` : ''})
                  </option>
                ))}
              </select>
              {(() => {
                const currentVeh = userVehicles.find(v => String(v.id) === String(selectedVehicleId));
                if (!currentVeh || !selectedSlotForPark) return null;
                const matches = currentVeh.vehicleType === selectedSlotForPark.vehicleTypeAllowed;
                return (
                  <div style={{ marginTop: '0.4rem', fontSize: '0.8rem', color: matches ? '#10b981' : '#f59e0b', fontWeight: 500 }}>
                    {matches
                      ? `✓ Vehicle type matches slot specification (${currentVeh.vehicleType})`
                      : `ℹ Vehicle is ${currentVeh.vehicleType}; slot is assigned for ${selectedSlotForPark.vehicleTypeAllowed}`}
                  </div>
                );
              })()}
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
};

export default LiveSlotsPage;
