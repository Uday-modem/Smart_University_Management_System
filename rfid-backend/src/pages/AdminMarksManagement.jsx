import React, { useState, useEffect, useContext, useMemo } from 'react';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './AdminMarksManagement.css';

const API_BASE_URL = 'http://localhost:8080/api';

const AdminMarksManagement = () => {
  const { token } = useContext(AuthContext);
  const navigate = useNavigate();
  
  const [regulations, setRegulations] = useState([]);
  const [branches, setBranches] = useState([]);
  const [years, setYears] = useState([]);
  const [semesters, setSemesters] = useState([]);
  const [sections, setSections] = useState([]);
  const [subjects, setSubjects] = useState([]);
  const [students, setStudents] = useState([]);
  
  const [selectedRegulation, setSelectedRegulation] = useState('');
  const [selectedBranch, setSelectedBranch] = useState('');
  const [selectedYear, setSelectedYear] = useState('');
  const [selectedSemester, setSelectedSemester] = useState('');
  const [selectedSection, setSelectedSection] = useState('');
  
  const [marks, setMarks] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const authAxios = useMemo(() => {
    return axios.create({
      baseURL: API_BASE_URL,
      headers: { Authorization: `Bearer ${token}` }
    });
  }, [token]);

  // Fetch regulations
  useEffect(() => {
    const fetchRegulations = async () => {
      try {
        const response = await authAxios.get('/regulations');
        setRegulations(response.data || []);
      } catch (err) {
        console.error("Regulations error:", err);
        setError('Failed to load regulations');
      }
    };
    if (token) fetchRegulations();
  }, [authAxios, token]);

  // Fetch branches
  useEffect(() => {
    const fetchBranches = async () => {
      try {
        const response = await authAxios.get('/sections/branches');
        setBranches(response.data || []);
      } catch (err) {
        console.error("Branches error:", err);
        setError('Failed to load branches');
      }
    };
    if (token) fetchBranches();
  }, [authAxios, token]);

  // Fetch years
  useEffect(() => {
    if (selectedBranch && token) {
      const fetchYears = async () => {
        try {
          const response = await authAxios.get(`/sections/years/${encodeURIComponent(selectedBranch)}`);
          setYears(response.data || []);
        } catch (err) {
          console.error("Years error:", err);
          setError('Failed to load years');
        }
      };
      fetchYears();
    }
  }, [selectedBranch, authAxios, token]);

  // Fetch semesters
  useEffect(() => {
    if (selectedYear && token) {
      const fetchSemesters = async () => {
        try {
          const response = await authAxios.get('/semesters', { params: { year: selectedYear } });
          console.log("Semesters API Response:", response.data); // Debug log
          
          // Robust handling: check if array or object wrapper
          const data = response.data;
          let semList = [];
          
          if (Array.isArray(data)) {
            semList = data;
          } else if (data && Array.isArray(data.semesters)) {
            semList = data.semesters;
          } else if (data) {
            // Fallback if it returns a map but not 'semesters' key? (Unlikely, but safe)
            semList = Object.values(data).find(val => Array.isArray(val)) || [];
          }

          setSemesters(semList);
        } catch (err) {
          console.error("Semesters error:", err);
          setError('Failed to load semesters');
        }
      };
      fetchSemesters();
    }
  }, [selectedYear, authAxios, token]);

  // Fetch sections
  useEffect(() => {
    if (selectedBranch && selectedYear && token) {
      const fetchSections = async () => {
        try {
          const response = await authAxios.get('/sections', {
             params: { branch: selectedBranch, year: parseInt(selectedYear) }
          });
          // Check if response.data is an array or an object with a 'sections' key
          const sectionsData = Array.isArray(response.data) 
              ? response.data 
              : (response.data.sections || []);
              
          setSections(sectionsData);
        } catch (err) {
          console.error("Sections error:", err);
          setError('Failed to load sections');
          setSections([]); 
        }
      };
      fetchSections();
    }
  }, [selectedBranch, selectedYear, authAxios, token]);

  // Fetch subjects and students
  useEffect(() => {
    // Check that selectedSemester is not empty/undefined before making request
    if (selectedRegulation && selectedSection && selectedSemester && token) {
      const fetchSubjectsAndStudents = async () => {
        try {
          const subjectsResponse = await authAxios.get(
            `/marks/subjects/${selectedBranch}/${selectedYear}/${selectedSemester}/${selectedRegulation}`
          );
          setSubjects(subjectsResponse.data || []);

          const studentsResponse = await authAxios.get(`/marks/students/section/${selectedSection}`);
          setStudents(studentsResponse.data || []);
        } catch (err) {
          console.error("Subjects/Students error:", err);
          setError('Failed to load subjects or students');
        }
      };
      fetchSubjectsAndStudents();
    }
  }, [selectedRegulation, selectedSection, selectedSemester, selectedBranch, selectedYear, authAxios, token]);

  const handleMarksChange = (studentId, subjectId, field, value) => {
    const key = `${studentId}-${subjectId}`;
    setMarks(prev => ({
      ...prev,
      [key]: {
        ...prev[key],
        studentId,
        subjectId,
        semester: parseInt(selectedSemester),
        [field]: parseFloat(value)
      }
    }));
  };

  const handleSaveMarks = async (studentId, subjectId) => {
    try {
      setLoading(true);
      const key = `${studentId}-${subjectId}`;
      const markData = marks[key];

      if (!markData || (markData.internalMarks === undefined && markData.externalMarks === undefined)) {
        setError('Please enter marks');
        return;
      }

      await authAxios.post('/marks/add', markData);
      setSuccess('Marks saved successfully!');
      setError('');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError('Failed to save marks');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="marks-management-container">
      <button onClick={() => navigate('/admin/dashboard')} className="back-button">
        ← Back to Dashboard
      </button>
      <h2>Manage Student Marks</h2>
      
      {/* Filter Section */}
      <div className="filter-section">
        <select 
          value={selectedRegulation} 
          onChange={e => setSelectedRegulation(e.target.value)}
          className="filter-select"
        >
          <option value="">Select Regulation</option>
          {regulations.map(r => (
            <option key={r.id} value={r.id}>{r.regulationCode} - {r.regulationName}</option>
          ))}
        </select>

        <select 
          value={selectedBranch} 
          onChange={e => setSelectedBranch(e.target.value)}
          disabled={!selectedRegulation}
          className="filter-select"
        >
          <option value="">Select Branch</option>
          {branches.map(b => (
            <option key={b} value={b}>{b}</option>
          ))}
        </select>

        <select 
          value={selectedYear} 
          onChange={e => setSelectedYear(e.target.value)}
          disabled={!selectedBranch}
          className="filter-select"
        >
          <option value="">Select Year</option>
          {years.map(y => (
            <option key={y} value={y}>Year {y}</option>
          ))}
        </select>

        {/* ✅ FIXED: Semester Dropdown (Robust handling for null/missing fields) */}
        <select 
          value={selectedSemester} 
          onChange={e => setSelectedSemester(e.target.value)}
          disabled={!selectedYear}
          className="filter-select"
        >
          <option value="">Select Semester</option>
          {semesters.map((s, index) => {
            // Safely extract fields, handling potential nulls or different casing
            const sObj = (typeof s === 'object' && s !== null) ? s : {};
            
            // Try finding the number: semesterNumber -> semester_number -> primitive -> index
            const semValue = sObj.semesterNumber || sObj.semester_number || (typeof s === 'number' ? s : index + 1);
            
            // Try finding the name: semesterName -> semester_name -> fallback
            const semLabel = sObj.semesterName || sObj.semester_name || `Semester ${semValue}`;
            
            // Unique key
            const semKey = sObj.id || `sem-${semValue}-${index}`;

            return (
              <option key={semKey} value={semValue}>
                {semLabel}
              </option>
            );
          })}
        </select>

        <select 
          value={selectedSection} 
          onChange={e => setSelectedSection(e.target.value)}
          disabled={!selectedSemester}
          className="filter-select"
        >
          <option value="">Select Section</option>
          {Array.isArray(sections) && sections.map(s => (
            <option key={s.sectionCode} value={s.sectionCode}>
              {s.displayName || s.sectionCode}
            </option>
          ))}
        </select>
      </div>

      {/* Marks Entry Table */}
      {selectedSection && subjects.length > 0 && students.length > 0 && (
        <div className="marks-table-container">
          <h3>Enter Marks for {selectedSemester ? `Semester ${selectedSemester}` : 'Selected Semester'}</h3>
          
          {error && <div className="error-message">❌ {error}</div>}
          {success && <div className="success-message">✅ {success}</div>}

          <div className="table-wrapper">
            <table className="marks-table">
              <thead>
                <tr>
                  <th>Reg No</th>
                  <th>Student Name</th>
                  {subjects.map(subject => (
                    <th key={subject.id}>{subject.subjectCode}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {students.map(student => (
                  <tr key={student.id}>
                    <td>{student.registrationNumber}</td>
                    <td>{student.name}</td>
                    {subjects.map(subject => (
                      <td key={`${student.id}-${subject.id}`}>
                        <div className="marks-input-group">
                          <input
                            type="number"
                            placeholder="Int"
                            min="0"
                            max="40"
                            onChange={e => handleMarksChange(student.id, subject.id, 'internalMarks', e.target.value)}
                            className="marks-input"
                          />
                          <input
                            type="number"
                            placeholder="Ext"
                            min="0"
                            max="60"
                            onChange={e => handleMarksChange(student.id, subject.id, 'externalMarks', e.target.value)}
                            className="marks-input"
                          />
                          <button
                            onClick={() => handleSaveMarks(student.id, subject.id)}
                            disabled={loading}
                            className="save-button"
                          >
                            Save
                          </button>
                        </div>
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminMarksManagement;
