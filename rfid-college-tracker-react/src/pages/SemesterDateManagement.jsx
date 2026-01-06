import React, { useState, useEffect, useContext, useMemo } from 'react';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './SemesterDateManagement.css';

const API_BASE_URL = 'http://localhost:8080/api';

const SemesterDateManagement = () => {
  const { token } = useContext(AuthContext);
  const navigate = useNavigate();
  
  const [academicYear, setAcademicYear] = useState('2024-2025');
  const [regulationId, setRegulationId] = useState('');
  const [year, setYear] = useState('');
  const [semester, setSemester] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  
  const [configurations, setConfigurations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const authAxios = useMemo(() => {
    return axios.create({
      baseURL: API_BASE_URL,
      headers: { Authorization: `Bearer ${token}` }
    });
  }, [token]);

  // Fetch existing configurations
  useEffect(() => {
    const fetchConfigurations = async () => {
      try {
        const response = await authAxios.get('/semester-config');
        setConfigurations(response.data || []);
      } catch (err) {
        console.error('Failed to fetch configurations:', err);
      }
    };
    if (token) fetchConfigurations();
  }, [authAxios, token]);

  const handleSave = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const configData = {
        academicYear,
        regulationId: parseInt(regulationId),
        year: parseInt(year),
        semester: parseInt(semester),
        startDate,
        endDate
      };

      const response = await authAxios.post('/semester-config', configData);

      if (response.data.success) {
        setSuccess('Semester dates saved successfully!');
        
        // Refresh configurations
        const refreshResponse = await authAxios.get('/semester-config');
        setConfigurations(refreshResponse.data || []);
        
        // Reset form
        setRegulationId('');
        setYear('');
        setSemester('');
        setStartDate('');
        setEndDate('');
      }
    } catch (err) {
      setError('Failed to save semester dates: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this configuration?')) return;

    try {
      await authAxios.delete(`/semester-config/${id}`);
      setSuccess('Configuration deleted successfully');
      
      // Refresh list
      const response = await authAxios.get('/semester-config');
      setConfigurations(response.data || []);
    } catch (err) {
      setError('Failed to delete configuration');
    }
  };

  return (
    <div className="semester-date-management-container">
      <button onClick={() => navigate('/admin/dashboard')} className="back-button">
        ← Back to Dashboard
      </button>

      <h2>Semester Dates Configuration</h2>

      {error && <div className="error-message">❌ {error}</div>}
      {success && <div className="success-message">✅ {success}</div>}

      {/* Configuration Form */}
      <div className="config-form-section">
        <h3>Add/Update Semester Dates</h3>
        <form onSubmit={handleSave} className="config-form">
          <div className="form-row">
            <div className="form-group">
              <label>Academic Year</label>
              <input
                type="text"
                value={academicYear}
                onChange={(e) => setAcademicYear(e.target.value)}
                placeholder="e.g., 2024-2025"
                required
              />
            </div>

            <div className="form-group">
              <label>Regulation</label>
              <select
                value={regulationId}
                onChange={(e) => setRegulationId(e.target.value)}
                required
              >
                <option value="">Select Regulation</option>
                <option value="1">R21</option>
                <option value="2">R23</option>
                <option value="3">R25</option>
              </select>
            </div>

            <div className="form-group">
              <label>Year</label>
              <select
                value={year}
                onChange={(e) => setYear(e.target.value)}
                required
              >
                <option value="">Select Year</option>
                <option value="1">1st Year</option>
                <option value="2">2nd Year</option>
                <option value="3">3rd Year</option>
                <option value="4">4th Year</option>
              </select>
            </div>

            <div className="form-group">
              <label>Semester</label>
              <select
                value={semester}
                onChange={(e) => setSemester(e.target.value)}
                required
              >
                <option value="">Select Semester</option>
                {[1, 2, 3, 4, 5, 6, 7, 8].map(s => (
                  <option key={s} value={s}>Semester {s}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Start Date</label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label>End Date</label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                required
              />
            </div>
          </div>

          <button type="submit" disabled={loading} className="save-button">
            {loading ? 'Saving...' : 'Save Configuration'}
          </button>
        </form>
      </div>

      {/* Existing Configurations Table */}
      <div className="configurations-table-section">
        <h3>Existing Configurations</h3>
        <div className="table-wrapper">
          <table className="config-table">
            <thead>
              <tr>
                <th>Academic Year</th>
                <th>Regulation</th>
                <th>Year</th>
                <th>Semester</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {configurations.map(config => (
                <tr key={config.id}>
                  <td>{config.academicYear}</td>
                  <td>R{config.regulationId === 1 ? '21' : config.regulationId === 2 ? '23' : '25'}</td>
                  <td>Year {config.year}</td>
                  <td>Semester {config.semester}</td>
                  <td>{config.startDate}</td>
                  <td>{config.endDate}</td>
                  <td>
                    <button 
                      onClick={() => handleDelete(config.id)}
                      className="delete-button"
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default SemesterDateManagement;
