import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../components/Toast';
import { usersApi } from '../../api/users';
import Navbar from '../../components/Navbar';
import { User, Mail, Hash, Phone, Shield, CalendarCheck } from 'lucide-react';

const ProfilePage = () => {
  const { user: authUser } = useAuth();
  const { addToast } = useToast();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await usersApi.getProfile();
        if (res.success) {
          setProfile(res.data);
        }
      } catch (err) {
        addToast(err.message || 'Failed to load profile', 'error');
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  const data = profile || authUser;

  return (
    <div>
      <Navbar
        title="Student Profile"
        subtitle="Manage your personal account credentials and college student identity."
      />

      <div className="page-wrapper">
        <div className="card" style={{ maxWidth: '720px' }}>
          <div className="card-header">
            <h3 className="card-title">Identity & Account Details</h3>
            <span className="badge badge-green">Account Active</span>
          </div>

          <div className="card-body">
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
              <div>
                <div className="stat-label" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <User size={14} />
                  <span>Full Legal Name</span>
                </div>
                <div style={{ fontSize: '1.1rem', fontWeight: 600, color: '#0f172a', marginTop: '0.2rem' }}>
                  {data?.fullName || '—'}
                </div>
              </div>

              <div>
                <div className="stat-label" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <Hash size={14} />
                  <span>Student ID / College ID</span>
                </div>
                <div style={{ fontSize: '1.1rem', fontWeight: 600, color: '#0f172a', marginTop: '0.2rem' }}>
                  <code>{data?.studentId || '—'}</code>
                </div>
              </div>

              <div>
                <div className="stat-label" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <Mail size={14} />
                  <span>Registered Email</span>
                </div>
                <div style={{ fontSize: '1rem', fontWeight: 500, color: '#0f172a', marginTop: '0.2rem' }}>
                  {data?.email || '—'}
                </div>
              </div>

              <div>
                <div className="stat-label" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <Phone size={14} />
                  <span>Phone Number</span>
                </div>
                <div style={{ fontSize: '1rem', fontWeight: 500, color: '#0f172a', marginTop: '0.2rem' }}>
                  {data?.phone || 'Not provided'}
                </div>
              </div>

              <div>
                <div className="stat-label" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <Shield size={14} />
                  <span>System Role</span>
                </div>
                <div style={{ marginTop: '0.2rem' }}>
                  <span className="badge badge-blue">{data?.role || 'STUDENT'}</span>
                </div>
              </div>

              <div>
                <div className="stat-label" style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                  <CalendarCheck size={14} />
                  <span>Account Created</span>
                </div>
                <div style={{ fontSize: '1rem', fontWeight: 500, color: '#0f172a', marginTop: '0.2rem' }}>
                  {data?.createdAt ? new Date(data.createdAt).toLocaleDateString() : '—'}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
