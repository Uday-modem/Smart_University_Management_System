import React, { useState, useEffect, useContext, useMemo } from 'react';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './StudentMarksView.css';

const API_BASE_URL = 'http://localhost:8080/api';

const SEMESTERS = [
  { value: 1, name: 'Semester 1' },
  { value: 2, name: 'Semester 2' },
  { value: 3, name: 'Semester 3' },
  { value: 4, name: 'Semester 4' },
  { value: 5, name: 'Semester 5' },
  { value: 6, name: 'Semester 6' },
  { value: 7, name: 'Semester 7' },
  { value: 8, name: 'Semester 8' }
];

const StudentMarksView = () => {
  const { user, token } = useContext(AuthContext);
  const navigate = useNavigate();
  
  const [selectedSemester, setSelectedSemester] = useState('');
  const [marks, setMarks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [totalMarks, setTotalMarks] = useState(0);
  const [averageGrade, setAverageGrade] = useState('');

  const authAxios = useMemo(() => {
    return axios.create({
      baseURL: API_BASE_URL,
      headers: { Authorization: `Bearer ${token}` }
    });
  }, [token]);

  // Fetch marks for selected semester
  useEffect(() => {
    if (selectedSemester && token) {
      const fetchMarks = async () => {
        setLoading(true);
        setError('');
        try {
          console.log('📌 Fetching marks for semester:', selectedSemester);
          const response = await authAxios.get(`/marks/student/semester/${selectedSemester}`);
          console.log('✅ Marks data:', response.data);
          
          setMarks(response.data || []);
          
          // Calculate total and average
          if (response.data && response.data.length > 0) {
            const total = response.data.reduce((sum, mark) => sum + mark.totalMarks, 0);
            setTotalMarks(total);
            
            const avgTotal = total / response.data.length;
            let avg = 'N/A';
            if (avgTotal >= 90) avg = 'O';
            else if (avgTotal >= 80) avg = 'A+';
            else if (avgTotal >= 70) avg = 'A';
            else if (avgTotal >= 60) avg = 'B+';
            else if (avgTotal >= 50) avg = 'B';
            else if (avgTotal >= 40) avg = 'C';
            else avg = 'F';
            setAverageGrade(avg);
          }
        } catch (err) {
          console.error('❌ Error fetching marks:', err);
          setError('Failed to load marks data');
        } finally {
          setLoading(false);
        }
      };
      fetchMarks();
    }
  }, [selectedSemester, authAxios, token]);

  const getGradeColor = (grade) => {
    const colors = {
      'O': '#4caf50',
      'A+': '#66bb6a',
      'A': '#81c784',
      'B+': '#aed581',
      'B': '#dce775',
      'C': '#ffd54f',
      'F': '#ef5350'
    };
    return colors[grade] || '#999';
  };

  return (
    <div className="student-marks-container">
      {/* Top Navigation */}
      <div className="marks-header">
        <button onClick={() => navigate('/student/dashboard')} className="back-button-marks">
          ← Back to Dashboard
        </button>
        <h1>My Marks</h1>
      </div>

      {/* Semester Selection */}
      <div className="semester-selector">
        <label>Select Semester:</label>
        <select 
          value={selectedSemester} 
          onChange={(e) => setSelectedSemester(e.target.value)}
          className="semester-select"
        >
          <option value="">Choose Semester</option>
          {SEMESTERS.map(s => <option key={s.value} value={s.value}>{s.name}</option>)}
        </select>
      </div>

      {/* Error Message */}
      {error && <div className="error-box">❌ {error}</div>}

      {/* Loading State */}
      {loading && <div className="loading-box">Loading marks...</div>}

      {/* Marks Display */}
      {selectedSemester && !loading && marks.length > 0 && (
        <div className="marks-display">
          {/* Summary Cards */}
          <div className="summary-cards">
            <div className="summary-card">
              <div className="summary-value">{marks.length}</div>
              <div className="summary-label">Total Subjects</div>
            </div>
            <div className="summary-card">
              <div className="summary-value">{totalMarks.toFixed(2)}</div>
              <div className="summary-label">Total Marks</div>
            </div>
            <div className="summary-card" style={{ backgroundColor: getGradeColor(averageGrade) }}>
              <div className="summary-value" style={{ color: 'white' }}>{averageGrade}</div>
              <div className="summary-label" style={{ color: 'white' }}>Average Grade</div>
            </div>
          </div>

          {/* Marks Table */}
          <div className="marks-table-container">
            <table className="marks-table">
              <thead>
                <tr>
                  <th>Subject Code</th>
                  <th>Subject Name</th>
                  <th>Internal</th>
                  <th>External</th>
                  <th>Total</th>
                  <th>Grade</th>
                </tr>
              </thead>
              <tbody>
                {marks.map((mark, index) => (
                  <tr key={index}>
                    <td>{mark.subjectCode}</td>
                    <td>{mark.subjectName}</td>
                    <td>{mark.internalMarks}</td>
                    <td>{mark.externalMarks}</td>
                    <td className="total-marks">{mark.totalMarks}</td>
                    <td>
                      <span 
                        className="grade-badge"
                        style={{ backgroundColor: getGradeColor(mark.grade) }}
                      >
                        {mark.grade}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* No Marks Message */}
      {selectedSemester && !loading && marks.length === 0 && (
        <div className="no-marks-box">
          📋 No marks published yet for {SEMESTERS.find(s => s.value === parseInt(selectedSemester))?.name}
        </div>
      )}
    </div>
  );
};

export default StudentMarksView;
