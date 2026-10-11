import React, { useState, useEffect } from 'react';
import { useToast } from '../../components/Toast';
import { adminApi } from '../../api/admin';
import Navbar from '../../components/Navbar';
import Modal from '../../components/Modal';
import { Search, UserCheck, UserX, RefreshCw, ChevronLeft, ChevronRight, Eye } from 'lucide-react';

const UserManagementPage = () => {
  const { addToast } = useToast();

  const [students, setStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  // User Details Modal
  const [selectedUser, setSelectedUser] = useState(null);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const params = {
        page: currentPage,
        size: 10,
      };
      if (searchTerm) params.search = searchTerm;

      const res = await adminApi.getUsers(params);
      if (res.success && res.data) {
        setStudents(res.data.content || []);
        setTotalPages(res.data.totalPages || 1);
        setTotalElements(res.data.totalElements || 0);
      }
    } catch (err) {
      addToast(err.message || 'Failed to fetch students', 'error');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [currentPage]);

  const handleSearch = (e) => {
    e.preventDefault();
    setCurrentPage(0);
    fetchUsers();
  };

  const handleToggleStatus = async (user) => {
    const nextStatus = !user.enabled;
    try {
      const res = await adminApi.toggleUserStatus(user.id, nextStatus);
      addToast(`Student account ${nextStatus ? 'activated' : 'deactivated'}`, 'success');
      fetchUsers();
      if (selectedUser?.id === user.id) {
        setSelectedUser({ ...selectedUser, enabled: nextStatus });
      }
    } catch (err) {
      addToast(err.message || 'Failed to update user status', 'error');
    }
  };

  return (
    <div>
      <Navbar
        title="Student User Management"
        subtitle="Search student records, view profiles, and manage activation privileges."
        actions={
          <button onClick={fetchUsers} className="btn btn-secondary btn-sm">
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
            <span>Refresh</span>
          </button>
        }
      />

      <div className="page-wrapper">
        <div className="card">
          <div className="card-header" style={{ flexWrap: 'wrap', gap: '1rem' }}>
            <form onSubmit={handleSearch} style={{ display: 'flex', gap: '0.75rem' }}>
              <div style={{ position: 'relative' }}>
                <input
                  type="text"
                  className="form-input"
                  placeholder="Search by name, ID, or email..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  style={{ width: '300px', paddingLeft: '2rem' }}
                />
                <Search size={16} color="#94a3b8" style={{ position: 'absolute', left: '0.65rem', top: '50%', transform: 'translateY(-50%)' }} />
              </div>
              <button type="submit" className="btn btn-primary btn-sm">
                Search
              </button>
              {searchTerm && (
                <button
                  type="button"
                  onClick={() => {
                    setSearchTerm('');
                    setCurrentPage(0);
                  }}
                  className="btn btn-secondary btn-sm"
                >
                  Clear
                </button>
              )}
            </form>

            <div style={{ fontSize: '0.85rem', color: '#64748b' }}>
              Total Students: <strong>{totalElements}</strong>
            </div>
          </div>

          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Full Name</th>
                  <th>Student ID</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Status</th>
                  <th>Enrolled Date</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {students.length > 0 ? (
                  students.map((stu) => (
                    <tr key={stu.id}>
                      <td style={{ fontWeight: 600 }}>{stu.fullName}</td>
                      <td><code>{stu.studentId}</code></td>
                      <td>{stu.email}</td>
                      <td>{stu.phone || '—'}</td>
                      <td>
                        <span className={`badge ${stu.enabled ? 'badge-green' : 'badge-red'}`}>
                          {stu.enabled ? 'Active' : 'Deactivated'}
                        </span>
                      </td>
                      <td>{new Date(stu.createdAt).toLocaleDateString()}</td>
                      <td>
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button
                            onClick={() => setSelectedUser(stu)}
                            className="btn btn-secondary btn-sm"
                            title="View Details"
                          >
                            <Eye size={14} />
                            <span>Details</span>
                          </button>
                          <button
                            onClick={() => handleToggleStatus(stu)}
                            className={`btn btn-sm ${stu.enabled ? 'btn-secondary' : 'btn-primary'}`}
                            style={stu.enabled ? { color: '#dc2626' } : {}}
                            title={stu.enabled ? 'Deactivate Account' : 'Activate Account'}
                          >
                            {stu.enabled ? <UserX size={14} /> : <UserCheck size={14} />}
                            <span>{stu.enabled ? 'Deactivate' : 'Activate'}</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="7" style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
                      No students found matching search criteria.
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

      {/* User Details Modal */}
      <Modal
        isOpen={!!selectedUser}
        onClose={() => setSelectedUser(null)}
        title="Student Account Details"
        footer={
          <button className="btn btn-secondary" onClick={() => setSelectedUser(null)}>
            Close
          </button>
        }
      >
        {selectedUser && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div>
              <div className="stat-label">Full Name</div>
              <div style={{ fontSize: '1.1rem', fontWeight: 600 }}>{selectedUser.fullName}</div>
            </div>
            <div>
              <div className="stat-label">College / Student ID</div>
              <div><code>{selectedUser.studentId}</code></div>
            </div>
            <div>
              <div className="stat-label">Email Address</div>
              <div>{selectedUser.email}</div>
            </div>
            <div>
              <div className="stat-label">Phone Number</div>
              <div>{selectedUser.phone || 'Not provided'}</div>
            </div>
            <div>
              <div className="stat-label">Assigned Role</div>
              <div><span className="badge badge-blue">{selectedUser.role}</span></div>
            </div>
            <div>
              <div className="stat-label">Current Status</div>
              <div>
                <span className={`badge ${selectedUser.enabled ? 'badge-green' : 'badge-red'}`}>
                  {selectedUser.enabled ? 'Active Account' : 'Deactivated'}
                </span>
              </div>
            </div>
            <div>
              <div className="stat-label">Account Created</div>
              <div>{new Date(selectedUser.createdAt).toLocaleString()}</div>
            </div>
            <div style={{ fontSize: '0.75rem', color: '#94a3b8', fontStyle: 'italic', borderTop: '1px solid #f1f5f9', paddingTop: '0.75rem' }}>
              Notice: Passwords and cryptographic password hashes are strictly redacted from administrative views.
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default UserManagementPage;
