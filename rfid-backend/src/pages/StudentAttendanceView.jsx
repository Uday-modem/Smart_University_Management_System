import React, { useState, useEffect, useContext, useMemo } from 'react';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './StudentAttendanceView.css';

const API_BASE_URL = 'http://localhost:8080/api';

const StudentAttendanceView = () => {
  const { user, token } = useContext(AuthContext);
  const navigate = useNavigate();
  
  const [liveSummary, setLiveSummary] = useState(null);
  const [selectedMonth, setSelectedMonth] = useState('');
  const [monthlyData, setMonthlyData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const authAxios = useMemo(() => {
    return axios.create({
      baseURL: API_BASE_URL,
      headers: { Authorization: `Bearer ${token}` }
    });
  }, [token]);

  // Fetch live attendance summary on component mount
  useEffect(() => {
    const fetchLiveSummary = async () => {
      setLoading(true);
      try {
        console.log('📌 Fetching live attendance summary...');
        const response = await authAxios.get('/attendance/student/live-summary');
        console.log('✅ Live summary:', response.data);
        setLiveSummary(response.data);
      } catch (err) {
        console.error('❌ Error fetching live summary:', err);
        setError('Failed to load live attendance data');
      } finally {
        setLoading(false);
      }
    };

    if (token) {
      fetchLiveSummary();
    }
  }, [authAxios, token]);

  // Fetch monthly attendance when month is selected
  useEffect(() => {
    if (selectedMonth) {
      const fetchMonthlyData = async () => {
        setLoading(true);
        try {
          const response = await authAxios.get(`/attendance/student/month/${selectedMonth}`);
          setMonthlyData(response.data);
        } catch (err) {
          console.error('Error fetching monthly data:', err);
          setError('Failed to load monthly attendance');
        } finally {
          setLoading(false);
        }
      };
      fetchMonthlyData();
    }
  }, [selectedMonth, authAxios]);

  const getStatusColor = (status) => {
    const colors = {
      'NORMAL': '#4caf50',
      'CONDONATION': '#ff9800',
      'DETAINED': '#f44336'
    };
    return colors[status] || '#999';
  };

  const getStatusIcon = (status) => {
    const icons = {
      'NORMAL': '✓',
      'CONDONATION': '⚠',
      'DETAINED': '✗'
    };
    return icons[status] || '?';
  };

  return (
    <div className="student-attendance-container">
      {/* Header */}
      <div className="attendance-header">
        <button onClick={() => navigate('/student/dashboard')} className="back-button-attendance">
          ← Back to Dashboard
        </button>
        <h1>My Attendance</h1>
      </div>

      {error && <div className="error-box">❌ {error}</div>}
      {loading && <div className="loading-box">Loading attendance data...</div>}

      {/* Live Attendance Summary Card - Like Screenshot */}
      {liveSummary && (
        <div className="live-attendance-card">
          <div className="card-header">
            <h2>Attendance</h2>
            <div className="help-icon" title="Live attendance tracking">
              <span>?</span>
            </div>
          </div>

          {/* Subject Name (Semester) */}
          <div className="subject-name">{liveSummary.semesterName}</div>

          {/* Progress Bar */}
          <div className="attendance-progress-section">
            <div className="progress-bar-container">
              <div 
                className="progress-bar-fill" 
                style={{ 
                  width: `${liveSummary.percentage || 0}%`,
                  backgroundColor: getStatusColor(liveSummary.status)
                }}
              ></div>
            </div>
            <div className="progress-labels">
              <span>0%</span>
              <span>100%</span>
            </div>
          </div>

          {/* Percentage Display */}
          <div 
            className="percentage-display"
            style={{ color: getStatusColor(liveSummary.status) }}
          >
            {/* ✅ FIXED: Safely handle null/undefined percentage */}
            {typeof liveSummary.percentage === 'number' ? liveSummary.percentage.toFixed(1) : '0.0'}%
          </div>

          {/* Stats Cards */}
          <div className="stats-cards">
            <div className="stat-card">
              <div className="stat-label">Total</div>
              <div className="stat-value">{liveSummary.totalClasses || 0}</div>
            </div>
            <div className="stat-card stat-present">
              <div className="stat-label">Present</div>
              <div className="stat-value">{liveSummary.attendedClasses || 0}</div>
            </div>
            <div className="stat-card stat-absent">
              <div className="stat-label">Absent</div>
              <div className="stat-value">{liveSummary.absentClasses || 0}</div>
            </div>
          </div>

          {/* Status Badge */}
          <div className="status-section">
            <div 
              className="status-badge-large"
              style={{ 
                backgroundColor: getStatusColor(liveSummary.status),
                color: 'white'
              }}
            >
              {getStatusIcon(liveSummary.status)} {liveSummary.status}
            </div>
            
            {liveSummary.status === 'DETAINED' && (
              <p className="status-message detained">
                ⚠️ Your attendance is below 65%. You are detained for this semester.
              </p>
            )}
            {liveSummary.status === 'CONDONATION' && (
              <p className="status-message condonation">
                ⚠️ Your attendance is between 65-75%. Condonation may be required.
              </p>
            )}
            {liveSummary.status === 'NORMAL' && (
              <p className="status-message normal">
                ✅ Your attendance is above 75%. Keep it up!
              </p>
            )}
          </div>

          {/* Semester Info */}
          <div className="semester-info">
            <div className="info-row">
              <span className="info-label">Semester Period:</span>
              <span className="info-value">
                {liveSummary.semesterStart ? new Date(liveSummary.semesterStart).toLocaleDateString() : 'N/A'} - {liveSummary.semesterEnd ? new Date(liveSummary.semesterEnd).toLocaleDateString() : 'N/A'}
              </span>
            </div>
            <div className="info-row">
              <span className="info-label">Calculated Until:</span>
              <span className="info-value">
                {liveSummary.calculationDate ? new Date(liveSummary.calculationDate).toLocaleDateString() : 'N/A'}
              </span>
            </div>
            <div className="info-row">
              <span className="info-label">Semester Status:</span>
              <span className={`info-value ${liveSummary.semesterActive ? 'active' : 'inactive'}`}>
                {liveSummary.semesterActive ? '🟢 Active' : '🔴 Ended'}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Monthly View Section */}
      <div className="monthly-view-section">
        <h3>View Monthly Attendance</h3>
        <div className="month-selector">
          <label>Select Month:</label>
          <input
            type="month"
            value={selectedMonth}
            onChange={(e) => setSelectedMonth(e.target.value)}
            className="month-input"
          />
        </div>

        {monthlyData && (
          <div className="monthly-details">
            <h4>Attendance for {selectedMonth}</h4>
            <div className="monthly-stats">
              <div className="monthly-stat">
                <span className="monthly-stat-label">Total Days:</span>
                <span className="monthly-stat-value">{monthlyData.totalDays}</span>
              </div>
              <div className="monthly-stat">
                <span className="monthly-stat-label">Present:</span>
                <span className="monthly-stat-value present">{monthlyData.presentCount}</span>
              </div>
              <div className="monthly-stat">
                <span className="monthly-stat-label">Absent:</span>
                <span className="monthly-stat-value absent">{monthlyData.absentCount}</span>
              </div>
            </div>

            {/* Daily Records */}
            <div className="daily-records">
              <h5>Daily Records:</h5>
              <div className="records-grid">
                {monthlyData.records.map((record, index) => (
                  <div key={index} className={`record-item ${record.status.toLowerCase()}`}>
                    <div className="record-date">{new Date(record.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</div>
                    <div className="record-status">{record.status === 'PRESENT' ? '✓' : '✗'}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default StudentAttendanceView;
