import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  LayoutDashboard,
  MapPin,
  Car,
  Clock,
  User,
  Users,
  Sliders,
  FileSpreadsheet,
  LogOut,
  ParkingSquare,
  ShieldCheck,
} from 'lucide-react';

const Sidebar = () => {
  const { user, isAdmin, logout } = useAuth();

  return (
    <aside className="sidebar">
      <div className="sidebar-header">
        <div className="brand-icon">
          <ParkingSquare size={24} />
        </div>
        <div>
          <h1 className="brand-title">SmartPark</h1>
          <span className="brand-subtitle">{isAdmin ? 'Admin Portal' : 'Student Portal'}</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        {isAdmin ? (
          <>
            <NavLink
              to="/admin/dashboard"
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <LayoutDashboard size={18} />
              <span>Admin Dashboard</span>
            </NavLink>
            <NavLink
              to="/admin/live"
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <MapPin size={18} />
              <span>Live Operations</span>
            </NavLink>
            <NavLink
              to="/admin/users"
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <Users size={18} />
              <span>User Management</span>
            </NavLink>
            <NavLink
              to="/admin/slots"
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <Sliders size={18} />
              <span>Slot Management</span>
            </NavLink>
            <NavLink
              to="/admin/history"
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <Clock size={18} />
              <span>Campus Sessions</span>
            </NavLink>
            <NavLink
              to="/admin/reports"
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <FileSpreadsheet size={18} />
              <span>Reports & Export</span>
            </NavLink>
          </>
        ) : (
          <>
            <NavLink
              to="/dashboard"
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <LayoutDashboard size={18} />
              <span>Dashboard</span>
            </NavLink>
            <NavLink
              to="/slots"
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <MapPin size={18} />
              <span>Live Parking Slots</span>
            </NavLink>
            <NavLink
              to="/vehicles"
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <Car size={18} />
              <span>My Vehicles</span>
            </NavLink>
            <NavLink
              to="/history"
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <Clock size={18} />
              <span>My Parking History</span>
            </NavLink>
            <NavLink
              to="/profile"
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
            >
              <User size={18} />
              <span>Profile</span>
            </NavLink>
          </>
        )}
      </nav>

      <div className="sidebar-footer">
        <div className="user-snippet">
          <div className="user-avatar">
            {isAdmin ? <ShieldCheck size={18} /> : (user?.fullName?.charAt(0) || 'U')}
          </div>
          <div style={{ overflow: 'hidden' }}>
            <div className="user-info-name">{user?.fullName || 'User'}</div>
            <div className="user-info-role">
              {isAdmin ? 'System Administrator' : `ID: ${user?.studentId || ''}`}
            </div>
          </div>
        </div>
        <button onClick={logout} className="btn-logout" title="Sign out">
          <LogOut size={16} />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
