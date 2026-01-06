import React, { useState, useEffect, useContext, useMemo } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './StudentTimetable.css';

const API_BASE_URL = 'http://localhost:8080/api';

const StudentTimetable = () => {
  const navigate = useNavigate();
  const [timetable, setTimetable] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const { token } = useContext(AuthContext);

  const authAxios = useMemo(() => {
    return axios.create({
      baseURL: API_BASE_URL,
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  }, [token]);

  useEffect(() => {
    const fetchStudentTimetable = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await authAxios.get('/timetable/student');
        setTimetable(response.data);
      } catch (err) {
        setError('Failed to load timetable');
        setTimetable([]);
      } finally {
        setLoading(false);
      }
    };
    if (token) fetchStudentTimetable();
  }, [authAxios, token]);

  const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  const timeSlots = [...new Set(timetable.map(item => item.timeSlot))].sort();

  return (
    <div className="timetable-container">
      <button
        onClick={() => navigate('/student/dashboard')}
        style={{
          position: 'absolute',
          top: '20px',
          left: '20px',
          padding: '10px 20px',
          background: '#1e88e5',
          color: 'white',
          border: 'none',
          borderRadius: '8px',
          cursor: 'pointer',
          fontSize: '14px',
          fontWeight: '500',
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          transition: 'all 0.3s ease',
          boxShadow: '0 2px 8px rgba(30, 136, 229, 0.3)'
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.background = '#42a5f5';
          e.currentTarget.style.transform = 'translateY(-2px)';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.background = '#1e88e5';
          e.currentTarget.style.transform = 'translateY(0)';
        }}
      >
        ← Back to Dashboard
      </button>

      <h2 style={{ marginTop: '60px' }}>My Timetable</h2>

      {error && <div className="error-message">{error}</div>}
      {loading && <div className="loading">Loading...</div>}

      {timetable.length > 0 && (
        <div className="timetable-wrapper">
          <table className="timetable-table">
            <thead>
              <tr>
                <th>Time</th>
                {days.map(day => <th key={day}>{day}</th>)}
              </tr>
            </thead>
            <tbody>
              {timeSlots.map(slot => (
                <tr key={slot}>
                  <td className="time-slot">{slot}</td>
                  {days.map(day => {
                    const entry = timetable.find(
                      item => item.dayOfWeek === day && item.timeSlot === slot
                    );
                    return (
                      <td key={day}>
                        {entry ? (
                          <div className="subject-info">
                            <strong>{entry.subject}</strong>
                            <small>Room: {entry.room}</small>
                          </div>
                        ) : (
                          '--'
                        )}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {!loading && !error && timetable.length === 0 && (
        <div style={{ textAlign: 'center', paddingTop: '40px', color: '#888', fontSize: '16px' }}>
          No timetable found for your account.
        </div>
      )}
    </div>
  );
};
export default StudentTimetable;
