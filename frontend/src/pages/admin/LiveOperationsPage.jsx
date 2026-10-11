import React, { useState, useEffect } from 'react';
import { useToast } from '../../components/Toast';
import { adminApi } from '../../api/admin';
import { slotsApi } from '../../api/slots';
import Navbar from '../../components/Navbar';
import SlotGrid from '../../components/SlotGrid';
import Modal from '../../components/Modal';
import { RefreshCw, Search, LogOut, Car } from 'lucide-react';

const LiveOperationsPage = () => {
  const { addToast } = useToast();

  const [activeSessions, setActiveSessions] = useState([]);
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  // Force Exit Modal
  const [sessionToExit, setSessionToExit] = useState(null);
  const [exitNotes, setExitNotes] = useState('Authorized exit by Parking Attendant');
  const [exiting, setExiting] = useState(false);

  const fetchData = async () => {
    try {
      const [sessRes, slotsRes] = await Promise.all([
        adminApi.getActiveSessions(),
        slotsApi.getSlots(),
      ]);

      if (sessRes.success) setActiveSessions(sessRes.data || []);
      if (slotsRes.success) setSlots(slotsRes.data || []);
    } catch (err) {
      addToast(err.message || 'Failed to load active operations', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 8000);
    return () => clearInterval(interval);
  }, []);

  const handleConfirmForceExit = async () => {
    if (!sessionToExit) return;
    setExiting(true);
    try {
      const res = await adminApi.forceExit(sessionToExit.id, exitNotes);
      addToast(res.message || 'Vehicle exited successfully by admin', 'success');
      setSessionToExit(null);
      fetchData();
    } catch (err) {
      addToast(err.message || 'Failed to record vehicle exit', 'error');
    } finally {
      setExiting(false);
    }
  };

  const filteredSessions = activeSessions.filter((s) => {
    if (!searchTerm) return true;
    const term = searchTerm.toLowerCase();
    return (
      s.vehicleRegistrationNumber?.toLowerCase().includes(term) ||
      s.slotNumber?.toLowerCase().includes(term) ||
      s.userFullName?.toLowerCase().includes(term) ||
      s.studentId?.toLowerCase().includes(term)
    );
  });

  return (
    <div>
      <Navbar
        title="Campus Live Parking & Entry/Exit Operations"
        subtitle="Authoritative monitoring of currently parked vehicles and administrative exit processing."
        actions={
          <button onClick={fetchData} className="btn btn-secondary btn-sm">
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
            <span>Refresh</span>
          </button>
        }
      />

      <div className="page-wrapper">
        {/* Active Sessions Table */}
        <div className="card">
          <div className="card-header" style={{ flexWrap: 'wrap', gap: '1rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <h3 className="card-title">Currently Active Sessions ({activeSessions.length})</h3>
            </div>

            <div style={{ position: 'relative' }}>
              <input
                type="text"
                className="form-input"
                placeholder="Search plate, slot, or student..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                style={{ width: '280px', paddingLeft: '2rem' }}
              />
              <Search size={16} color="#94a3b8" style={{ position: 'absolute', left: '0.65rem', top: '50%', transform: 'translateY(-50%)' }} />
            </div>
          </div>

          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Bay / Slot</th>
                  <th>Vehicle Plate</th>
                  <th>Vehicle Type</th>
                  <th>Student Name</th>
                  <th>Student ID</th>
                  <th>Entry Time</th>
                  <th>Current Duration</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredSessions.length > 0 ? (
                  filteredSessions.map((s) => (
                    <tr key={s.id}>
                      <td style={{ fontWeight: 800, color: '#2563eb' }}>{s.slotNumber}</td>
                      <td style={{ fontWeight: 600 }}>{s.vehicleRegistrationNumber}</td>
                      <td><span className="badge badge-blue">{s.vehicleType}</span></td>
                      <td>{s.userFullName}</td>
                      <td><code>{s.studentId}</code></td>
                      <td>{new Date(s.entryTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</td>
                      <td style={{ fontFamily: 'monospace', color: '#059669', fontWeight: 600 }}>
                        {s.formattedDuration}
                      </td>
                      <td>
                        <button
                          onClick={() => setSessionToExit(s)}
                          className="btn btn-danger btn-sm"
                          title="Record Vehicle Exit"
                        >
                          <LogOut size={14} />
                          <span>Record Exit</span>
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="8" style={{ textAlign: 'center', padding: '2.5rem', color: '#64748b' }}>
                      No active vehicle sessions match the filter.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Live Slot Map */}
        <div className="slots-container">
          <div className="card-header" style={{ padding: '0 0 1rem 0' }}>
            <h3 className="card-title">Live Parking Bay Map</h3>
          </div>
          <SlotGrid slots={slots} readOnly={true} />
        </div>
      </div>

      {/* Force Exit Modal */}
      <Modal
        isOpen={!!sessionToExit}
        onClose={() => setSessionToExit(null)}
        title="Record Vehicle Exit"
        footer={
          <>
            <button className="btn btn-secondary" onClick={() => setSessionToExit(null)}>
              Cancel
            </button>
            <button className="btn btn-danger" onClick={handleConfirmForceExit} disabled={exiting}>
              {exiting ? 'Processing...' : 'Confirm Exit & Release Slot'}
            </button>
          </>
        }
      >
        <p style={{ color: '#334155', marginBottom: '1rem' }}>
          Confirm exit for vehicle <strong>{sessionToExit?.vehicleRegistrationNumber}</strong> parked in slot{' '}
          <strong>{sessionToExit?.slotNumber}</strong> by driver <strong>{sessionToExit?.userFullName}</strong>.
        </p>

        <div className="form-group">
          <label className="form-label">Administrative Notes</label>
          <input
            type="text"
            className="form-input"
            value={exitNotes}
            onChange={(e) => setExitNotes(e.target.value)}
          />
        </div>
      </Modal>
    </div>
  );
};

export default LiveOperationsPage;
