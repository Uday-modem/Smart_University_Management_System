import React from 'react';
import './LateAlertPopup.css';

/**
 * LateAlertPopup Component
 * 
 * Displays a dismissible popup notification for staff late alerts in admin dashboard.
 * Shows staff name, ID, room, time slot, and minutes late.
 * Admin can close the popup by clicking the X button, which acknowledges the alert.
 * 
 * Props:
 * @param {Object} alert - StaffLateAlert object containing alert details
 * @param {Function} onClose - Callback function when admin clicks X button
 */
const LateAlertPopup = ({ alert, onClose }) => {
  if (!alert) return null;

  const formatTime = (timeString) => {
    if (!timeString) return 'N/A';
    try {
      // Handle LocalTime format (HH:mm:ss) or similar
      return timeString.substring(0, 5); // Show HH:mm only
    } catch (e) {
      return timeString;
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('en-US', { 
        weekday: 'short', 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric' 
      });
    } catch (e) {
      return dateString;
    }
  };

  return (
    <div className="late-alert-popup">
      <div className="late-alert-header">
        <div className="late-alert-title">
          <span className="late-alert-icon">⚠️</span>
          <span className="late-alert-title-text">Staff Late Alert</span>
        </div>
        <button 
          className="late-alert-close-btn"
          onClick={onClose}
          aria-label="Close alert"
        >
          ×
        </button>
      </div>
      
      <div className="late-alert-content">
        <div className="late-alert-field">
          <span className="late-alert-label">Staff Name:</span>
          <span className="late-alert-value">{alert.staffName || 'N/A'}</span>
        </div>
        
        <div className="late-alert-field">
          <span className="late-alert-label">Staff ID:</span>
          <span className="late-alert-value">{alert.staffIdNumber || 'N/A'}</span>
        </div>
        
        <div className="late-alert-field">
          <span className="late-alert-label">Room:</span>
          <span className="late-alert-value">{alert.roomNumber || 'N/A'}</span>
        </div>
        
        <div className="late-alert-field">
          <span className="late-alert-label">Time Slot:</span>
          <span className="late-alert-value">{alert.timeSlot || 'N/A'}</span>
        </div>
        
        <div className="late-alert-field">
          <span className="late-alert-label">Scheduled Time:</span>
          <span className="late-alert-value">
            {formatTime(alert.scheduledTime)}
          </span>
        </div>
        
        <div className="late-alert-field">
          <span className="late-alert-label">Actual Entry:</span>
          <span className="late-alert-value late-alert-late">
            {formatTime(alert.actualEntryTime)}
          </span>
        </div>
        
        <div className="late-alert-field late-alert-highlight">
          <span className="late-alert-label">Minutes Late:</span>
          <span className="late-alert-value late-alert-late-bold">
            {alert.minutesLate || 0} minutes
          </span>
        </div>
        
        <div className="late-alert-field">
          <span className="late-alert-label">Date:</span>
          <span className="late-alert-value">{formatDate(alert.alertDate)}</span>
        </div>
      </div>
      
      <div className="late-alert-footer">
        <span className="late-alert-note">
          Email notifications have been sent to staff and admin.
        </span>
      </div>
    </div>
  );
};

export default LateAlertPopup;

