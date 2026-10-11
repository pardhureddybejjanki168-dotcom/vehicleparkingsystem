import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Bell, Shield } from 'lucide-react';

const Navbar = ({ title, subtitle, actions }) => {
  const { user, isAdmin } = useAuth();

  return (
    <header className="top-navbar">
      <div>
        <h2 className="page-header-title">{title}</h2>
        {subtitle && <p className="page-header-desc">{subtitle}</p>}
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {actions}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: '#f1f5f9', padding: '0.4rem 0.85rem', borderRadius: '9999px', fontSize: '0.8rem', fontWeight: 600 }}>
          <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: '#10b981' }}></span>
          <span>System Online</span>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
