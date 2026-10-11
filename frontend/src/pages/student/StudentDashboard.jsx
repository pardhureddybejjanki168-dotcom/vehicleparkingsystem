import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../components/Toast';
import { usersApi } from '../../api/users';
import { sessionsApi } from '../../api/sessions';
import { vehiclesApi } from '../../api/vehicles';
import { slotsApi } from '../../api/slots';
import Navbar from '../../components/Navbar';
import StatCard from '../../components/StatCard';
import Modal from '../../components/Modal';
import {
  Car,
  MapPin,
  Clock,
  ArrowRight,
  LogOut,
  CheckCircle2,
  RefreshCw,
  AlertCircle,
} from 'lucide-react';

const StudentDashboard = () => {
  const { user } = useAuth();
  const { addToast } = useToast();

  const [dashboardData, setDashboardData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [elapsedTimer, setElapsedTimer] = useState('');

  // Exit Modal State
  const [showExitModal, setShowExitModal] = useState(false);
  const [exitNotes, setExitNotes] = useState('');
  const [submittingExit, setSubmittingExit] = useState(false);

  // Quick Park Modal State
  const [showEntryModal, setShowEntryModal] = useState(false);
  const [vehicles, setVehicles] = useState([]);
  const [availableSlots, setAvailableSlots] = useState([]);
  const [selectedVehicleId, setSelectedVehicleId] = useState('');
  const [selectedSlotId, setSelectedSlotId] = useState('');
  const [submittingEntry, setSubmittingEntry] = useState(false);

  const fetchDashboard = async () => {
    try {
      const res = await usersApi.getStudentDashboard();
      if (res.success) {
        setDashboardData(res.data);
      }
    } catch (err) {
      addToast(err.message || 'Failed to load dashboard data', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboard();
    const interval = setInterval(fetchDashboard, 15000); // 15s poll
    return () => clearInterval(interval);
  }, []);

  // Live timer for active session
  useEffect(() => {
    if (!dashboardData?.activeSession?.entryTime) {
      setElapsedTimer('');
      return;
    }

    const calculateElapsed = () => {
      const entry = new Date(dashboardData.activeSession.entryTime);
      const now = new Date();
      const diffMs = Math.max(0, now - entry);
      const totalSecs = Math.floor(diffMs / 1000);
      const hours = Math.floor(totalSecs / 3600);
      const mins = Math.floor((totalSecs % 3600) / 60);
      const secs = totalSecs % 60;

      setElapsedTimer(`${hours > 0 ? `${hours}h ` : ''}${mins}m ${secs < 10 ? '0' : ''}${secs}s`);
    };

    calculateElapsed();
    const timer = setInterval(calculateElapsed, 1000);
    return () => clearInterval(timer);
  }, [dashboardData?.activeSession]);

  const handleOpenEntryModal = async () => {
    try {
      const [vRes, sRes] = await Promise.all([
        vehiclesApi.getMyVehicles(),
        slotsApi.getSlots({ status: 'AVAILABLE' }),
      ]);
      setVehicles(vRes.data || []);
      setAvailableSlots(sRes.data || []);
      if (vRes.data?.length > 0) setSelectedVehicleId(vRes.data[0].id);
      if (sRes.data?.length > 0) setSelectedSlotId(sRes.data[0].id);
      setShowEntryModal(true);
    } catch (err) {
      addToast(err.message || 'Failed to initialize parking request', 'error');
    }
  };

  const handleConfirmEntry = async (e) => {
    e.preventDefault();
    if (!selectedVehicleId || !selectedSlotId) {
      addToast('Please select both a vehicle and an available slot', 'error');
      return;
    }

    setSubmittingEntry(true);
    try {
      const res = await sessionsApi.enterParking({
        vehicleId: Number(selectedVehicleId),
        slotId: Number(selectedSlotId),
      });
      addToast(res.message || 'Vehicle parked successfully!', 'success');
      setShowEntryModal(false);
      fetchDashboard();
    } catch (err) {
      addToast(err.message || 'Failed to claim parking slot', 'error');
    } finally {
      setSubmittingEntry(false);
    }
  };

  const handleConfirmExit = async () => {
    if (!dashboardData?.activeSession?.id) return;
    setSubmittingExit(true);
    try {
      const res = await sessionsApi.exitParking({
        sessionId: dashboardData.activeSession.id,
        notes: exitNotes,
      });
      addToast(res.message || 'Vehicle exited. Parking slot released.', 'success');
      setShowExitModal(false);
      setExitNotes('');
      fetchDashboard();
    } catch (err) {
      addToast(err.message || 'Failed to exit parking session', 'error');
    } finally {
      setSubmittingExit(false);
    }
  };

  return (
    <div>
      <Navbar
        title={`Welcome back, ${dashboardData?.studentName || user?.fullName || 'Student'}!`}
        subtitle="Manage your vehicles, claim available slots, and monitor active sessions."
        actions={
          <button onClick={fetchDashboard} className="btn btn-secondary btn-sm" title="Refresh data">
            <RefreshCw size={14} />
            <span>Refresh</span>
          </button>
        }
      />

      <div className="page-wrapper">
        {/* Active Session Widget */}
        {dashboardData?.activeSession ? (
          <div className="active-session-banner">
            <div>
              <div className="session-badge-pulse">
                <span className="pulse-dot"></span>
                <span>ACTIVE PARKING SESSION</span>
              </div>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.75rem' }}>
                <span className="session-slot-pill">Slot {dashboardData.activeSession.slotNumber}</span>
                <span style={{ color: '#94a3b8', fontSize: '0.9rem' }}>
                  ({dashboardData.activeSession.zoneName} - {dashboardData.activeSession.zoneFloor})
                </span>
              </div>

              <div className="session-info-grid">
                <div>
                  <div className="session-info-item-label">Vehicle Registration</div>
                  <div className="session-info-item-val">{dashboardData.activeSession.vehicleRegistrationNumber}</div>
                </div>
                <div>
                  <div className="session-info-item-label">Entry Timestamp</div>
                  <div className="session-info-item-val">
                    {new Date(dashboardData.activeSession.entryTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </div>
                </div>
                <div>
                  <div className="session-info-item-label">Live Duration</div>
                  <div className="session-info-item-val" style={{ color: '#34d399', fontFamily: 'monospace' }}>
                    {elapsedTimer || dashboardData.activeSession.formattedDuration}
                  </div>
                </div>
              </div>
            </div>

            <div>
              <button
                onClick={() => setShowExitModal(true)}
                className="btn btn-danger"
                style={{ padding: '0.75rem 1.5rem', fontWeight: 700 }}
              >
                <LogOut size={18} />
                <span>Exit Parking</span>
              </button>
            </div>
          </div>
        ) : (
          <div
            style={{
              backgroundColor: '#fff',
              border: '1px solid #e2e8f0',
              borderRadius: '12px',
              padding: '1.5rem 2rem',
              marginBottom: '2rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              boxShadow: '0 1px 3px rgba(0,0,0,0.05)',
            }}
          >
            <div>
              <h3 style={{ fontSize: '1.15rem', fontWeight: 700, color: '#0f172a' }}>
                No Active Parking Session
              </h3>
              <p style={{ color: '#64748b', fontSize: '0.875rem', marginTop: '0.2rem' }}>
                You currently have no vehicle parked on campus. Check live availability and claim a bay.
              </p>
            </div>
            <button onClick={handleOpenEntryModal} className="btn btn-primary">
              <Car size={18} />
              <span>Park Vehicle Now</span>
            </button>
          </div>
        )}

        {/* Stats Grid */}
        <div className="stats-grid">
          <StatCard
            label="Available Campus Slots"
            value={dashboardData?.availableSlotsCount ?? '-'}
            subtext={`Out of ${dashboardData?.totalSlotsCount || 0} total bays`}
            icon={MapPin}
            theme="green"
          />
          <StatCard
            label="Registered Vehicles"
            value={dashboardData?.totalRegisteredVehicles ?? '-'}
            subtext="Associated with your ID"
            icon={Car}
            theme="blue"
          />
          <StatCard
            label="My Completed Trips"
            value={dashboardData?.recentSessions?.length ?? 0}
            subtext="Recent history records"
            icon={Clock}
            theme="slate"
          />
        </div>

        {/* Recent Personal Parking Records */}
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Recent Parking Activity</h3>
            <Link to="/history" className="btn btn-secondary btn-sm">
              <span>View Full History</span>
              <ArrowRight size={14} />
            </Link>
          </div>

          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Vehicle</th>
                  <th>Parking Slot</th>
                  <th>Entry Time</th>
                  <th>Exit Time</th>
                  <th>Duration</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {dashboardData?.recentSessions && dashboardData.recentSessions.length > 0 ? (
                  dashboardData.recentSessions.map((session) => (
                    <tr key={session.id}>
                      <td style={{ fontWeight: 600 }}>{session.vehicleRegistrationNumber}</td>
                      <td>{session.slotNumber} ({session.zoneFloor})</td>
                      <td>{new Date(session.entryTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}</td>
                      <td>{session.exitTime ? new Date(session.exitTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' }) : '—'}</td>
                      <td>{session.formattedDuration}</td>
                      <td>
                        <span className={`badge ${session.status === 'ACTIVE' ? 'badge-green' : 'badge-slate'}`}>
                          {session.status}
                        </span>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', padding: '2rem', color: '#64748b' }}>
                      No parking records found yet. Park a vehicle to get started!
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* Park Vehicle Modal */}
      <Modal
        isOpen={showEntryModal}
        onClose={() => setShowEntryModal(false)}
        title="Start Parking Session"
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setShowEntryModal(false)}>
              Cancel
            </button>
            <button
              className="btn btn-primary"
              onClick={handleConfirmEntry}
              disabled={submittingEntry || vehicles.length === 0 || availableSlots.length === 0}
            >
              {submittingEntry ? 'Confirming...' : 'Confirm Parking'}
            </button>
          </>
        }
      >
        {vehicles.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '1rem' }}>
            <AlertCircle size={36} color="#f59e0b" style={{ margin: '0 auto 0.75rem' }} />
            <p style={{ fontWeight: 600, color: '#0f172a' }}>No Vehicles Registered</p>
            <p style={{ fontSize: '0.85rem', color: '#64748b', margin: '0.5rem 0 1rem' }}>
              You need to register at least one vehicle before parking.
            </p>
            <Link to="/vehicles" className="btn btn-primary btn-sm">
              Register Vehicle First
            </Link>
          </div>
        ) : (
          <form onSubmit={handleConfirmEntry}>
            <div className="form-group">
              <label className="form-label">Select Registered Vehicle *</label>
              <select
                className="form-select"
                value={selectedVehicleId}
                onChange={(e) => setSelectedVehicleId(e.target.value)}
                required
              >
                {vehicles.map((v) => (
                  <option key={v.id} value={v.id}>
                    {v.registrationNumber} ({v.vehicleType} {v.makeModel ? `- ${v.makeModel}` : ''})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Select Available Slot *</label>
              {availableSlots.length === 0 ? (
                <p style={{ color: '#dc2626', fontSize: '0.85rem' }}>No slots currently available.</p>
              ) : (
                <select
                  className="form-select"
                  value={selectedSlotId}
                  onChange={(e) => setSelectedSlotId(e.target.value)}
                  required
                >
                  {availableSlots.map((s) => (
                    <option key={s.id} value={s.id}>
                      Slot {s.slotNumber} — {s.zoneName} ({s.zoneFloor}) [{s.vehicleTypeAllowed}]
                    </option>
                  ))}
                </select>
              )}
            </div>
          </form>
        )}
      </Modal>

      {/* Exit Parking Modal */}
      <Modal
        isOpen={showExitModal}
        onClose={() => setShowExitModal(false)}
        title="Confirm Vehicle Exit"
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setShowExitModal(false)}>
              Cancel
            </button>
            <button
              className="btn btn-danger"
              onClick={handleConfirmExit}
              disabled={submittingExit}
            >
              {submittingExit ? 'Processing Exit...' : 'Confirm Exit & Release Slot'}
            </button>
          </>
        }
      >
        <p style={{ fontSize: '0.9rem', color: '#334155', marginBottom: '1rem' }}>
          Are you sure you want to end your parking session for slot{' '}
          <strong>{dashboardData?.activeSession?.slotNumber}</strong>? The parking slot will be released back to the campus.
        </p>

        <div className="form-group">
          <label className="form-label">Exit Notes (Optional)</label>
          <input
            type="text"
            className="form-input"
            placeholder="e.g. Completed lecture"
            value={exitNotes}
            onChange={(e) => setExitNotes(e.target.value)}
          />
        </div>
      </Modal>
    </div>
  );
};

export default StudentDashboard;
