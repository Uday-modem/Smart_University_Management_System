import React, { useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const handleContactAdmin = () => {
    alert('Contact Admin feature - Will be implemented');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        Smart University Management System
      </div>
      
      <div className="navbar-menu">
        <button className="nav-item active">Home</button>
        <button className="nav-item">Timetable</button>
        <button className="nav-item">Marks</button>
        <button className="nav-item">Complaint Box</button>
      </div>

      <div className="navbar-right">
        <button className="contact-admin-btn" onClick={handleContactAdmin}>
          📞 Contact Admin
        </button>
        
        <div className="profile-section">
          <div className="profile-avatar">
            {user?.email?.charAt(0).toUpperCase() || 'U'}
          </div>
          <div className="profile-dropdown">
            <div className="profile-info">
              <p className="profile-name">{user?.name || 'User'}</p>
              <p className="profile-email">{user?.email || 'N/A'}</p>
            </div>
            <button className="logout-btn" onClick={handleLogout}>
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
