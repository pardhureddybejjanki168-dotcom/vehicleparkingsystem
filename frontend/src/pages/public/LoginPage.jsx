import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../components/Toast';
import { ParkingSquare, Eye, EyeOff, Loader2 } from 'lucide-react';

const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const { login } = useAuth();
  const { addToast } = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (!username.trim() || !password) {
      setError('Please provide both username/email and password.');
      return;
    }

    setLoading(true);
    try {
      const user = await login({ username: username.trim(), password });
      addToast(`Welcome back, ${user.fullName}!`, 'success');

      if (user.role === 'ADMIN') {
        navigate('/admin/dashboard', { replace: true });
      } else {
        navigate('/dashboard', { replace: true });
      }
    } catch (err) {
      setError(err.message || 'Invalid credentials provided');
      addToast(err.message || 'Login failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handlePrefillAdmin = () => {
    setUsername('admin@smartpark.edu');
    setPassword('Admin@123456');
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-header">
          <div className="auth-logo">
            <ParkingSquare size={32} />
          </div>
          <h2 className="auth-title">Sign In to SmartPark</h2>
          <p className="auth-subtitle">University Campus Parking Management Portal</p>
        </div>

        {error && (
          <div
            style={{
              backgroundColor: '#fef2f2',
              color: '#991b1b',
              border: '1px solid #fecaca',
              padding: '0.75rem 1rem',
              borderRadius: '6px',
              fontSize: '0.85rem',
              marginBottom: '1.25rem',
            }}
          >
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="username">
              Email Address or Student ID
            </label>
            <input
              id="username"
              type="text"
              className="form-input"
              placeholder="e.g. student@campus.edu or STU-1001"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              disabled={loading}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="password">
              Password
            </label>
            <div style={{ position: 'relative' }}>
              <input
                id="password"
                type={showPassword ? 'text' : 'password'}
                className="form-input"
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading}
                required
                style={{ paddingRight: '2.5rem' }}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  position: 'absolute',
                  right: '0.75rem',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  background: 'none',
                  border: 'none',
                  color: '#64748b',
                  cursor: 'pointer',
                  padding: 0,
                }}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <button
            type="submit"
            className="btn btn-primary"
            style={{ width: '100%', marginTop: '0.5rem', height: '44px' }}
            disabled={loading}
          >
            {loading ? (
              <>
                <Loader2 size={18} className="animate-spin" />
                <span>Authenticating...</span>
              </>
            ) : (
              <span>Sign In</span>
            )}
          </button>
        </form>

        <div style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.875rem', color: '#64748b' }}>
          Don't have an account?{' '}
          <Link to="/register" style={{ color: '#2563eb', fontWeight: 600, textDecoration: 'none' }}>
            Register as a Student
          </Link>
        </div>

        {/* Demo Fast Login Helper */}
        <div
          style={{
            marginTop: '1.75rem',
            padding: '0.85rem',
            background: '#f8fafc',
            borderRadius: '8px',
            border: '1px dashed #cbd5e1',
            fontSize: '0.75rem',
            color: '#64748b',
          }}
        >
          <div style={{ fontWeight: 600, color: '#334155', marginBottom: '0.35rem' }}>Initial Admin Account:</div>
          <div>Email: <code>admin@smartpark.edu</code> | Pass: <code>Admin@123456</code></div>
          <button
            type="button"
            onClick={handlePrefillAdmin}
            style={{
              marginTop: '0.4rem',
              background: 'none',
              border: 'none',
              color: '#2563eb',
              textDecoration: 'underline',
              cursor: 'pointer',
              fontSize: '0.75rem',
              padding: 0,
            }}
          >
            Auto-fill Admin Credentials
          </button>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
