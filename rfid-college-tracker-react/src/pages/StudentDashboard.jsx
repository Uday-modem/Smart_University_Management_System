import React, { useState, useEffect, useRef, useContext, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';
import { ThemeContext } from '../context/ThemeContext';
import StudyAssistant from '../components/StudyAssistant';
import StudentAttendance from './StudentAttendance';

const API_BASE_URL = 'http://localhost:8080/api';

const StudentDashboard = () => {
  const { user, token, logout } = useContext(AuthContext);
  
  // Handle ThemeContext safely - use default if undefined
  const themeContext = useContext(ThemeContext);
  const darkMode = themeContext?.darkMode || false;
  const toggleTheme = themeContext?.toggleTheme || (() => {});
  
  const navigate = useNavigate();
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const [liveSummary, setLiveSummary] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const menuRef = useRef();
  const [localDarkMode, setLocalDarkMode] = useState(darkMode);


  // Complaint box state
  const [showComplaintModal, setShowComplaintModal] = useState(false);
  const [complaintIssueType, setComplaintIssueType] = useState('');
  const [complaintDescription, setComplaintDescription] = useState('');
  const [complaintSubmitting, setComplaintSubmitting] = useState(false);
  const [complaintError, setComplaintError] = useState('');


  // Study Assistant state
  const [studyAssistantOpen, setStudyAssistantOpen] = useState(false);

  // ✅ FIXED: Period Attendance state - ALL REQUIRED STATES
  const [showAttendanceForm, setShowAttendanceForm] = useState(false);
  const [codeInput, setCodeInput] = useState('');
  const [sectionId, setSectionId] = useState('');
  const [timeSlot, setTimeSlot] = useState('');
  const [codeLoading, setCodeLoading] = useState(false);
  const [codeMessage, setCodeMessage] = useState('');
  const [codeMessageType, setCodeMessageType] = useState('');
  const [retryCount, setRetryCount] = useState(0);
  const [timerActive, setTimerActive] = useState(false);
  const [timeLeft, setTimeLeft] = useState(0);

  const authAxios = useMemo(() => {
    return axios.create({
      baseURL: API_BASE_URL,
      headers: { Authorization: `Bearer ${token}` }
    });
  }, [token]);

  // Sync local dark mode with context
  useEffect(() => {
    setLocalDarkMode(darkMode);
  }, [darkMode]);

  // ✅ FIXED: Timer countdown for retry logic
  useEffect(() => {
    if (!timerActive || timeLeft <= 0) return;
    
    const timer = setInterval(() => {
      setTimeLeft(prev => {
        if (prev <= 1) {
          setTimerActive(false);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    
    return () => clearInterval(timer);
  }, [timerActive, timeLeft]);

  if (!user) return <div>Loading...</div>;

  // Fetch live attendance summary on component mount
  useEffect(() => {
    const fetchLiveSummary = async () => {
      if (!token) {
        setError('No authentication token found');
        return;
      }

      setLoading(true);
      setError(null);

      try {
        const response = await authAxios.get('/attendance/student/live-summary');
        setLiveSummary(response.data);
      } catch (err) {
        if (err.response) {
          if (err.response.status === 404) {
            setError('Semester configuration not set up yet');
          } else if (err.response.status === 401) {
            setError('Authentication failed. Please login again.');
          } else {
            setError(err.response.data?.message || 'Failed to load attendance data');
          }
        } else if (err.request) {
          setError('Cannot connect to server. Please check your connection.');
        } else {
          setError('An unexpected error occurred');
        }
      } finally {
        setLoading(false);
      }
    };

    fetchLiveSummary();
  }, [authAxios, token]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setShowProfileMenu(false);
      }
    };
    if (showProfileMenu) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [showProfileMenu]);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const handleViewProfile = () => {
    setShowProfileMenu(false);
    navigate('/student/profile');
  };

  const handleViewTimetable = () => {
    navigate('/student/timetable');
  };

  const handleViewAttendance = () => {
    navigate('/student/attendance');
  };

  const handleViewMarks = () => {
    navigate('/student/marks');
  };

  const handleComplaintBoxClick = () => {
    setShowComplaintModal(true);
    setComplaintIssueType('');
    setComplaintDescription('');
    setComplaintError('');
  };

  const handleSubmitComplaint = async () => {
    // Validation
    if (!complaintIssueType) {
      setComplaintError('Please select an issue type');
      return;
    }
    if (!complaintDescription.trim()) {
      setComplaintError('Please describe your complaint');
      return;
    }
    if (complaintDescription.trim().length < 10) {
      setComplaintError('Description must be at least 10 characters');
      return;
    }

    setComplaintSubmitting(true);
    setComplaintError('');

    try {
      const response = await authAxios.post('/complaints/submit', {
        issueType: complaintIssueType,
        description: complaintDescription.trim()
      });

      if (response.data.success) {
        alert('✅ Complaint submitted successfully! Admin will review it soon.');
        setShowComplaintModal(false);
        setComplaintIssueType('');
        setComplaintDescription('');
      } else {
        setComplaintError(response.data.message || 'Failed to submit complaint');
      }
    } catch (err) {
      setComplaintError(err.response?.data?.message || 'Failed to submit complaint. Please try again.');
    } finally {
      setComplaintSubmitting(false);
    }
  };

  const handleToggleTheme = () => {
    // Call the context toggle function
    toggleTheme();
    // Update local state as backup
    setLocalDarkMode(!localDarkMode);
  };

  // ✅ FIXED: Handle code input change
  const handleCodeInputChange = (e) => {
    setCodeInput(e.target.value.toUpperCase());
  };

  // ✅ FIXED: Handle section ID input change
  const handleSectionIdChange = (e) => {
    setSectionId(e.target.value);
  };

  // ✅ FIXED: Handle time slot input change
  const handleTimeSlotChange = (e) => {
    setTimeSlot(e.target.value);
  };

  // ✅ FIXED: Submit verification code to backend
  const handleVerifyCode = async () => {
    if (!codeInput || !sectionId || !timeSlot) {
      setCodeMessage('⚠️ Please fill in all fields');
      setCodeMessageType('error');
      return;
    }

    if (timerActive) {
      setCodeMessage(`⏳ Please wait ${timeLeft}s before trying again`);
      setCodeMessageType('error');
      return;
    }

    try {
      setCodeLoading(true);
      setCodeMessage('');

      const response = await axios.post(`${API_BASE_URL}/attendance/verify-code`, {
        studentRegNo: user.username || user.email,
        code: codeInput,
        sectionId: sectionId,
        date: new Date().toISOString().split('T')[0],
        timeSlot: timeSlot
      });

      if (response.data.success) {
        setCodeMessage('✅ Attendance marked successfully!');
        setCodeMessageType('success');
        setCodeInput('');
        setSectionId('');
        setTimeSlot('');
        setRetryCount(0);
        
        // Close form after 2 seconds
        setTimeout(() => {
          setShowAttendanceForm(false);
          setCodeMessage('');
        }, 2000);
      } else {
        setCodeMessage(response.data.message || '❌ Failed to verify code');
        setCodeMessageType('error');
      }
    } catch (error) {
      console.error('Error verifying code:', error);
      const newRetryCount = retryCount + 1;
      setRetryCount(newRetryCount);

      if (newRetryCount >= 3) {
        setTimerActive(true);
        setTimeLeft(60);
        setCodeMessage('❌ Max attempts reached. Try again in 60 seconds.');
      } else {
        const remaining = 3 - newRetryCount;
        setCodeMessage(`❌ ${error.response?.data?.message || 'Invalid code'}. ${remaining} attempts left.`);
      }
      setCodeMessageType('error');
    } finally {
      setCodeLoading(false);
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      'NORMAL': '#4caf50',
      'CONDONATION': '#ff9800',
      'DETAINED': '#f44336'
    };
    return colors[status] || '#999';
  };

  return (
    <div className={localDarkMode ? 'dark-mode' : ''} style={{ minHeight: '100vh', background: localDarkMode ? '#222325' : '#f5f7fa', fontFamily: 'Segoe UI, sans-serif', transition: 'background-color 0.3s ease, color 0.3s ease' }}>
      {/* Top Navigation Bar */}
      <nav style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '16px 30px',
        background: localDarkMode ? '#181919' : '#fff',
        boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
        position: 'sticky',
        top: 0,
        zIndex: 100,
        transition: 'background-color 0.3s ease'
      }}>
        <h1 style={{ margin: 0, fontSize: '20px', fontWeight: 600, color: localDarkMode ? '#80bfff' : '#1e88e5' }}>
          Smart University Management System
        </h1>

        <div style={{ display: 'flex', gap: '30px', alignItems: 'center' }}>
          <button onClick={() => navigate('/student/dashboard')} style={{
            background: 'none',
            border: 'none',
            fontSize: '15px',
            color: localDarkMode ? '#80bfff' : '#1e88e5',
            cursor: 'pointer',
            fontWeight: 500,
            borderBottom: `3px solid ${localDarkMode ? '#80bfff' : '#1e88e5'}`,
            paddingBottom: '4px',
            transition: 'color 0.3s ease'
          }}>
            Home
          </button>
          <button onClick={handleViewTimetable} style={{
            background: 'none',
            border: 'none',
            fontSize: '15px',
            color: localDarkMode ? '#ccc' : '#666',
            cursor: 'pointer',
            transition: 'color 0.3s'
          }}
          onMouseEnter={(e) => e.target.style.color = localDarkMode ? '#80bfff' : '#1e88e5'}
          onMouseLeave={(e) => e.target.style.color = localDarkMode ? '#ccc' : '#666'}
          >
            Timetable
          </button>
          <button onClick={handleViewAttendance} style={{
            background: 'none',
            border: 'none',
            fontSize: '15px',
            color: localDarkMode ? '#ccc' : '#666',
            cursor: 'pointer',
            transition: 'color 0.3s'
          }}
          onMouseEnter={(e) => e.target.style.color = localDarkMode ? '#80bfff' : '#1e88e5'}
          onMouseLeave={(e) => e.target.style.color = localDarkMode ? '#ccc' : '#666'}
          >
            📊 Attendance
          </button>
          <button onClick={handleViewMarks} style={{
            background: 'none',
            border: 'none',
            fontSize: '15px',
            color: localDarkMode ? '#ccc' : '#666',
            cursor: 'pointer',
            transition: 'color 0.3s'
          }}
          onMouseEnter={(e) => e.target.style.color = localDarkMode ? '#80bfff' : '#1e88e5'}
          onMouseLeave={(e) => e.target.style.color = localDarkMode ? '#ccc' : '#666'}
          >
            📈 Marks
          </button>
          <button onClick={handleComplaintBoxClick} style={{
            background: 'none',
            border: 'none',
            fontSize: '15px',
            color: localDarkMode ? '#ccc' : '#666',
            cursor: 'pointer',
            transition: 'color 0.3s'
          }}
          onMouseEnter={(e) => e.target.style.color = localDarkMode ? '#80bfff' : '#1e88e5'}
          onMouseLeave={(e) => e.target.style.color = localDarkMode ? '#ccc' : '#666'}
          >
            📮 Complaint Box
          </button>
          {/* DARK MODE TOGGLE BUTTON */}
          <button onClick={handleToggleTheme} style={{
            background: 'none',
            border: 'none',
            fontSize: '20px',
            color: localDarkMode ? '#ffd700' : '#ffa500',
            cursor: 'pointer',
            marginLeft: '15px',
            padding: '0',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            transition: 'transform 0.3s ease, color 0.3s ease'
          }}
          onMouseEnter={(e) => e.target.style.transform = 'scale(1.2)'}
          onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
          title="Toggle Dark Mode"
          >
            {localDarkMode ? '☀️' : '🌙'}
          </button>
        </div>

        <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
          <button onClick={() => alert('Contact Admin')} style={{
            background: localDarkMode ? '#333' : 'none',
            border: '1px solid #ddd',
            fontSize: '15px',
            color: localDarkMode ? '#eee' : '#666',
            cursor: 'pointer',
            padding: '6px 14px',
            borderRadius: '20px',
            transition: 'background-color 0.3s ease, color 0.3s ease'
          }}>
            📞 Contact Admin
          </button>

          <div style={{ position: 'relative' }} ref={menuRef}>
            <div
              onClick={() => setShowProfileMenu((v) => !v)}
              style={{
                width: 42,
                height: 42,
                borderRadius: '50%',
                background: localDarkMode ? '#282828' : '#1e88e5',
                color: '#fff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: 'bold',
                fontSize: '18px',
                cursor: 'pointer',
                border: `2px solid ${localDarkMode ? '#80bfff' : '#1e88e5'}`,
                transition: 'background-color 0.3s ease, border-color 0.3s ease'
              }}
              title="Profile"
            >
              {user.name ? user.name.charAt(0).toUpperCase() : 'S'}
            </div>

            {showProfileMenu && (
              <div style={{
                position: 'absolute',
                right: 0,
                top: '50px',
                background: localDarkMode ? '#222325' : '#fff',
                borderRadius: '8px',
                boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
                minWidth: '160px',
                zIndex: 1000,
                overflow: 'hidden',
                transition: 'background-color 0.3s ease'
              }}>
                <button
                  onClick={handleViewProfile}
                  style={{
                    width: '100%',
                    padding: '12px 16px',
                    background: 'none',
                    border: 'none',
                    textAlign: 'left',
                    cursor: 'pointer',
                    fontSize: '14px',
                    color: localDarkMode ? '#80bfff' : '#333',
                    transition: 'color 0.3s ease'
                  }}
                >
                  View Profile
                </button>
                <hr style={{ margin: 0 }} />
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
                    color: localDarkMode ? '#80bfff' : '#333',
                    transition: 'color 0.3s ease'
                  }}
                >
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </nav>


      {/* Main Content */}
      <div style={{
        width: '100%',
        margin: '0 auto'
      }}>
        {/* STUDY ASSISTANT HERO SECTION - 100% WIDTH CENTERED - TOP SECTION */}
        <div style={{
          width: '100%',
          display: 'flex',
          flexDirection: 'column',
          minHeight: '600px',
          padding: '60px 20px',
          background: localDarkMode ? '#222325' : '#f5f7fa',
          position: 'relative',
          transition: 'background-color 0.3s ease'
        }}>
          {/* Welcome Message - TOP RIGHT */}
          <div style={{
            position: 'absolute',
            top: '60px',
            right: '20px',
            maxWidth: '1200px'
          }}>
            <h2 style={{
              fontSize: '30px',
              fontWeight: 600,
              color: localDarkMode ? '#eee' : '#333',
              margin: 0,
              transition: 'color 0.3s ease'
            }}>
              Welcome, {user.name}! 👋
            </h2>
          </div>


          {/* Hero Content - CENTER */}
          <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            flex: 1
          }}>
            <div style={{
              textAlign: 'center',
              maxWidth: '700px',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              {/* Brain Icon */}
              <div style={{
                fontSize: '80px',
                marginBottom: '40px',
                animation: 'float 3s ease-in-out infinite',
                display: 'inline-block'
              }}>
                🧠
              </div>


              {/* Main Heading */}
              <h1 style={{
                fontSize: '56px',
                fontWeight: 700,
                color: localDarkMode ? '#fff' : '#222',
                margin: '0 0 20px 0',
                lineHeight: '1.2',
                letterSpacing: '-0.5px',
                transition: 'color 0.3s ease'
              }}>
                Understand{' '}
                <span style={{
                  background: 'linear-gradient(135deg, #20c997 0%, #17a2b8 100%)',
                  WebkitBackgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                  backgroundClip: 'text'
                }}>
                  Anything
                </span>
              </h1>


              {/* Subtitle */}
              <p style={{
                fontSize: '16px',
                color: localDarkMode ? '#aaa' : '#666',
                margin: '0 0 40px 0',
                lineHeight: '1.6',
                maxWidth: '600px',
                transition: 'color 0.3s ease'
              }}>
                Generate AI-powered mind maps from YouTube videos to enhance your learning. Transform complex concepts into visual knowledge.
              </p>


              {/* Divider Line */}
              <div style={{
                width: '60px',
                height: '3px',
                background: 'linear-gradient(135deg, #20c997 0%, #17a2b8 100%)',
                borderRadius: '2px',
                marginBottom: '40px'
              }}></div>


              {/* CTA Button */}
              <button
                onClick={() => setStudyAssistantOpen(true)}
                style={{
                  padding: '16px 40px',
                  fontSize: '16px',
                  fontWeight: 600,
                  border: 'none',
                  borderRadius: '12px',
                  background: 'linear-gradient(135deg, #20c997 0%, #17a2b8 100%)',
                  color: 'white',
                  cursor: 'pointer',
                  transition: 'all 0.3s ease',
                  boxShadow: '0 4px 16px rgba(32, 201, 151, 0.3)',
                  display: 'inline-block'
                }}
                onMouseEnter={(e) => {
                  e.target.style.transform = 'translateY(-2px)';
                  e.target.style.boxShadow = '0 8px 24px rgba(32, 201, 151, 0.4)';
                }}
                onMouseLeave={(e) => {
                  e.target.style.transform = 'translateY(0)';
                  e.target.style.boxShadow = '0 4px 16px rgba(32, 201, 151, 0.3)';
                }}
              >
                Try Study Assistant →
              </button>
            </div>
          </div>
        </div>


        {/* CSS for floating animation */}
        <style>{`
          @keyframes float {
            0%, 100% { transform: translateY(0px); }
            50% { transform: translateY(-20px); }
          }
          @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
          }
        `}</style>


        {/* Live Attendance Widget - BOTTOM SECTION - LEFT ALIGNED */}
        <div style={{
          maxWidth: '1200px',
          margin: '0 auto',
          padding: '60px 20px',
          display: 'flex',
          justifyContent: 'flex-start',
          gap: '40px',
          flexWrap: 'wrap',
          alignItems: 'flex-start'
        }}>
          <div style={{
            width: '350px',
            background: localDarkMode ? '#222325' : 'white',
            borderRadius: '12px',
            padding: '20px',
            boxShadow: '0 4px 16px rgba(0, 0, 0, 0.1)',
            border: localDarkMode ? '1px solid #444' : '1px solid #e0e0e0',
            transition: 'background-color 0.3s ease, border-color 0.3s ease'
          }}>
            {loading && (
              <div style={{ textAlign: 'center', padding: '20px', color: '#999', fontSize: '13px' }}>
                Loading attendance...
              </div>
            )}


            {error && !loading && (
              <div style={{
                textAlign: 'center',
                padding: '20px',
                color: '#f44336',
                fontSize: '13px',
                background: '#ffebee',
                borderRadius: '8px'
              }}>
                ⚠️ {error}
              </div>
            )}


            {!loading && !error && liveSummary && (
              <>
                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  marginBottom: '12px'
                }}>
                  <h3 style={{
                    fontSize: '16px',
                    fontWeight: 600,
                    color: localDarkMode ? '#eee' : '#333',
                    margin: 0,
                    transition: 'color 0.3s ease'
                  }}>
                    Attendance
                  </h3>
                  <div style={{
                    width: '20px',
                    height: '20px',
                    borderRadius: '50%',
                    border: '2px solid #999',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: '12px',
                    color: '#999',
                    cursor: 'help'
                  }} title="Live tracking">
                    ?
                  </div>
                </div>


                <div style={{
                  fontSize: '14px',
                  fontWeight: 600,
                  color: localDarkMode ? '#ccc' : '#555',
                  marginBottom: '16px',
                  transition: 'color 0.3s ease'
                }}>
                  {liveSummary.semesterName}
                </div>


                <div style={{ marginBottom: '12px' }}>
                  <div style={{
                    width: '100%',
                    height: '6px',
                    background: localDarkMode ? '#444' : '#e0e0e0',
                    borderRadius: '3px',
                    overflow: 'hidden',
                    transition: 'background-color 0.3s ease'
                  }}>
                    <div style={{
                      width: `${liveSummary.percentage || 0}%`,
                      height: '100%',
                      background: getStatusColor(liveSummary.status),
                      borderRadius: '3px',
                      transition: 'width 0.3s ease'
                    }}></div>
                  </div>
                </div>


                <div style={{
                  fontSize: '32px',
                  fontWeight: 700,
                  textAlign: 'center',
                  margin: '16px 0',
                  color: getStatusColor(liveSummary.status)
                }}>
                  {typeof liveSummary.percentage === 'number' ? liveSummary.percentage.toFixed(1) : '0.0'}%
                </div>


                <div style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  marginBottom: '16px',
                  gap: '8px'
                }}>
                  <div style={{
                    flex: 1,
                    background: localDarkMode ? '#282828' : '#f5f5f5',
                    padding: '10px',
                    borderRadius: '8px',
                    textAlign: 'center',
                    transition: 'background-color 0.3s ease'
                  }}>
                    <div style={{ fontSize: '10px', color: '#666', marginBottom: '4px' }}>Total</div>
                    <div style={{ fontSize: '18px', fontWeight: 700, color: localDarkMode ? '#eee' : '#333', transition: 'color 0.3s ease' }}>
                      {liveSummary.totalClasses}
                    </div>
                  </div>
                  <div style={{
                    flex: 1,
                    background: '#e8f5e9',
                    padding: '10px',
                    borderRadius: '8px',
                    textAlign: 'center',
                    border: '1px solid #4caf50'
                  }}>
                    <div style={{ fontSize: '10px', color: '#666', marginBottom: '4px' }}>Present</div>
                    <div style={{ fontSize: '18px', fontWeight: 700, color: localDarkMode ? '#222' : '#333' }}>
                      {liveSummary.attendedClasses}
                    </div>
                  </div>
                  <div style={{
                    flex: 1,
                    background: '#ffebee',
                    padding: '10px',
                    borderRadius: '8px',
                    textAlign: 'center',
                    border: '1px solid #f44336'
                  }}>
                    <div style={{ fontSize: '10px', color: '#666', marginBottom: '4px' }}>Absent</div>
                    <div style={{ fontSize: '18px', fontWeight: 700, color: localDarkMode ? '#222' : '#333' }}>
                      {liveSummary.absentClasses || 0}
                    </div>
                  </div>
                </div>


                <div style={{
                  textAlign: 'center',
                  marginBottom: '12px'
                }}>
                  <div style={{
                    display: 'inline-block',
                    padding: '6px 16px',
                    borderRadius: '16px',
                    fontSize: '12px',
                    fontWeight: 600,
                    background: getStatusColor(liveSummary.status),
                    color: 'white'
                  }}>
                    {liveSummary.status}
                  </div>
                </div>


                <div style={{
                  fontSize: '11px',
                  color: localDarkMode ? '#eee' : '#666',
                  textAlign: 'center',
                  marginBottom: '12px',
                  paddingTop: '12px',
                  borderTop: '1px solid #e0e0e0',
                  transition: 'color 0.3s ease'
                }}>
                  Status: {liveSummary.semesterActive ? '🟢 Active' : '🔴 Ended'}
                </div>


                <button
                  onClick={handleViewAttendance}
                  style={{
                    width: '100%',
                    padding: '10px',
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    color: 'white',
                    border: 'none',
                    borderRadius: '6px',
                    fontSize: '13px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    transition: 'all 0.3s ease'
                  }}
                  onMouseEnter={(e) => {
                    e.target.style.transform = 'translateY(-1px)';
                    e.target.style.boxShadow = '0 4px 12px rgba(118, 75, 162, 0.3)';
                  }}
                  onMouseLeave={(e) => {
                    e.target.style.transform = 'translateY(0)';
                    e.target.style.boxShadow = 'none';
                  }}
                >
                  View Details
                </button>
              </>
            )}


            {!loading && !error && !liveSummary && (
              <div style={{
                textAlign: 'center',
                padding: '20px',
                color: '#999',
                fontSize: '13px'
              }}>
                No attendance data available
              </div>
            )}
          </div>



          {/* ✅ FIXED: Period Attendance Card with Input Form */}
          <div className="period-attendance-card" style={{
            width: '350px',
            background: localDarkMode ? '#222325' : 'white',
            borderRadius: '12px',
            padding: '20px',
            boxShadow: '0 4px 16px rgba(0, 0, 0, 0.1)',
            border: localDarkMode ? '1px solid #444' : '1px solid #e0e0e0',
            transition: 'all 0.3s ease',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            height: 'fit-content',
            position: 'relative',
            overflow: 'hidden',
            animation: 'fadeIn 0.3s ease'
          }}>
            {/* Decorative top bar */}
            <div style={{
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              height: '4px',
              background: 'linear-gradient(90deg, #20c997, #17a2b8)'
            }} />

            <h3 style={{
              margin: '10px 0 16px 0',
              fontSize: '16px',
              fontWeight: 600,
              color: localDarkMode ? '#eee' : '#333',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              transition: 'color 0.3s ease'
            }}>
              ✅ Mark Attendance
            </h3>

            {!showAttendanceForm ? (
              <div style={{
                display: 'flex',
                flexDirection: 'column',
                gap: '16px'
              }}>
                <div style={{
                  background: localDarkMode ? 'rgba(32, 201, 151, 0.1)' : '#f0fdf9',
                  padding: '16px',
                  borderRadius: '8px',
                  border: '1px dashed #20c997'
                }}>
                  <p style={{
                    fontSize: '13px',
                    color: localDarkMode ? '#ccc' : '#555',
                    margin: 0,
                    textAlign: 'center',
                    lineHeight: '1.5'
                  }}>
                    Have a code? Enter it below to mark your presence for this session.
                  </p>
                </div>

                <button 
                  onClick={() => setShowAttendanceForm(true)}
                  style={{
                    width: '100%',
                    padding: '12px',
                    background: 'linear-gradient(135deg, #20c997 0%, #17a2b8 100%)',
                    color: 'white',
                    border: 'none',
                    borderRadius: '8px',
                    fontSize: '14px',
                    fontWeight: '600',
                    cursor: 'pointer',
                    transition: 'all 0.3s ease',
                    boxShadow: '0 4px 12px rgba(32, 201, 151, 0.2)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    gap: '8px'
                  }}
                  onMouseEnter={(e) => {
                    e.target.style.transform = 'translateY(-2px)';
                    e.target.style.boxShadow = '0 6px 16px rgba(32, 201, 151, 0.3)';
                  }}
                  onMouseLeave={(e) => {
                    e.target.style.transform = 'translateY(0)';
                    e.target.style.boxShadow = '0 4px 12px rgba(32, 201, 151, 0.2)';
                  }}
                >
                  Enter Code →
                </button>
              </div>
            ) : (
              <div className="attendance-form-container" style={{ animation: 'fadeIn 0.3s ease' }}>
                {/* ✅ FIXED: Input Fields */}
                <div style={{ marginBottom: '16px' }}>
                  <label style={{
                    display: 'block',
                    fontSize: '12px',
                    fontWeight: 600,
                    color: localDarkMode ? '#ccc' : '#666',
                    marginBottom: '6px',
                    textTransform: 'uppercase',
                    letterSpacing: '0.5px'
                  }}>
                    Verification Code
                  </label>
                  <input
                    type="text"
                    id="codeInput"
                    placeholder="Enter 6-digit code"
                    value={codeInput}
                    onChange={handleCodeInputChange}
                    maxLength="6"
                    disabled={codeLoading || timerActive}
                    style={{
                      width: '100%',
                      padding: '10px 12px',
                      border: `1px solid ${localDarkMode ? '#444' : '#ddd'}`,
                      borderRadius: '6px',
                      fontSize: '14px',
                      color: localDarkMode ? '#eee' : '#333',
                      background: localDarkMode ? '#333' : '#fff',
                      fontFamily: 'inherit',
                      transition: 'all 0.3s ease',
                      opacity: codeLoading || timerActive ? 0.6 : 1
                    }}
                  />
                </div>

                <div style={{ marginBottom: '16px' }}>
                  <label style={{
                    display: 'block',
                    fontSize: '12px',
                    fontWeight: 600,
                    color: localDarkMode ? '#ccc' : '#666',
                    marginBottom: '6px',
                    textTransform: 'uppercase',
                    letterSpacing: '0.5px'
                  }}>
                    Section ID
                  </label>
                  <input
                    type="text"
                    id="sectionInput"
                    placeholder="e.g., ECE2A"
                    value={sectionId}
                    onChange={handleSectionIdChange}
                    disabled={codeLoading || timerActive}
                    style={{
                      width: '100%',
                      padding: '10px 12px',
                      border: `1px solid ${localDarkMode ? '#444' : '#ddd'}`,
                      borderRadius: '6px',
                      fontSize: '14px',
                      color: localDarkMode ? '#eee' : '#333',
                      background: localDarkMode ? '#333' : '#fff',
                      fontFamily: 'inherit',
                      transition: 'all 0.3s ease',
                      opacity: codeLoading || timerActive ? 0.6 : 1
                    }}
                  />
                </div>

                <div style={{ marginBottom: '16px' }}>
                  <label style={{
                    display: 'block',
                    fontSize: '12px',
                    fontWeight: 600,
                    color: localDarkMode ? '#ccc' : '#666',
                    marginBottom: '6px',
                    textTransform: 'uppercase',
                    letterSpacing: '0.5px'
                  }}>
                    Time Slot
                  </label>
                  <input
                    type="text"
                    id="timeSlotInput"
                    placeholder="e.g., 10:00-11:00"
                    value={timeSlot}
                    onChange={handleTimeSlotChange}
                    disabled={codeLoading || timerActive}
                    style={{
                      width: '100%',
                      padding: '10px 12px',
                      border: `1px solid ${localDarkMode ? '#444' : '#ddd'}`,
                      borderRadius: '6px',
                      fontSize: '14px',
                      color: localDarkMode ? '#eee' : '#333',
                      background: localDarkMode ? '#333' : '#fff',
                      fontFamily: 'inherit',
                      transition: 'all 0.3s ease',
                      opacity: codeLoading || timerActive ? 0.6 : 1
                    }}
                  />
                </div>

                {/* Submit & Cancel Buttons */}
                <div style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
                  <button
                    onClick={handleVerifyCode}
                    disabled={codeLoading || !codeInput || !sectionId || !timeSlot || timerActive}
                    style={{
                      flex: 1,
                      padding: '10px',
                      background: codeLoading || !codeInput || !sectionId || !timeSlot || timerActive 
                        ? '#ccc' 
                        : 'linear-gradient(135deg, #20c997 0%, #17a2b8 100%)',
                      color: 'white',
                      border: 'none',
                      borderRadius: '6px',
                      fontSize: '13px',
                      fontWeight: '600',
                      cursor: codeLoading || !codeInput || !sectionId || !timeSlot || timerActive ? 'not-allowed' : 'pointer',
                      transition: 'all 0.3s ease'
                    }}
                    onMouseEnter={(e) => {
                      if (!codeLoading && codeInput && sectionId && timeSlot && !timerActive) {
                        e.target.style.transform = 'translateY(-2px)';
                      }
                    }}
                    onMouseLeave={(e) => {
                      e.target.style.transform = 'translateY(0)';
                    }}
                  >
                    {codeLoading ? '⏳ Submitting...' : '✅ Submit'}
                  </button>
                  <button
                    onClick={() => {
                      setShowAttendanceForm(false);
                      setCodeInput('');
                      setCodeMessage('');
                      setSectionId('');
                      setTimeSlot('');
                    }}
                    disabled={codeLoading}
                    style={{
                      flex: 1,
                      padding: '10px',
                      background: localDarkMode ? '#333' : '#f5f5f5',
                      color: localDarkMode ? '#eee' : '#666',
                      border: `1px solid ${localDarkMode ? '#444' : '#ddd'}`,
                      borderRadius: '6px',
                      fontSize: '13px',
                      fontWeight: '600',
                      cursor: codeLoading ? 'not-allowed' : 'pointer',
                      transition: 'all 0.3s ease',
                      opacity: codeLoading ? 0.6 : 1
                    }}
                  >
                    ❌ Cancel
                  </button>
                </div>

                {/* Message Display */}
                {codeMessage && (
                  <div style={{
                    padding: '10px',
                    borderRadius: '6px',
                    fontSize: '12px',
                    fontWeight: 500,
                    textAlign: 'center',
                    background: codeMessageType === 'success' 
                      ? 'rgba(76, 175, 80, 0.1)' 
                      : 'rgba(244, 67, 54, 0.1)',
                    color: codeMessageType === 'success' ? '#4caf50' : '#f44336',
                    border: `1px solid ${codeMessageType === 'success' ? '#4caf50' : '#f44336'}`,
                    animation: 'fadeIn 0.3s ease'
                  }}>
                    {codeMessage}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>


      {/* Complaint Box Modal */}
      {showComplaintModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            background: localDarkMode ? '#222325' : 'white',
            borderRadius: '12px',
            padding: '32px',
            width: '90%',
            maxWidth: '500px',
            boxShadow: '0 10px 40px rgba(0, 0, 0, 0.2)',
            transition: 'background-color 0.3s ease'
          }}>
            {/* Modal Header */}
            <div style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: '24px'
            }}>
              <h2 style={{
                fontSize: '24px',
                fontWeight: 600,
                color: localDarkMode ? '#eee' : '#333',
                margin: 0,
                transition: 'color 0.3s ease'
              }}>
                📮 Submit Complaint
              </h2>
              <button
                onClick={() => setShowComplaintModal(false)}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '24px',
                  cursor: 'pointer',
                  color: '#999'
                }}
              >
                ×
              </button>
            </div>


            {complaintError && (
              <div style={{
                padding: '12px',
                background: '#ffebee',
                borderLeft: '4px solid #f44336',
                borderRadius: '4px',
                marginBottom: '16px',
                color: '#c62828',
                fontSize: '14px'
              }}>
                {complaintError}
              </div>
            )}


            <div style={{ marginBottom: '20px' }}>
              <label style={{
                display: 'block',
                fontSize: '14px',
                fontWeight: 600,
                color: localDarkMode ? '#eee' : '#333',
                marginBottom: '8px',
                transition: 'color 0.3s ease'
              }}>
                Issue Type *
              </label>
              <select
                value={complaintIssueType}
                onChange={(e) => setComplaintIssueType(e.target.value)}
                style={{
                  width: '100%',
                  padding: '12px',
                  border: '1px solid #ddd',
                  borderRadius: '8px',
                  fontSize: '14px',
                  color: localDarkMode ? '#eee' : '#333',
                  background: localDarkMode ? '#333' : '#fff',
                  transition: 'background-color 0.3s ease, color 0.3s ease'
                }}
              >
                <option value="">Select issue type</option>
                <option value="STAFF_RELATED">Staff Related</option>
                <option value="ADMINISTRATION_RELATED">Administration Related</option>
                <option value="CLASS_RELATED">Class Related</option>
                <option value="APPLICATION_RELATED">Application Related</option>
                <option value="INFRASTRUCTURE_RELATED">Infrastructure Related</option>
                <option value="OTHER">Other</option>
              </select>
            </div>


            <div style={{ marginBottom: '24px' }}>
              <label style={{
                display: 'block',
                fontSize: '14px',
                fontWeight: 600,
                color: localDarkMode ? '#eee' : '#333',
                marginBottom: '8px',
                transition: 'color 0.3s ease'
              }}>
                Description *
              </label>
              <textarea
                value={complaintDescription}
                onChange={(e) => setComplaintDescription(e.target.value)}
                placeholder="Describe your complaint in detail..."
                rows={6}
                style={{
                  width: '100%',
                  padding: '12px',
                  border: '1px solid #ddd',
                  borderRadius: '8px',
                  fontSize: '14px',
                  color: localDarkMode ? '#eee' : '#333',
                  background: localDarkMode ? '#333' : '#fff',
                  fontFamily: 'inherit',
                  resize: 'vertical',
                  transition: 'background-color 0.3s ease, color 0.3s ease'
                }}
              />
              <small style={{ color: '#666', fontSize: '12px' }}>
                Minimum 10 characters required
              </small>
            </div>


            <div style={{
              display: 'flex',
              gap: '12px',
              justifyContent: 'flex-end'
            }}>
              <button
                onClick={() => setShowComplaintModal(false)}
                disabled={complaintSubmitting}
                style={{
                  padding: '12px 24px',
                  background: localDarkMode ? '#333' : '#f5f5f5',
                  border: 'none',
                  borderRadius: '8px',
                  fontSize: '14px',
                  fontWeight: 600,
                  color: localDarkMode ? '#eee' : '#666',
                  cursor: complaintSubmitting ? 'not-allowed' : 'pointer',
                  transition: 'background-color 0.3s ease, color 0.3s ease'
                }}
              >
                Cancel
              </button>
              <button
                onClick={handleSubmitComplaint}
                disabled={complaintSubmitting}
                style={{
                  padding: '12px 24px',
                  background: complaintSubmitting ? '#ccc' : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  border: 'none',
                  borderRadius: '8px',
                  fontSize: '14px',
                  fontWeight: 600,
                  color: 'white',
                  cursor: complaintSubmitting ? 'not-allowed' : 'pointer',
                  transition: 'all 0.3s ease'
                }}
              >
                {complaintSubmitting ? 'Sending...' : 'Send Complaint'}
              </button>
            </div>
          </div>
        </div>
      )}


      {/* STUDY ASSISTANT MODAL COMPONENT */}
      <StudyAssistant
        isOpen={studyAssistantOpen}
        onClose={() => setStudyAssistantOpen(false)}
        token={token}
      />
    </div>
  );
};


export default StudentDashboard;