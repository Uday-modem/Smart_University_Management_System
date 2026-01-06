import React, { useState, useEffect, useContext, useMemo } from 'react';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';
import './StudentAttendance.css';

const API_BASE_URL = 'http://localhost:8080/api';

const StudentAttendance = () => {
  const { token } = useContext(AuthContext);
  
  // ==================== STATE ====================
  const [branches, setBranches] = useState([]);
  const [years, setYears] = useState([]);
  const [semesters, setSemesters] = useState([]);
  const [sections, setSections] = useState([]);
  const [selectedBranch, setSelectedBranch] = useState('');
  const [selectedYear, setSelectedYear] = useState('');
  const [selectedSemester, setSelectedSemester] = useState('');
  const [selectedSection, setSelectedSection] = useState('');
  const [selectedMonth, setSelectedMonth] = useState('');
  const [currentYear] = useState(new Date().getFullYear());
  const [attendanceData, setAttendanceData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const authAxios = useMemo(() => {
    return axios.create({
      baseURL: API_BASE_URL,
      headers: { Authorization: `Bearer ${token}` }
    });
  }, [token]);

  // ==================== FETCH BRANCHES ====================
  useEffect(() => {
    if (!token) return;
    const fetchBranches = async () => {
      try {
        const response = await authAxios.get('/sections/branches');
        setBranches(response.data || []);
      } catch (err) {
        console.error('❌ Error fetching branches:', err);
        setError('Failed to load branches');
      }
    };
    fetchBranches();
  }, [authAxios, token]);

  // ==================== FETCH YEARS ====================
  useEffect(() => {
    if (selectedBranch && token) {
      const fetchYears = async () => {
        try {
          const response = await authAxios.get(`/sections/years/${encodeURIComponent(selectedBranch)}`);
          setYears(response.data || []);
        } catch (err) {
          console.error('❌ Error fetching years:', err);
          setYears([1, 2, 3, 4]); // Fallback
        }
      };
      fetchYears();
    }
  }, [selectedBranch, authAxios, token]);

  // ==================== FETCH SEMESTERS ====================
  useEffect(() => {
    if (selectedYear && token) {
      const fetchSemesters = async () => {
        try {
          const response = await authAxios.get('/semesters', { params: { year: selectedYear } });
          setSemesters(response.data || []);
        } catch (err) {
          console.error('❌ Error fetching semesters:', err);
          setSemesters([]);
        }
      };
      fetchSemesters();
    }
  }, [selectedYear, authAxios, token]);

  // ==================== FETCH SECTIONS ====================
  useEffect(() => {
    if (selectedBranch && selectedYear && selectedSemester && token) {
      const fetchSections = async () => {
        try {
          console.log('📌 Fetching sections for:', { selectedBranch, selectedYear, selectedSemester });
          const response = await authAxios.get('/sections', {
            params: { branch: selectedBranch, year: parseInt(selectedYear) }
          });
          
          // ✅ Handle both response structures
          const sectionsArray = Array.isArray(response.data?.sections)
            ? response.data.sections
            : Array.isArray(response.data)
            ? response.data
            : [];
          
          setSections(sectionsArray);
          console.log('✅ Sections loaded:', sectionsArray);
          
          if (sectionsArray.length === 0) {
            setError('No sections found');
          }
        } catch (err) {
          console.error('❌ Error fetching sections:', err);
          setSections([]);
          setError('Failed to load sections');
        }
      };
      fetchSections();
    }
  }, [selectedBranch, selectedYear, selectedSemester, authAxios, token]);

  // ==================== FETCH ATTENDANCE ====================
  useEffect(() => {
    if (selectedSection && selectedMonth && token) {
      const fetchAttendance = async () => {
        setLoading(true);
        setError('');
        try {
          console.log('📌 Fetching attendance for:', { selectedSection, currentYear, selectedMonth });
          
          // ✅ CORRECT: Using the actual working endpoint from AttendanceController
          const response = await authAxios.get(
            `/attendance/view/student/${selectedSection}/${currentYear}/${selectedMonth}`
          );
          
          console.log('✅ Raw Response:', response.data);
          
          // ✅ Handle response and structure it properly
          if (response.data) {
            // If response has students key (object format)
            if (response.data.students || response.data.attendanceGrid) {
              const grid = response.data.students || response.data.attendanceGrid;
              const totalDays = response.data.totalDays || response.data.daysInMonth || new Date(currentYear, selectedMonth, 0).getDate();
              
              // Transform if needed
              const transformedData = {
                attendanceGrid: Array.isArray(grid) ? grid : [grid],
                daysInMonth: totalDays
              };
              
              setAttendanceData(transformedData);
              console.log('✅ Attendance data loaded successfully');
            } 
            // If response is direct array
            else if (Array.isArray(response.data)) {
              setAttendanceData({
                attendanceGrid: response.data,
                daysInMonth: new Date(currentYear, selectedMonth, 0).getDate()
              });
              console.log('✅ Attendance data loaded (array format)');
            } 
            // If response is empty
            else {
              setAttendanceData(null);
              setError('No attendance data available');
            }
          } else {
            setAttendanceData(null);
            setError('No attendance data available');
          }
        } catch (err) {
          console.error('❌ Error fetching attendance:', err);
          if (err.response?.status === 404) {
            setError('Attendance endpoint not found - Check backend route');
          } else if (err.response?.status === 500) {
            setError('Server error - Check backend logs');
          } else {
            setError('Failed to load attendance data');
          }
          setAttendanceData(null);
        } finally {
          setLoading(false);
        }
      };
      fetchAttendance();
    }
  }, [selectedSection, selectedMonth, currentYear, authAxios, token]);

  const MONTHS = [
    { value: 1, name: 'January' }, { value: 2, name: 'February' },
    { value: 3, name: 'March' }, { value: 4, name: 'April' },
    { value: 5, name: 'May' }, { value: 6, name: 'June' },
    { value: 7, name: 'July' }, { value: 8, name: 'August' },
    { value: 9, name: 'September' }, { value: 10, name: 'October' },
    { value: 11, name: 'November' }, { value: 12, name: 'December' }
  ];

  const handleExportExcel = async () => {
    try {
      const monthName = MONTHS.find(m => m.value === parseInt(selectedMonth))?.name || selectedMonth;
      const response = await authAxios.get(
        `/attendance/download/student/${selectedSection}/${currentYear}/${selectedMonth}`,
        { responseType: 'blob' }
      );
      
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `student_attendance_${monthName}_${currentYear}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('❌ Error exporting:', err);
      setError('Failed to export Excel file');
    }
  };

  return (
    <div className="student-attendance-container">
      {/* ==================== FILTERS ==================== */}
      <div className="filter-section">
        <select
          value={selectedBranch}
          onChange={(e) => {
            setSelectedBranch(e.target.value);
            setSelectedYear('');
            setSelectedSemester('');
            setSelectedSection('');
            setSelectedMonth('');
            setAttendanceData(null);
            setError('');
          }}
          className="filter-select"
        >
          <option value="">Select Branch</option>
          {branches.map((b) => (
            <option key={b} value={b}>
              {b}
            </option>
          ))}
        </select>

        <select
          value={selectedYear}
          onChange={(e) => {
            setSelectedYear(e.target.value);
            setSelectedSemester('');
            setSelectedSection('');
            setSelectedMonth('');
            setAttendanceData(null);
            setError('');
          }}
          disabled={!selectedBranch}
          className="filter-select"
        >
          <option value="">Select Year</option>
          {years.map((y) => (
            <option key={y} value={y}>
              Year {y}
            </option>
          ))}
        </select>

        <select
          value={selectedSemester}
          onChange={(e) => {
            setSelectedSemester(e.target.value);
            setSelectedSection('');
            setSelectedMonth('');
            setAttendanceData(null);
            setError('');
          }}
          disabled={!selectedYear}
          className="filter-select"
        >
          <option value="">Select Semester</option>
          {semesters.map((s) => (
            <option key={s.id || s.number} value={s.number || s.id}>
              {s.name || s.semesterName || `Semester ${s.number}`}
            </option>
          ))}
        </select>

        <select
          value={selectedSection}
          onChange={(e) => {
            setSelectedSection(e.target.value);
            setSelectedMonth('');
            setAttendanceData(null);
            setError('');
          }}
          disabled={!selectedSemester || !Array.isArray(sections) || sections.length === 0}
          className="filter-select"
        >
          <option value="">Select Section</option>
          {Array.isArray(sections) && sections.map((s) => (
            <option key={s.sectionCode || s.id} value={s.sectionCode || s.id}>
              {s.displayName || s.sectionName || s.sectionCode || 'Unknown'}
            </option>
          ))}
          {!Array.isArray(sections) && <option disabled>No sections available</option>}
        </select>

        <select
          value={selectedMonth}
          onChange={(e) => {
            setSelectedMonth(e.target.value);
            setError('');
          }}
          disabled={!selectedSection}
          className="filter-select"
        >
          <option value="">Select Month</option>
          {MONTHS.map((m) => (
            <option key={m.value} value={m.value}>
              {m.name}
            </option>
          ))}
        </select>
      </div>

      {error && <div className="error-message">❌ {error}</div>}
      {loading && <div className="loading-message">⏳ Loading attendance data...</div>}

      {/* ==================== ATTENDANCE TABLE ==================== */}
      {selectedMonth && attendanceData && (
        <div className="attendance-grid-container">
          <div className="grid-header">
            <h3>
              {MONTHS.find(m => m.value === parseInt(selectedMonth))?.name} {currentYear} - Student Attendance
            </h3>
            <div className="header-buttons">
              <button onClick={handleExportExcel} className="export-button">
                📥 Download Excel
              </button>
            </div>
          </div>

          {attendanceData.attendanceGrid && attendanceData.attendanceGrid.length > 0 ? (
            <div className="attendance-table-wrapper">
              <table className="attendance-table">
                <thead>
                  <tr>
                    <th>Reg No</th>
                    <th>Student Name</th>
                    {attendanceData.daysInMonth && Array.from({ length: attendanceData.daysInMonth }, (_, i) => (
                      <th key={i}>{i + 1}</th>
                    ))}
                    <th>P</th>
                    <th>L</th>
                    <th>A</th>
                  </tr>
                </thead>
                <tbody>
                  {attendanceData.attendanceGrid.map((student) => (
                    <tr key={student.id || student.studentId || Math.random()}>
                      <td>{student.registrationNumber || student.regNo || student.id || '-'}</td>
                      <td className="student-name">{student.name || student.studentName || '-'}</td>
                      {attendanceData.daysInMonth && Array.from({ length: attendanceData.daysInMonth }, (_, i) => {
                        const day = i + 1;
                        const status = student.attendance && student.attendance[day];
                        return (
                          <td key={day} className="status-cell">
                            {status === 'PRESENT' ? 'P' : status === 'ABSENT' ? 'A' : status === 'LATE' ? 'L' : '-'}
                          </td>
                        );
                      })}
                      <td className="count-cell present-count">{student.presentCount || 0}</td>
                      <td className="count-cell late-count">{student.lateCount || 0}</td>
                      <td className="count-cell absent-count">{student.absentCount || 0}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="no-data-message">📭 No attendance records found for selected month.</div>
          )}
        </div>
      )}

      {selectedMonth && !attendanceData && !loading && !error && (
        <div className="no-data-message">📭 No attendance data available for this selection.</div>
      )}
    </div>
  );
};

export default StudentAttendance;
