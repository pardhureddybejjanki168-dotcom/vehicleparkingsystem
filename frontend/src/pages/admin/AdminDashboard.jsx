import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useToast } from '../../components/Toast';
import { adminApi } from '../../api/admin';
import Navbar from '../../components/Navbar';
import StatCard from '../../components/StatCard';
import {
  Users,
  MapPin,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Clock,
  ArrowDownRight,
  ArrowUpRight,
  Percent,
  RefreshCw,
  LogOut,
  Sliders,
} from 'lucide-react';

const AdminDashboard = () => {
  const { addToast } = useToast();

  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchStats = async () => {
    try {
      const res = await adminApi.getStats();
      if (res.success) {
        setStats(res.data);
      }
    } catch (err) {
      addToast(err.message || 'Failed to load administrator statistics', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
    const interval = setInterval(fetchStats, 15000);
    return () => clearInterval(interval);
  }, []);

  const handleForceExit = async (sessionId) => {
    try {
      const res = await adminApi.forceExit(sessionId, 'Admin force exit from dashboard');
      addToast(res.message || 'Vehicle exited by admin', 'success');
      fetchStats();
    } catch (err) {
      addToast(err.message || 'Failed to force exit vehicle', 'error');
    }
  };

  return (
    <div>
      <Navbar
        title="Campus Parking Operations Overview"
        subtitle="Global real-time metrics, occupancy statistics, and active session controls."
        actions={
          <button onClick={fetchStats} className="btn btn-secondary btn-sm">
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
            <span>Refresh</span>
          </button>
        }
      />

      <div className="page-wrapper">
        {/* Metric Cards Grid */}
        <div className="stats-grid">
          <StatCard
            label="Total Registered Students"
            value={stats?.totalUsers ?? '-'}
            subtext="Enrolled drivers"
            icon={Users}
            theme="blue"
          />
          <StatCard
            label="Total Parking Slots"
            value={stats?.totalSlots ?? '-'}
            subtext="Campus inventory"
            icon={MapPin}
            theme="slate"
          />
          <StatCard
            label="Available Slots"
            value={stats?.availableSlots ?? '-'}
            subtext="Ready for entry"
            icon={CheckCircle2}
            theme="green"
          />
          <StatCard
            label="Occupied Slots"
            value={stats?.occupiedSlots ?? '-'}
            subtext="Currently parked"
            icon={XCircle}
            theme="red"
          />
          <StatCard
            label="Out of Service"
            value={stats?.outOfServiceSlots ?? '-'}
            subtext="Maintenance / blocked"
            icon={AlertTriangle}
            theme="amber"
          />
          <StatCard
            label="Campus Occupancy Rate"
            value={`${stats?.occupancyRatePercent ?? 0}%`}
            subtext="Capacity utilization"
            icon={Percent}
            theme="blue"
          />
          <StatCard
            label="Today's Vehicle Entries"
            value={stats?.todayEntries ?? '-'}
            subtext="Since 12:00 AM"
            icon={ArrowDownRight}
            theme="green"
          />
          <StatCard
            label="Today's Vehicle Exits"
            value={stats?.todayExits ?? '-'}
            subtext="Since 12:00 AM"
            icon={ArrowUpRight}
            theme="slate"
          />
        </div>

        {/* Zone Breakdown & Active Sessions */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.5rem', marginBottom: '2rem' }}>
          {/* Zone Occupancy */}
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">Occupancy by Zone</h3>
            </div>
            <div className="card-body">
              {stats?.zoneOccupancy && Object.keys(stats.zoneOccupancy).length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {Object.entries(stats.zoneOccupancy).map(([zoneName, occupiedCount]) => (
                    <div key={zoneName} style={{ borderBottom: '1px solid #f1f5f9', paddingBottom: '0.75rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', fontWeight: 600 }}>
                        <span>{zoneName}</span>
                        <span style={{ color: '#ef4444' }}>{occupiedCount} Occupied</span>
                      </div>
                      <div
                        style={{
                          width: '100%',
                          height: '8px',
                          background: '#e2e8f0',
                          borderRadius: '9999px',
                          marginTop: '0.4rem',
                          overflow: 'hidden',
                        }}
                      >
                        <div
                          style={{
                            width: `${Math.min(100, (occupiedCount / 8) * 100)}%`,
                            height: '100%',
                            background: '#2563eb',
                            borderRadius: '9999px',
                          }}
                        ></div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p style={{ color: '#64748b', fontSize: '0.85rem' }}>No zone occupancy data available.</p>
              )}
            </div>
          </div>

          {/* Current Active Parking Sessions */}
          <div className="card">
            <div className="card-header">
              <h3 className="card-title">
                Active Parking Sessions ({stats?.activeSessions ?? 0})
              </h3>
              <Link to="/admin/live" className="btn btn-secondary btn-sm">
                <span>View Full Layout</span>
              </Link>
            </div>
            <div className="table-responsive">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Slot</th>
                    <th>Vehicle</th>
                    <th>Student Driver</th>
                    <th>Entry Time</th>
                    <th>Duration</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {stats?.recentSessions && stats.recentSessions.length > 0 ? (
                    stats.recentSessions.map((session) => (
                      <tr key={session.id}>
                        <td style={{ fontWeight: 700, color: '#2563eb' }}>{session.slotNumber}</td>
                        <td style={{ fontWeight: 600 }}>{session.vehicleRegistrationNumber}</td>
                        <td>
                          <div>{session.userFullName}</div>
                          <div style={{ fontSize: '0.75rem', color: '#64748b' }}>{session.studentId}</div>
                        </td>
                        <td>{new Date(session.entryTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</td>
                        <td>{session.formattedDuration}</td>
                        <td>
                          {session.status === 'ACTIVE' && (
                            <button
                              onClick={() => handleForceExit(session.id)}
                              className="btn btn-danger btn-sm"
                              title="Force Vehicle Exit"
                            >
                              <LogOut size={12} />
                              <span>Exit</span>
                            </button>
                          )}
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="6" style={{ textAlign: 'center', padding: '2rem', color: '#64748b' }}>
                        No active parking sessions in progress.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Recent Registrations */}
        <div className="card">
          <div className="card-header">
            <h3 className="card-title">Recent Student Registrations</h3>
            <Link to="/admin/users" className="btn btn-secondary btn-sm">
              <span>View All Users</span>
            </Link>
          </div>
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Student Name</th>
                  <th>Student ID</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Status</th>
                  <th>Registration Date</th>
                </tr>
              </thead>
              <tbody>
                {stats?.recentRegistrations && stats.recentRegistrations.length > 0 ? (
                  stats.recentRegistrations.map((u) => (
                    <tr key={u.id}>
                      <td style={{ fontWeight: 600 }}>{u.fullName}</td>
                      <td><code>{u.studentId}</code></td>
                      <td>{u.email}</td>
                      <td>{u.phone || '—'}</td>
                      <td>
                        <span className={`badge ${u.enabled ? 'badge-green' : 'badge-red'}`}>
                          {u.enabled ? 'Active' : 'Disabled'}
                        </span>
                      </td>
                      <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', padding: '2rem', color: '#64748b' }}>
                      No student registrations found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
