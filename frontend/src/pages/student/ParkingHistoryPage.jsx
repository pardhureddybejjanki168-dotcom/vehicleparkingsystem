import React, { useState, useEffect } from 'react';
import { useToast } from '../../components/Toast';
import { sessionsApi } from '../../api/sessions';
import Navbar from '../../components/Navbar';
import { Search, Calendar, RefreshCw, ChevronLeft, ChevronRight } from 'lucide-react';

const ParkingHistoryPage = () => {
  const { addToast } = useToast();

  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters & Pagination
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);

  const fetchHistory = async () => {
    setLoading(true);
    try {
      const params = {
        page: currentPage,
        size: 10,
      };
      if (searchTerm) params.search = searchTerm;
      if (statusFilter) params.status = statusFilter;
      if (fromDate) params.fromDate = fromDate;
      if (toDate) params.toDate = toDate;

      const res = await sessionsApi.getHistory(params);
      if (res.success && res.data) {
        setSessions(res.data.content || []);
        setTotalPages(res.data.totalPages || 1);
        setTotalRecords(res.data.totalElements || 0);
      }
    } catch (err) {
      addToast(err.message || 'Failed to fetch parking history', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory();
  }, [currentPage, statusFilter, fromDate, toDate]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchHistory();
  };

  const handleClearFilters = () => {
    setSearchTerm('');
    setStatusFilter('');
    setFromDate('');
    setToDate('');
    setCurrentPage(0);
  };

  return (
    <div>
      <Navbar
        title="My Parking History"
        subtitle="Review your historical entry and exit events, bay durations, and statuses."
        actions={
          <button onClick={fetchHistory} className="btn btn-secondary btn-sm">
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
            <span>Refresh</span>
          </button>
        }
      />

      <div className="page-wrapper">
        <div className="card">
          {/* Filter Bar */}
          <div className="card-header" style={{ flexWrap: 'wrap', gap: '1rem' }}>
            <form onSubmit={handleSearchSubmit} style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  className="form-input"
                  placeholder="Search vehicle or slot..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  style={{ width: '220px', paddingLeft: '2rem' }}
                />
                <Search size={16} color="#94a3b8" style={{ position: 'absolute', left: '0.65rem', top: '50%', transform: 'translateY(-50%)' }} />
              </div>

              <select
                className="form-select"
                value={statusFilter}
                onChange={(e) => {
                  setStatusFilter(e.target.value);
                  setCurrentPage(0);
                }}
                style={{ width: '150px' }}
              >
                <option value="">All Statuses</option>
                <option value="ACTIVE">Active</option>
                <option value="COMPLETED">Completed</option>
                <option value="CANCELLED">Cancelled</option>
              </select>

              <input
                type="date"
                className="form-input"
                value={fromDate}
                onChange={(e) => {
                  setFromDate(e.target.value);
                  setCurrentPage(0);
                }}
                title="From Date"
                style={{ width: '150px' }}
              />

              <input
                type="date"
                className="form-input"
                value={toDate}
                onChange={(e) => {
                  setToDate(e.target.value);
                  setCurrentPage(0);
                }}
                title="To Date"
                style={{ width: '150px' }}
              />

              <button type="submit" className="btn btn-primary btn-sm">
                Search
              </button>
              {(searchTerm || statusFilter || fromDate || toDate) && (
                <button type="button" onClick={handleClearFilters} className="btn btn-secondary btn-sm">
                  Clear
                </button>
              )}
            </form>

            <div style={{ fontSize: '0.85rem', color: '#64748b' }}>
              Total: <strong>{totalRecords}</strong> records
            </div>
          </div>

          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Session Code</th>
                  <th>Vehicle</th>
                  <th>Slot / Location</th>
                  <th>Entry Time</th>
                  <th>Exit Time</th>
                  <th>Duration</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {sessions.length > 0 ? (
                  sessions.map((s) => (
                    <tr key={s.id}>
                      <td><code style={{ fontSize: '0.8rem' }}>{s.sessionCode}</code></td>
                      <td style={{ fontWeight: 600 }}>{s.vehicleRegistrationNumber}</td>
                      <td>
                        <strong>{s.slotNumber}</strong> ({s.zoneName} - {s.zoneFloor})
                      </td>
                      <td>{new Date(s.entryTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}</td>
                      <td>{s.exitTime ? new Date(s.exitTime).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' }) : '—'}</td>
                      <td>{s.formattedDuration}</td>
                      <td>
                        <span className={`badge ${s.status === 'ACTIVE' ? 'badge-green' : 'badge-slate'}`}>
                          {s.status}
                        </span>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="7" style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
                      No parking records matching the selected criteria.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination Controls */}
          {totalPages > 1 && (
            <div
              style={{
                padding: '1rem 1.5rem',
                borderTop: '1px solid #e2e8f0',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
              }}
            >
              <span style={{ fontSize: '0.85rem', color: '#64748b' }}>
                Page {currentPage + 1} of {totalPages}
              </span>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button
                  className="btn btn-secondary btn-sm"
                  disabled={currentPage === 0}
                  onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                >
                  <ChevronLeft size={16} />
                  <span>Previous</span>
                </button>
                <button
                  className="btn btn-secondary btn-sm"
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => setCurrentPage((p) => p + 1)}
                >
                  <span>Next</span>
                  <ChevronRight size={16} />
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ParkingHistoryPage;
