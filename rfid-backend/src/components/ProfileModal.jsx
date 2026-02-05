import React, { useState } from 'react';
import './ProfileModal.css';

const ProfileModal = ({ user, onLogout, onClose }) => {
  const [showDetails, setShowDetails] = useState(false);

  return (
    <div className="profile-modal-container">
      {!showDetails ? (
        <div className="profile-menu">
          <button 
            className="profile-menu-btn view-profile-btn"
            onClick={() => setShowDetails(true)}
          >
            View Profile
          </button>
          <button 
            className="profile-menu-btn logout-btn"
            onClick={onLogout}
          >
            Logout
          </button>
        </div>
      ) : (
        <div className="profile-details-container">
          <h3 className="profile-details-title">Student Profile</h3>
          <div className="profile-details">
            <div className="profile-detail-row">
              <span className="profile-detail-label">Name:</span>
              <span className="profile-detail-value">{user?.name || 'N/A'}</span>
            </div>
            <div className="profile-detail-row">
              <span className="profile-detail-label">Registration Number:</span>
              <span className="profile-detail-value">{user?.registrationNumber || 'N/A'}</span>
            </div>
            <div className="profile-detail-row">
              <span className="profile-detail-label">Email:</span>
              <span className="profile-detail-value">{user?.email || 'N/A'}</span>
            </div>
            <div className="profile-detail-row">
              <span className="profile-detail-label">Branch:</span>
              <span className="profile-detail-value">{user?.branch || 'N/A'}</span>
            </div>
            <div className="profile-detail-row">
              <span className="profile-detail-label">Year:</span>
              <span className="profile-detail-value">{user?.year || 'N/A'}</span>
            </div>
            <div className="profile-detail-row">
              <span className="profile-detail-label">Semester:</span>
              <span className="profile-detail-value">{user?.semester || 'N/A'}</span>
            </div>
          </div>
          <button 
            className="profile-menu-btn back-btn"
            onClick={() => setShowDetails(false)}
          >
            Back
          </button>
          <button 
            className="profile-menu-btn logout-btn"
            onClick={onLogout}
          >
            Logout
          </button>
        </div>
      )}
    </div>
  );
};

export default ProfileModal;
