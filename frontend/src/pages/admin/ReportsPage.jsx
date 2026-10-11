import React, { useState, useEffect } from 'react';
import { useToast } from '../../components/Toast';
import { adminApi } from '../../api/admin';
import Navbar from '../../components/Navbar';
import StatCard from '../../components/StatCard';
import {
  Download,
  Calendar,
  Search,
  RefreshCw,
  FileSpreadsheet,
  CheckCircle2,
  Clock,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react';

const ReportsPage = () => {
  const { addToast } = useToast();

  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);

  // Date filters
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalRecords, setTotalRecords] = useState(0);

  const fetchSessions = async () => {
    setLoading(true);
    try {
      const params = {
        page: currentPage,
        size: 15,
      };
      if (fromDate) params.fromDate = fromDate;
      if (toDate) params.toDate = toDate;
      if (statusFilter) params.status = statusFilter;
      if (searchTerm) params.search = searchTerm;

      const res = await adminApi.getCampusHistory(params);
      if (res.success && res.data) {
        setSessions(res.data.content || []);
        setTotalPages(res.data.totalPages || 1);
        setTotalRecords(res.data.totalElements || 0);
      }
    } catch (err) {
      addToast(err.message || 'Failed to fetch parking reports', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSessions();
  }, [currentPage, fromDate, toDate, statusFilter]);

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchSessions();
  };

  const handleDownloadCsv = () => {
    const token = localStorage.getItem('smartpark_token');
    const url = adminApi.exportCsvUrl(fromDate, toDate);

    // Trigger authenticated download via fetch blob
    fetch(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => {
        if (!res.ok) throw new Error('Failed to download CSV');
        return res.blob();
      })
      .then((blob) => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = `smartpark-parking-report-${new Date().toISOString().slice(0, 10)}.csv`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        addToast('CSV Report exported successfully!', 'success');
      })
      .catch((err) => {
        addToast(err.message || 'Error exporting CSV', 'error');
      });
  };

  return (
    <div>
      <Navbar
        title="Parking Activity Reports & Analytics"
        subtitle="Filter campus parking history by date range and export authoritative CSV records."
        actions={
          <button onClick={handleDownloadCsv} className="btn btn-primary btn-sm">
            <Download size={16} />
            <span>Export to CSV</span>
          </button>
        }
      />

      <div className="page-wrapper">
        <div className="card">
          <div className="card-header" style={{ flexWrap: 'wrap', gap: '1rem' }}>
            <form onSubmit={handleSearch} style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  className="form-input"
                  placeholder="Search student, vehicle, slot..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  style={{ width: '240px', paddingLeft: '2rem' }}
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

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <span style={{ fontSize: '0.8rem', color: '#64748b' }}>From:</span>
                <input
                  type="date"
                  className="form-input"
                  value={fromDate}
                  onChange={(e) => {
                    setFromDate(e.target.value);
                    setCurrentPage(0);
                  }}
                  style={{ width: '140px' }}
                />
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <span style={{ fontSize: '0.8rem', color: '#64748b' }}>To:</span>
                <input
                  type="date"
                  className="form-input"
                  value={toDate}
                  onChange={(e) => {
                    setToDate(e.target.value);
                    setCurrentPage(0);
                  }}
                  style={{ width: '140px' }}
                />
              </div>

              <button type="submit" className="btn btn-primary btn-sm">
                Apply Filters
              </button>
              {(searchTerm || statusFilter || fromDate || toDate) && (
                <button
                  type="button"
                  onClick={() => {
                    setSearchTerm('');
                    setStatusFilter('');
                    setFromDate('');
                    setToDate('');
                    setCurrentPage(0);
                  }}
                  className="btn btn-secondary btn-sm"
                >
                  Reset
                </button>
              )}
            </form>

            <div style={{ fontSize: '0.85rem', color: '#64748b' }}>
              Records found: <strong>{totalRecords}</strong>
            </div>
          </div>

          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Session Reference</th>
                  <th>Student Driver</th>
                  <th>Student ID</th>
                  <th>Vehicle Plate</th>
                  <th>Bay / Zone</th>
                  <th>Entry Timestamp</th>
                  <th>Exit Timestamp</th>
                  <th>Duration</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {sessions.length > 0 ? (
                  sessions.map((s) => (
                    <tr key={s.id}>
                      <td><code style={{ fontSize: '0.8rem' }}>{s.sessionCode}</code></td>
                      <td style={{ fontWeight: 600 }}>{s.userFullName}</td>
                      <td><code>{s.studentId}</code></td>
                      <td><strong>{s.vehicleRegistrationNumber}</strong></td>
                      <td>{s.slotNumber} ({s.zoneFloor})</td>
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
                    <td colSpan="9" style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
                      No parking records found matching the specified date range.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

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

export default ReportsPage;
