import React, { useState, useEffect, useContext, useMemo } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './AdminTimetable.css';
import { getBranchesForDropdown } from '../utils/branchMapping';

const API_BASE_URL = 'http://localhost:8080/api';

const AdminTimetable = () => {
  const navigate = useNavigate();
  const [branches, setBranches] = useState([]);
  const [years, setYears] = useState([]);
  const [semesters, setSemesters] = useState([]);
  const [sections, setSections] = useState([]);
  const [timetable, setTimetable] = useState([]);
  const [allStaff, setAllStaff] = useState([]);
  const [staffList, setStaffList] = useState([]);
  const [staffExpertise, setStaffExpertise] = useState([]);

  const [selectedBranch, setSelectedBranch] = useState('');
  const [selectedYear, setSelectedYear] = useState('');
  const [selectedSemester, setSelectedSemester] = useState('');
  const [selectedSection, setSelectedSection] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [showAddModal, setShowAddModal] = useState(false);
  const [editingEntry, setEditingEntry] = useState(null);
  const [newEntry, setNewEntry] = useState({ 
    dayOfWeek: 'Monday', 
    timeSlot: '', 
    subject: '', 
    staffId: '', 
    room: '' 
  });
  const [useOtherBranch, setUseOtherBranch] = useState(false);
  const [selectedStaffBranch, setSelectedStaffBranch] = useState('');

  const { token } = useContext(AuthContext);

  const authAxios = useMemo(() => axios.create({
      baseURL: API_BASE_URL,
      headers: { Authorization: `Bearer ${token}` }
  }), [token]);

  // Fetch all staff on mount (Robust Fetch for Name Lookup)
  useEffect(() => {
    const fetchAllStaff = async () => {
      if (!token) return;
      try {
        const res = await authAxios.get('/staff'); 
        setAllStaff(res.data || []);
      } catch (err) {
        console.error('Failed to fetch all staff:', err);
      }
    };
    fetchAllStaff();
  }, [token, authAxios]);

  // Fetch branches
  useEffect(() => {
    if (!token) return;
    const fetchBranches = async () => {
      try {
        const res = await authAxios.get('/sections/branches');
        setBranches(getBranchesForDropdown(res.data || []));
      } catch {
        setError('Failed to load branches.');
      }
    };
    fetchBranches();
  }, [token, authAxios]);

  // Fetch years
  useEffect(() => {
    if (!selectedBranch || !token) return;
    const fetchYears = async () => {
      try {
        const res = await authAxios.get('/sections/years', { params: { branch: selectedBranch } });
        setYears(res.data || []);
      } catch {
        setYears([]);
      }
    };
    fetchYears();
    setYears([]);
    setSemesters([]);
    setSections([]);
    setSelectedYear('');
    setSelectedSemester('');
    setSelectedSection('');
  }, [selectedBranch, token, authAxios]);

  // Fetch semesters for the selected year
  useEffect(() => {
    if (!selectedYear || !token) return;
    const fetchSemesters = async () => {
      try {
        const res = await authAxios.get('/semesters', { params: { year: selectedYear } });
        const data = res.data;
        let semList = [];
        if (Array.isArray(data)) {
          semList = data;
        } else if (data && Array.isArray(data.semesters)) {
          semList = data.semesters;
        } else if (data) {
          semList = Object.values(data).find(val => Array.isArray(val)) || [];
        }
        setSemesters(semList);
      } catch {
        setSemesters([]);
      }
    };
    fetchSemesters();
    setSemesters([]);
    setSections([]);
    setSelectedSemester('');
    setSelectedSection('');
  }, [selectedYear, token, authAxios]);

  // Fetch sections for the selected branch and year
  useEffect(() => {
    if (!selectedSemester || !token) return;
    const fetchSections = async () => {
      try {
        const res = await authAxios.get('/sections', { 
          params: { 
            branch: selectedBranch, 
            year: parseInt(selectedYear)
          } 
        });
        const sectionsData = Array.isArray(res.data) 
          ? res.data 
          : (res.data.sections || []);
        setSections(sectionsData);
      } catch {
        setSections([]);
      }
    };
    fetchSections();
    setSections([]);
    setSelectedSection('');
  }, [selectedSemester, selectedYear, selectedBranch, token, authAxios]);

  useEffect(() => {
    const fetchTimetable = async (sectionId) => {
      if (!token) return;
      setLoading(true);
      try {
        const response = await authAxios.get(`/timetable/section/${sectionId}`);
        setTimetable(response.data || []);
      } catch {
        setError('Failed to load timetable.');
      } finally {
        setLoading(false);
      }
    };

    if (selectedSection && token) {
      fetchTimetable(selectedSection);
      fetchStaffByBranch(selectedBranch);
    } else {
      setTimetable([]);
    }
  }, [selectedSection, authAxios, token, selectedBranch]);

  const fetchStaffByBranch = async (branch) => {
    if (!branch || !token) {
      setStaffList([]);
      return;
    }
    try {
      const response = await authAxios.get(
        `/staff/by-branch/${encodeURIComponent(branch)}`
      );
      setStaffList(response.data || []);

      try {
        const expResponse = await authAxios.get(`/staff/expertise/branch/${encodeURIComponent(branch)}`);
        setStaffExpertise(expResponse.data || []);
      } catch (e) {
        console.warn("Could not fetch expertise details", e);
      }

    } catch {
      setError('Failed to load staff for the selected branch.');
      setStaffList([]);
    }
  };

  const handleOpenAddModal = () => {
    setNewEntry({ 
      dayOfWeek: 'Monday', 
      timeSlot: '', 
      subject: '', 
      staffId: '', 
      room: '' 
    });
    setUseOtherBranch(false);
    setSelectedStaffBranch('');
    if (selectedBranch) {
      fetchStaffByBranch(selectedBranch);
    }
    setShowAddModal(true);
  };

  const handleAddEntry = async () => {
    if (!newEntry.dayOfWeek || !newEntry.timeSlot || !newEntry.subject || 
        !newEntry.staffId || !newEntry.room) {
      setError('Please fill all fields.');
      setTimeout(() => setError(''), 3000);
      return;
    }
    setLoading(true);
    try {
      const response = await authAxios.post('/timetable', { 
        ...newEntry, 
        sectionId: selectedSection,
        staffId: parseInt(newEntry.staffId)
      });
      setTimetable([...timetable, response.data]);
      setShowAddModal(false);
      setSuccess('Class added!');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error("Error response:", err.response);
      setError(err.response?.data?.message || err.response?.data || 'Failed to add class.');
      setTimeout(() => setError(''), 5000);
    } finally {
      setLoading(false);
    }
  };

  // ✅ FIXED: Staff conversion logic here
  const handleOpenEditModal = (entry) => {
    fetchStaffByBranch(selectedBranch);
    
    // Find staff by staffId string and set numeric ID for dropdown matching
    const staffMember = allStaff.find(s => s.staffId === entry.staffId);
    const numericStaffId = staffMember ? staffMember.id : entry.staffId;
    
    setEditingEntry({
      ...entry, 
      staffId: numericStaffId  // Use numeric ID for dropdown
    });
  };

  const handleUpdateEntry = async () => {
    if (!editingEntry) return;
    setLoading(true);
    try {
      // ✅ Ensure staffId is a number for the backend conversion
      const updateData = {
        subject: editingEntry.subject,
        room: editingEntry.room,
        staffId: parseInt(editingEntry.staffId)
      };
      
      const response = await authAxios.put(
        `/timetable/${editingEntry.id}`, 
        updateData
      );
      
          // ✅ WITH THIS:
    const updatedEntry = {
      ...editingEntry,
      ...response.data
    };
    setTimetable(timetable.map(t => 
      t.id === editingEntry.id ? updatedEntry : t
    ));

      setEditingEntry(null);
      setSuccess('Class updated!');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error("Update error:", err.response);
      setError(err.response?.data?.message || 'Failed to update class.');
      setTimeout(() => setError(''), 5000);
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteEntry = async (id) => {
    if (window.confirm('Are you sure you want to delete this class?')) {
      try {
        await authAxios.delete(`/timetable/${id}`);
        setTimetable(timetable.filter(t => t.id !== id));
        setSuccess('Class deleted.');
        setTimeout(() => setSuccess(''), 3000);
      } catch {
        setError('Failed to delete class.');
        setTimeout(() => setError(''), 5000);
      }
    }
  };

  const handleOtherBranchChange = (e) => {
    setUseOtherBranch(e.target.checked);
    setNewEntry({ ...newEntry, staffId: '' });
    if (e.target.checked) {
      setStaffList([]);
    } else {
      fetchStaffByBranch(selectedBranch);
    }
  };

  const handleBranchSelectChange = (branch) => {
    setSelectedStaffBranch(branch);
    fetchStaffByBranch(branch);
  };

  const getStaffName = (staffId) => {
    if (!staffId) return 'Unknown Staff';
    
    if (typeof staffId === 'object' && staffId !== null) {
      return staffId.name || 'Unknown Staff';
    }

    const staff = allStaff.find(s => s.id == staffId || s.staffId === staffId);
    return staff ? staff.name : 'Unknown Staff';
  };

  const getStaffSubjects = (staffId) => {
    return staffExpertise
      .filter(e => e.staff?.id === parseInt(staffId))
      .map(e => e.subject)
      .join(', ') || 'No subjects listed';
  };

  const days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  const uniqueTimeSlots = timetable && timetable.length > 0 
  ? [...new Set(timetable.map(item => item.timeSlot))].sort() 
  : [];

  return (
    <div className="admin-timetable-container">
      {/* Back Button */}
      <button
        onClick={() => navigate('/admin/dashboard')}
        style={{
          position: 'absolute',
          top: '20px',
          left: '20px',
          padding: '10px 20px',
          background: '#764ba2',
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
          boxShadow: '0 2px 8px rgba(118, 75, 162, 0.3)'
        }}
        onMouseEnter={(e) => {
          e.currentTarget.style.background = '#9b6bc7';
          e.currentTarget.style.transform = 'translateY(-2px)';
        }}
        onMouseLeave={(e) => {
          e.currentTarget.style.background = '#764ba2';
          e.currentTarget.style.transform = 'translateY(0)';
        }}
      >
        ← Back to Dashboard
      </button>
      <h2 style={{ marginTop: '60px' }}>Admin Timetable Management</h2>

      {/* Filter Section */}
      <div className="filter-section">
        <select 
          value={selectedBranch} 
          onChange={e => setSelectedBranch(e.target.value)}
        >
          <option value="">Select Branch</option>
          {branches.map(b => (
            <option key={b.code} value={b.code}>{b.name}</option>
          ))}
        </select>

        <select 
          value={selectedYear} 
          onChange={e => setSelectedYear(e.target.value)} 
          disabled={!selectedBranch}
        >
          <option value="">Select Year</option>
          {years.map(y => <option key={y} value={y}>Year {y}</option>)}
        </select>

        <select 
          value={selectedSemester} 
          onChange={e => setSelectedSemester(e.target.value)} 
          disabled={!selectedYear}
        >
          <option value="">Select Semester</option>
          {semesters.map((s, index) => {
            const sObj = (typeof s === 'object' && s !== null) ? s : {};
            const semValue = sObj.semesterNumber || sObj.semester_number || (typeof s === 'number' ? s : index + 1);
            const semLabel = sObj.semesterName || sObj.semester_name || `Semester ${semValue}`;
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
        >
          <option value="">Select Section</option>
          {Array.isArray(sections) && sections.map(s => (
            <option key={s.sectionCode} value={s.sectionCode}>
              {s.displayName || s.sectionCode}
            </option>
          ))}
        </select>
      </div>

      {success && <p className="success-message">{success}</p>}
      {error && <p className="error-message">{error}</p>}

      {selectedSection && (
        <div className="button-group">
          <button className="btn btn-add" onClick={handleOpenAddModal}>
            + Add New Class
          </button>
        </div>
      )}

      {selectedSection && (
        <div className="timetable-grid">
          {loading && !showAddModal && !editingEntry ? (
            <div className="info-message">Loading...</div>
          ) : timetable.length === 0 ? (
            <div className="info-message">No classes scheduled.</div>
          ) : (
            <table className="timetable-table">
              <thead>
                <tr>
                  <th>Time</th>
                  {days.map(day => <th key={day}>{day}</th>)}
                </tr>
              </thead>
              <tbody>
                {uniqueTimeSlots.map(slot => (
                  <tr key={slot}>
                    <td className="time-cell">{slot}</td>
                    {days.map(day => {
                      const entry = timetable.find(
                        t => t.dayOfWeek === day && t.timeSlot === slot
                      );
                      return (
                        <td key={day} className="subject-cell">
                          {entry ? (
                            <div className="entry-content">
                              <strong>{entry.subject}</strong>
                              <p>{entry.room}</p>
                              <small>{getStaffName(entry.staffId)}</small>
                              <div className="entry-actions">
                                <button onClick={() => handleOpenEditModal(entry)}>
                                  Edit
                                </button>
                                <button onClick={() => handleDeleteEntry(entry.id)}>
                                  Del
                                </button>
                              </div>
                            </div>
                          ) : (
                            <span>-</span>
                          )}
                        </td>
                      );
                    })}
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* Add Modal */}
      {showAddModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Add New Class</h3>
            <div className="modal-field">
              <label>Day of Week*</label>
              <select 
                value={newEntry.dayOfWeek} 
                onChange={e => setNewEntry({...newEntry, dayOfWeek: e.target.value})}
              >
                {days.map(d => <option key={d} value={d}>{d}</option>)}
              </select>
            </div>
            <div className="modal-field">
              <label>Time Slot*</label>
              <input 
                type="text" 
                placeholder="e.g., 09:00-10:00" 
                value={newEntry.timeSlot} 
                onChange={e => setNewEntry({...newEntry, timeSlot: e.target.value})}
              />
            </div>
            <div className="modal-field">
              <label>Subject*</label>
              <input 
                type="text" 
                value={newEntry.subject} 
                onChange={e => setNewEntry({...newEntry, subject: e.target.value})}
              />
            </div>
            <div className="modal-field">
              <label>Room No*</label>
              <input 
                type="text" 
                value={newEntry.room} 
                onChange={e => setNewEntry({...newEntry, room: e.target.value})}
              />
            </div>
            <div className="modal-field">
              <label>Staff (from {selectedBranch})*</label>
              <select 
                value={useOtherBranch ? '' : newEntry.staffId} 
                onChange={e => setNewEntry({...newEntry, staffId: e.target.value})} 
                disabled={useOtherBranch} 
                required
              >
                <option value="">Select Staff</option>
                {staffList.map(s => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </div>
            <div className="modal-field">
              <label>
                <input 
                  type="checkbox" 
                  checked={useOtherBranch} 
                  onChange={handleOtherBranchChange}
                />
                {' '}Use Staff from Another Branch?
              </label>
            </div>
            {useOtherBranch && (
              <>
                <div className="modal-field">
                  <label>Select Other Branch</label>
                  <select 
                    value={selectedStaffBranch} 
                    onChange={e => handleBranchSelectChange(e.target.value)}
                  >
                    <option value="">Select Branch</option>
                    {branches.filter(b => b.code !== selectedBranch).map(b => (
                      <option key={b.code} value={b.code}>{b.name}</option>
                    ))}
                  </select>
                </div>
                <div className="modal-field">
                  <label>Staff (from {selectedStaffBranch || 'other'})*</label>
                  <select 
                    value={newEntry.staffId} 
                    onChange={e => setNewEntry({...newEntry, staffId: e.target.value})} 
                    required
                  >
                    <option value="">Select Staff</option>
                    {staffList.map(s => (
                      <option key={s.id} value={s.id}>{s.name}</option>
                    ))}
                  </select>
                </div>
              </>
            )}
            {newEntry.staffId && (
              <small>Subjects: {getStaffSubjects(newEntry.staffId)}</small>
            )}

            <div className="modal-buttons">
              <button onClick={handleAddEntry} disabled={loading}>Save</button>
              <button onClick={() => setShowAddModal(false)} disabled={loading}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Modal */}
      {editingEntry && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3>Edit Class</h3>
            <div className="modal-field">
              <label>Subject</label>
              <input 
                type="text" 
                value={editingEntry.subject || ''} 
                onChange={e => setEditingEntry({...editingEntry, subject: e.target.value})}
              />
            </div>
            <div className="modal-field">
              <label>Room</label>
              <input 
                type="text" 
                value={editingEntry.room || ''} 
                onChange={e => setEditingEntry({...editingEntry, room: e.target.value})}
              />
            </div>
            <div className="modal-field">
              <label>Staff</label>
              <select 
                value={editingEntry.staffId || ''} 
                onChange={e => setEditingEntry({
                  ...editingEntry, 
                  staffId: e.target.value
                })}
              >
                <option value="">Select Staff</option>
                {staffList.map(s => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </div>
            <div className="modal-buttons">
              <button onClick={handleUpdateEntry} disabled={loading}>Update</button>
              <button onClick={() => setEditingEntry(null)} disabled={loading}>
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminTimetable;
