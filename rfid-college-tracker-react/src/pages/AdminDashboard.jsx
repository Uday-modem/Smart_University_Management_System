import React, { useContext, useState, useEffect } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import LateAlertPopup from '../components/Common/LateAlertPopup';

const API_BASE_URL = 'http://localhost:8080/api';

const AdminDashboard = () => {
  const { user, token, logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  
  // ✅ NEW: Complaint Stats State
  const [complaintStats, setComplaintStats] = useState({
    total: 0,
    pending: 0,
    resolved: 0
  });
  const [loadingStats, setLoadingStats] = useState(false);

  // ✅ NEW: Staff Late Alerts State
  const [lateAlerts, setLateAlerts] = useState([]);
  const [loadingAlerts, setLoadingAlerts] = useState(false);
  const [showAlertDropdown, setShowAlertDropdown] = useState(false); // ✅ NEW: Alert dropdown state

  if (!user) return <div>Loading...</div>;

  // ✅ NEW: Fetch Complaint Statistics
  useEffect(() => {
    const fetchComplaintStats = async () => {
      if (!token) return;

      setLoadingStats(true);
      try {
        const response = await axios.get(`${API_BASE_URL}/complaints/stats`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        console.log('✅ Complaint stats:', response.data);
        setComplaintStats(response.data);
      } catch (err) {
        console.error('❌ Error fetching complaint stats:', err);
      } finally {
        setLoadingStats(false);
      }
    };

    fetchComplaintStats();
  }, [token]);

  // ✅ NEW: Fetch Staff Late Alerts
  useEffect(() => {
    const fetchLateAlerts = async () => {
      if (!token) return;

      setLoadingAlerts(true);
      try {
        const response = await axios.get(`${API_BASE_URL}/admin/alerts`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        console.log('✅ Late alerts fetched:', response.data);
        setLateAlerts(response.data || []);
      } catch (err) {
        console.error('❌ Error fetching late alerts:', err);
        setLateAlerts([]);
      } finally {
        setLoadingAlerts(false);
      }
    };

    // Fetch immediately on mount
    fetchLateAlerts();

    // Poll every 30 seconds for new alerts
    const interval = setInterval(fetchLateAlerts, 30000);

    return () => clearInterval(interval);
  }, [token]);

  // ✅ NEW: Handle closing/acknowledging an alert
  const handleCloseAlert = async (alertId) => {
    if (!token) return;

    try {
      await axios.delete(
        `${API_BASE_URL}/admin/alerts/${alertId}`,
        {
          headers: { Authorization: `Bearer ${token}` }
        }
      );
      
      // Remove the acknowledged alert from state
      setLateAlerts((prevAlerts) => 
        prevAlerts.filter((alert) => alert.id !== alertId)
      );
      
      console.log('✅ Alert dismissed:', alertId);
    } catch (err) {
      console.error('❌ Error dismissing alert:', err);
      // Still remove from UI even if API call fails (optimistic update)
      setLateAlerts((prevAlerts) => 
        prevAlerts.filter((alert) => alert.id !== alertId)
      );
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const handleManageTimetable = () => {
    navigate('/admin/timetable');
  };

  const handleManageStaff = () => {
    navigate('/admin/staff');
  };

  const handleManageAttendance = () => {
    navigate('/admin/attendance');
  };

  const handleManageMarks = () => {
    navigate('/admin/marks');
  };

  const handleSemesterConfig = () => {
    navigate('/admin/semester-config');
  };

  // ✅ NEW: Handle View Complaints
  const handleViewComplaints = () => {
    navigate('/admin/complaints');
  };

  return (
    <div style={{ minHeight: '100vh', background: '#f5f7fa', fontFamily: 'Segoe UI, sans-serif' }}>
      {/* ✅ NEW: Staff Late Alert Popup */}
      {lateAlerts.length > 0 && (
        <LateAlertPopup 
          alert={lateAlerts[0]} 
          onClose={() => handleCloseAlert(lateAlerts[0].id)} 
        />
      )}

      {/* Top Navigation Bar */}
      <nav style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '16px 30px',
        background: '#fff',
        boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
        position: 'sticky',
        top: 0,
        zIndex: 100
      }}>
        <h1 style={{ margin: 0, fontSize: '20px', fontWeight: 600, color: '#764ba2' }}>
          Admin Panel
        </h1>

        <div style={{ display: 'flex', gap: '30px', alignItems: 'center' }}>
          <button onClick={() => navigate('/admin/dashboard')} style={{
            background: 'none',
            border: 'none',
            fontSize: '15px',
            color: '#764ba2',
            cursor: 'pointer',
            fontWeight: 500,
            borderBottom: '3px solid #764ba2',
            paddingBottom: '4px'
          }}>
            Dashboard
          </button>
          
          <button onClick={handleManageTimetable} style={{
            background: 'none',
            border: 'none',
            fontSize: '15px',
            color: '#666',
            cursor: 'pointer'
          }}>
            Manage Timetable
          </button>

          <button onClick={() => alert('Manage Users')} style={{
            background: 'none',
            border: 'none',
            fontSize: '15px',
            color: '#666',
            cursor: 'pointer'
          }}>
            Users
          </button>
          
          <button onClick={() => alert('Reports')} style={{
            background: 'none',
            border: 'none',
            fontSize: '15px',
            color: '#666',
            cursor: 'pointer'
          }}>
            Reports
          </button>

          <button onClick={() => alert('Settings')} style={{
            background: 'none',
            border: 'none',
            fontSize: '15px',
            color: '#666',
            cursor: 'pointer',
            padding: '6px 14px',
            border: '1px solid #ddd',
            borderRadius: '20px'
          }}>
            ⚙️ Settings
          </button>
        </div>

        {/* ✅ NEW: Right Section with Notifications and Profile */}
        <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
          {/* ✅ NEW: Notification Bell Icon */}
          <div style={{ position: 'relative' }}>
            <button
              onClick={() => setShowAlertDropdown(!showAlertDropdown)}
              style={{
                background: 'none',
                border: 'none',
                fontSize: '24px',
                cursor: 'pointer',
                position: 'relative',
                padding: '4px 8px'
              }}
              title={`${lateAlerts.length} new alerts`}
            >
              🔔
              {lateAlerts.length > 0 && (
                <span style={{
                  position: 'absolute',
                  top: '-4px',
                  right: '-4px',
                  background: '#f44336',
                  color: 'white',
                  width: '22px',
                  height: '22px',
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  boxShadow: '0 2px 8px rgba(244, 67, 54, 0.4)'
                }}>
                  {lateAlerts.length > 9 ? '9+' : lateAlerts.length}
                </span>
              )}
            </button>

            {/* ✅ NEW: Alert Dropdown Menu */}
            {showAlertDropdown && (
              <div style={{
                position: 'absolute',
                right: 0,
                top: '45px',
                background: 'white',
                borderRadius: '8px',
                boxShadow: '0 4px 16px rgba(0,0,0,0.15)',
                minWidth: '350px',
                maxWidth: '400px',
                zIndex: 1000,
                overflow: 'hidden',
                border: '1px solid #e0e0e0'
              }}>
                {/* Header */}
                <div style={{
                  padding: '12px 16px',
                  background: '#f5f5f5',
                  borderBottom: '1px solid #e0e0e0',
                  fontSize: '14px',
                  fontWeight: 'bold',
                  color: '#333'
                }}>
                  ⚠️ Staff Late Alerts ({lateAlerts.length})
                </div>

                {/* Alerts List */}
                <div style={{
                  maxHeight: '300px',
                  overflowY: 'auto'
                }}>
                  {lateAlerts.length === 0 ? (
                    <div style={{
                      padding: '16px',
                      textAlign: 'center',
                      color: '#999',
                      fontSize: '14px'
                    }}>
                      No new alerts
                    </div>
                  ) : (
                    lateAlerts.map((alert) => (
                      <div
                        key={alert.id}
                        style={{
                          padding: '12px 16px',
                          borderBottom: '1px solid #f0f0f0',
                          display: 'flex',
                          justifyContent: 'space-between',
                          alignItems: 'flex-start',
                          gap: '12px',
                          fontSize: '13px',
                          backgroundColor: '#fffbf0'
                        }}
                      >
                        <div style={{ flex: 1 }}>
                          <strong style={{ color: '#d32f2f' }}>
                            {alert.staffName}
                          </strong>
                          <div style={{ color: '#666', marginTop: '4px' }}>
                            ({alert.staffIdNumber})
                          </div>
                          <div style={{ color: '#666', marginTop: '4px' }}>
                            Late by {alert.minutesLate} mins - {alert.timeSlot}
                          </div>
                          <div style={{ color: '#999', marginTop: '4px', fontSize: '12px' }}>
                            Room: {alert.roomNumber} | {alert.alertDate}
                          </div>
                        </div>
                        <button
                          onClick={() => {
                            handleCloseAlert(alert.id);
                            if (lateAlerts.length === 1) {
                              setShowAlertDropdown(false);
                            }
                          }}
                          style={{
                            background: '#f44336',
                            border: 'none',
                            color: 'white',
                            width: '28px',
                            height: '28px',
                            borderRadius: '50%',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '16px',
                            fontWeight: 'bold',
                            flexShrink: 0,
                            transition: 'all 0.2s ease'
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.background = '#d32f2f';
                            e.currentTarget.style.transform = 'scale(1.1)';
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.background = '#f44336';
                            e.currentTarget.style.transform = 'scale(1)';
                          }}
                          title="Dismiss alert"
                        >
                          ✕
                        </button>
                      </div>
                    ))
                  )}
                </div>

                {/* Footer */}
                {lateAlerts.length > 0 && (
                  <div style={{
                    padding: '8px 16px',
                    background: '#f5f5f5',
                    borderTop: '1px solid #e0e0e0',
                    fontSize: '12px',
                    textAlign: 'center',
                    color: '#666'
                  }}>
                    Alerts are auto-updated every 30 seconds
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Profile Avatar and Menu */}
          <div style={{ position: 'relative' }}>
            <div
              onClick={() => setShowProfileMenu(!showProfileMenu)}
              style={{
                width: 42,
                height: 42,
                borderRadius: '50%',
                background: '#764ba2',
                color: '#fff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: 'bold',
                fontSize: '18px',
                cursor: 'pointer',
                border: '2px solid #764ba2'
              }}
              title="Profile"
            >
              {user.name ? user.name.charAt(0).toUpperCase() : 'A'}
            </div>

            {showProfileMenu && (
              <div style={{
                position: 'absolute',
                right: 0,
                top: '50px',
                background: 'white',
                borderRadius: '8px',
                boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
                minWidth: '160px',
                zIndex: 1000,
                overflow: 'hidden'
              }}>
                <button
                  onClick={handleLogout}
                  style={{
                    width: '100%',
                    padding: '12px 16px',
                    background: 'none',
                    border: 'none',
                    textAlign: 'left',
                    cursor: 'pointer',
                    fontSize: '14px',
                    color: '#333'
                  }}
                >
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </nav>

      {/* Main Content Area */}
      <div style={{
        maxWidth: '1200px',
        margin: '0 auto',
        padding: '40px 20px'
      }}>
        {/* Welcome Message */}
        <h2 style={{
          fontSize: '32px',
          fontWeight: 600,
          color: '#333',
          marginBottom: '40px',
          textAlign: 'center'
        }}>
          Welcome, {user.name}! 👋
        </h2>

        {/* Dashboard Cards */}
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          gap: '24px',
          flexWrap: 'wrap'
        }}>
          {/* Manage Staff Card */}
          <div
            onClick={handleManageStaff}
            style={{
              width: '280px',
              padding: '32px 24px',
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              borderRadius: '16px',
              boxShadow: '0 10px 30px rgba(118, 75, 162, 0.3)',
              cursor: 'pointer',
              transition: 'all 0.3s ease',
              textAlign: 'center'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-8px)';
              e.currentTarget.style.boxShadow = '0 15px 40px rgba(118, 75, 162, 0.4)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = '0 10px 30px rgba(118, 75, 162, 0.3)';
            }}
          >
            <div style={{ fontSize: '48px', marginBottom: '16px' }}>👥</div>
            <h3 style={{ fontSize: '22px', fontWeight: 600, color: 'white', margin: '0 0 12px 0' }}>
              Manage Staff
            </h3>
            <p style={{ fontSize: '14px', color: 'rgba(255,255,255,0.9)', margin: 0, lineHeight: '1.6' }}>
              Add new staff members and manage their expertise
            </p>
          </div>

          {/* Manage Attendance Card */}
          <div
            onClick={handleManageAttendance}
            style={{
              width: '280px',
              padding: '32px 24px',
              background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
              borderRadius: '16px',
              boxShadow: '0 10px 30px rgba(245, 87, 108, 0.3)',
              cursor: 'pointer',
              transition: 'all 0.3s ease',
              textAlign: 'center'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-8px)';
              e.currentTarget.style.boxShadow = '0 15px 40px rgba(245, 87, 108, 0.4)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = '0 10px 30px rgba(245, 87, 108, 0.3)';
            }}
          >
            <div style={{ fontSize: '48px', marginBottom: '16px' }}>📊</div>
            <h3 style={{ fontSize: '22px', fontWeight: 600, color: 'white', margin: '0 0 12px 0' }}>
              Manage Attendance
            </h3>
            <p style={{ fontSize: '14px', color: 'rgba(255,255,255,0.9)', margin: 0, lineHeight: '1.6' }}>
              Track student and staff attendance month-wise
            </p>
          </div>

          {/* Manage Marks Card */}
          <div
            onClick={handleManageMarks}
            style={{
              width: '280px',
              padding: '32px 24px',
              background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
              borderRadius: '16px',
              boxShadow: '0 10px 30px rgba(79, 172, 254, 0.3)',
              cursor: 'pointer',
              transition: 'all 0.3s ease',
              textAlign: 'center'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-8px)';
              e.currentTarget.style.boxShadow = '0 15px 40px rgba(79, 172, 254, 0.4)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = '0 10px 30px rgba(79, 172, 254, 0.3)';
            }}
          >
            <div style={{ fontSize: '48px', marginBottom: '16px' }}>📈</div>
            <h3 style={{ fontSize: '22px', fontWeight: 600, color: 'white', margin: '0 0 12px 0' }}>
              Manage Marks
            </h3>
            <p style={{ fontSize: '14px', color: 'rgba(255,255,255,0.9)', margin: 0, lineHeight: '1.6' }}>
              Add and manage student semester-wise marks
            </p>
          </div>

          {/* Semester Dates Configuration Card */}
          <div
            onClick={handleSemesterConfig}
            style={{
              width: '280px',
              padding: '32px 24px',
              background: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
              borderRadius: '16px',
              boxShadow: '0 10px 30px rgba(168, 237, 234, 0.3)',
              cursor: 'pointer',
              transition: 'all 0.3s ease',
              textAlign: 'center'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-8px)';
              e.currentTarget.style.boxShadow = '0 15px 40px rgba(168, 237, 234, 0.4)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = '0 10px 30px rgba(168, 237, 234, 0.3)';
            }}
          >
            <div style={{ fontSize: '48px', marginBottom: '16px' }}>📅</div>
            <h3 style={{ fontSize: '22px', fontWeight: 600, color: '#333', margin: '0 0 12px 0' }}>
              Semester Dates
            </h3>
            <p style={{ fontSize: '14px', color: '#555', margin: 0, lineHeight: '1.6' }}>
              Configure semester start and end dates for attendance tracking
            </p>
          </div>

          {/* ✅ EXISTING: Student Complaints Card */}
          <div
            onClick={handleViewComplaints}
            style={{
              width: '280px',
              padding: '32px 24px',
              background: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
              borderRadius: '16px',
              boxShadow: '0 10px 30px rgba(250, 112, 154, 0.3)',
              cursor: 'pointer',
              transition: 'all 0.3s ease',
              textAlign: 'center',
              position: 'relative'
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.transform = 'translateY(-8px)';
              e.currentTarget.style.boxShadow = '0 15px 40px rgba(250, 112, 154, 0.4)';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.transform = 'translateY(0)';
              e.currentTarget.style.boxShadow = '0 10px 30px rgba(250, 112, 154, 0.3)';
            }}
          >
            {/* Pending Count Badge */}
            {complaintStats.pending > 0 && (
              <div style={{
                position: 'absolute',
                top: '16px',
                right: '16px',
                background: '#f44336',
                color: 'white',
                width: '32px',
                height: '32px',
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '14px',
                fontWeight: 'bold',
                boxShadow: '0 2px 8px rgba(244, 67, 54, 0.4)'
              }}>
                {complaintStats.pending}
              </div>
            )}

            <div style={{ fontSize: '48px', marginBottom: '16px' }}>📮</div>
            <h3 style={{ fontSize: '22px', fontWeight: 600, color: '#333', margin: '0 0 12px 0' }}>
              Student Complaints
            </h3>
            <p style={{ fontSize: '14px', color: '#555', margin: 0, lineHeight: '1.6' }}>
              {loadingStats ? 'Loading...' : `${complaintStats.pending} pending • ${complaintStats.total} total`}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
