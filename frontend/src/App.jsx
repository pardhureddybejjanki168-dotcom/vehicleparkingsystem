import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './components/Toast';
import Sidebar from './components/Sidebar';

// Public Pages
import LoginPage from './pages/public/LoginPage';
import RegisterPage from './pages/public/RegisterPage';

// Student Pages
import StudentDashboard from './pages/student/StudentDashboard';
import LiveSlotsPage from './pages/student/LiveSlotsPage';
import VehiclesPage from './pages/student/VehiclesPage';
import ParkingHistoryPage from './pages/student/ParkingHistoryPage';
import ProfilePage from './pages/student/ProfilePage';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import UserManagementPage from './pages/admin/UserManagementPage';
import SlotManagementPage from './pages/admin/SlotManagementPage';
import LiveOperationsPage from './pages/admin/LiveOperationsPage';
import ReportsPage from './pages/admin/ReportsPage';

const ProtectedLayout = () => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div style={{ display: 'flex', height: '100vh', alignItems: 'center', justifyContent: 'center' }}>
        <p>Initializing SmartPark portal...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="app-container">
      <Sidebar />
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
};

const AdminRoute = () => {
  const { isAdmin, loading } = useAuth();
  if (loading) return null;
  return isAdmin ? <Outlet /> : <Navigate to="/dashboard" replace />;
};

const StudentRoute = () => {
  const { isAdmin, loading } = useAuth();
  if (loading) return null;
  return !isAdmin ? <Outlet /> : <Navigate to="/admin/dashboard" replace />;
};

const RootRedirect = () => {
  const { isAuthenticated, isAdmin, loading } = useAuth();
  if (loading) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return isAdmin ? <Navigate to="/admin/dashboard" replace /> : <Navigate to="/dashboard" replace />;
};

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            {/* Public */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/" element={<RootRedirect />} />

            {/* Protected Shell */}
            <Route element={<ProtectedLayout />}>
              {/* Student Routes */}
              <Route element={<StudentRoute />}>
                <Route path="/dashboard" element={<StudentDashboard />} />
                <Route path="/slots" element={<LiveSlotsPage />} />
                <Route path="/vehicles" element={<VehiclesPage />} />
                <Route path="/history" element={<ParkingHistoryPage />} />
                <Route path="/profile" element={<ProfilePage />} />
              </Route>

              {/* Admin Routes */}
              <Route path="/admin" element={<AdminRoute />}>
                <Route path="dashboard" element={<AdminDashboard />} />
                <Route path="live" element={<LiveOperationsPage />} />
                <Route path="users" element={<UserManagementPage />} />
                <Route path="slots" element={<SlotManagementPage />} />
                <Route path="history" element={<ReportsPage />} />
                <Route path="reports" element={<ReportsPage />} />
              </Route>
            </Route>

            {/* Catch-all */}
            <Route path="*" element={<RootRedirect />} />
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
