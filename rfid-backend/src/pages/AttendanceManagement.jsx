import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import StudentAttendance from './StudentAttendance';
import StaffAttendance from './StaffAttendance';
import './AttendanceManagement.css';

const AttendanceManagement = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('student');

  return (
    <div className="attendance-management-container">
      <button onClick={() => navigate('/admin/dashboard')} className="back-button">
        ← Back to Dashboard
      </button>

      <h2>Attendance Management</h2>

      <div className="tab-buttons">
        <button 
          className={`tab-button ${activeTab === 'student' ? 'active' : ''}`}
          onClick={() => setActiveTab('student')}
        >
          📚 Student Attendance
        </button>
        <button 
          className={`tab-button ${activeTab === 'staff' ? 'active' : ''}`}
          onClick={() => setActiveTab('staff')}
        >
          👨‍🏫 Staff Attendance
        </button>
      </div>

      <div className="tab-content">
        {activeTab === 'student' ? <StudentAttendance /> : <StaffAttendance />}
      </div>
    </div>
  );
};

export default AttendanceManagement;
