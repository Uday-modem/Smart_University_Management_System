import React, { useState, useContext, useMemo, useEffect } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import './StaffManagement.css';

const API_BASE_URL = 'http://localhost:8080/api';

// ✅ BRANCH MAPPING: Full names to short codes
const BRANCH_MAP = {
  'Computer Science': 'CSE',
  'Electronics & Communication': 'ECE',
  'Mechanical': 'MECH',
  'Civil': 'CIVIL',
  'Electrical': 'EEE'
};

const BRANCH_DISPLAY = [
  { label: 'Computer Science', value: 'Computer Science' },
  { label: 'Electronics & Communication', value: 'Electronics & Communication' },
  { label: 'Mechanical', value: 'Mechanical' },
  { label: 'Civil', value: 'Civil' },
  { label: 'Electrical', value: 'Electrical' }
];

const StaffManagement = () => {
  const navigate = useNavigate();
  const { token } = useContext(AuthContext);
  const [activeTab, setActiveTab] = useState('add');

  const authAxios = useMemo(() => {
    console.log('🔑 Creating authAxios with token:', token ? token.substring(0, 30) + '...' : 'MISSING');
    return axios.create({
      baseURL: API_BASE_URL,
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  }, [token]);

  return (
    <div className="staff-management-container">
      <button onClick={() => navigate('/admin/dashboard')} className="back-button">
        ← Back to Dashboard
      </button>
      <h2>Staff Management</h2>
      <div className="tab-buttons">
        <button className={activeTab === 'add' ? 'tab-active' : 'tab-inactive'} onClick={() => setActiveTab('add')}>
          Add Staff
        </button>
        <button className={activeTab === 'manage' ? 'tab-active' : 'tab-inactive'} onClick={() => setActiveTab('manage')}>
          Manage Staff
        </button>
      </div>
      {activeTab === 'add' ? (<AddStaffTab authAxios={authAxios} />) : (<ManageStaffTab authAxios={authAxios} />)}
    </div>
  );
};

const AddStaffTab = ({ authAxios }) => {
    const [formData, setFormData] = useState({ name: '', email: '', phone: '', staffId: '', branch: '', subjects: '' });
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ type: '', text: '' });

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!formData.name || !formData.email || !formData.staffId || !formData.branch || !formData.subjects) {
            setMessage({ type: 'error', text: 'Please fill all required fields' });
            setTimeout(() => setMessage({ type: '', text: '' }), 3000);
            return;
        }
        setLoading(true);
        try {
            // ✅ Convert full branch name to short code
            const branchCode = BRANCH_MAP[formData.branch];
            console.log(`🔄 Converting branch: '${formData.branch}' → '${branchCode}'`);

            const staffResponse = await authAxios.post('/staff', {
                name: formData.name.trim(),
                email: formData.email.trim(),
                phone: formData.phone?.trim() || null,
                staffId: formData.staffId.trim(),
                branch: branchCode  // ✅ Send mapped code to backend
            });
            
            const staffIdValue = staffResponse.data.id;
            const subjectsArray = formData.subjects.split(',').map(s => s.trim()).filter(s => s.length > 0);
            const expertisePromises = subjectsArray.map(subject =>
                authAxios.post('/staff/expertise', { staffId: staffIdValue, subject: subject })
            );
            await Promise.all(expertisePromises);

            setMessage({ type: 'success', text: 'Staff member added successfully!' });
            setFormData({ name: '', email: '', phone: '', staffId: '', branch: '', subjects: '' });
            setTimeout(() => setMessage({ type: '', text: '' }), 3000);
        } catch (err) {
            console.error("Add error:", err);
            const errorMsg = err.response?.data?.message || err.response?.data || 'Failed to add staff member';
            setMessage({ type: 'error', text: errorMsg });
            setTimeout(() => setMessage({ type: '', text: '' }), 5000);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="add-staff-container">
            <form onSubmit={handleSubmit} className="staff-form">
                <div className="form-group"><label>Name*</label><input type="text" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} placeholder="Enter full name" required /></div>
                <div className="form-group"><label>Staff ID (for RFID)*</label><input type="text" value={formData.staffId} onChange={(e) => setFormData({ ...formData, staffId: e.target.value })} placeholder="e.g., ECESTF001" required /><small>Unique ID that will be encoded on RFID card</small></div>
                <div className="form-group"><label>Email*</label><input type="email" value={formData.email} onChange={(e) => setFormData({ ...formData, email: e.target.value })} placeholder="example@university.com" required /></div>
                <div className="form-group"><label>Phone</label><input type="tel" value={formData.phone} onChange={(e) => setFormData({ ...formData, phone: e.target.value })} placeholder="9876543210" /></div>
                <div className="form-group">
                  <label>Branch*</label>
                  <select value={formData.branch} onChange={(e) => setFormData({ ...formData, branch: e.target.value })} required>
                    <option value="">Select Branch</option>
                    {BRANCH_DISPLAY.map(b => (<option key={b.value} value={b.value}>{b.label}</option>))}
                  </select>
                </div>
                <div className="form-group"><label>Subjects (comma-separated)*</label><textarea value={formData.subjects} onChange={(e) => setFormData({ ...formData, subjects: e.target.value })} placeholder="e.g., Algorithms, Data Structures, Machine Learning" rows="3" required /><small>Enter subjects separated by commas</small></div>
                {message.text && (<div className={`message ${message.type}`}>{message.text}</div>)}
                <button type="submit" className="submit-button" disabled={loading}>{loading ? 'Adding...' : 'Add Staff Member'}</button>
            </form>
        </div>
    );
};

const ManageStaffTab = ({ authAxios }) => {
  const [staffList, setStaffList] = useState([]);
  const [selectedBranch, setSelectedBranch] = useState('');
  const [loading, setLoading] = useState(false);
  const [editingStaff, setEditingStaff] = useState(null);
  const [message, setMessage] = useState({ type: '', text: '' });

  const fetchStaffByBranch = async (branchDisplayName) => {
    if (!branchDisplayName) {
      setStaffList([]);
      return;
    }
    
    setLoading(true);
    try {
      // ✅ Convert full branch name to short code for API call
      const branchCode = BRANCH_MAP[branchDisplayName];
      console.log(`🔍 Fetching staff for: '${branchDisplayName}' (Code: '${branchCode}')`);
      
      const response = await authAxios.get(`/staff/by-branch/${branchCode}`);
      console.log('✅ Staff fetched successfully:', response.data);
      setStaffList(response.data || []);
    } catch (err) {
      console.error('❌ Error fetching staff:', err);
      setStaffList([]);
      setMessage({ type: 'error', text: `Failed to load staff: ${err.response?.data || err.message}` });
      setTimeout(() => setMessage({ type: '', text: '' }), 5000);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStaffByBranch(selectedBranch);
  }, [selectedBranch]);

  const handleUpdate = async () => {
    if (!editingStaff || !editingStaff.name || !editingStaff.email || !editingStaff.staffId) {
      setMessage({ type: 'error', text: 'Name, Email, and Staff ID are required.' });
      setTimeout(() => setMessage({ type: '', text: '' }), 3000);
      return;
    }
    try {
      // ✅ TRIM ALL INPUTS before sending
      const payload = {
          ...editingStaff,
          staffId: String(editingStaff.staffId).trim(),
          email: String(editingStaff.email).trim(),
          name: String(editingStaff.name).trim(),
          phone: editingStaff.phone ? String(editingStaff.phone).trim() : ''
      };

      console.log('📤 Sending update payload:', payload);
      
      const response = await authAxios.put(`/staff/${editingStaff.id}`, payload);
      
      console.log('✅ Update successful:', response.data);
      setMessage({ type: 'success', text: 'Staff updated successfully!' });
      setEditingStaff(null);
      fetchStaffByBranch(selectedBranch);
      setTimeout(() => setMessage({ type: '', text: '' }), 3000);
      
    } catch (err) {
      console.error("Update Error:", err);
      console.error("Error Status:", err.response?.status);
      console.error("Error Data:", err.response?.data);
      
      // Extract error message from response
      let errorMsg = 'Failed to update staff.';
      
      if (err.response?.status === 409) {
        // 409 Conflict - Staff ID already in use
        errorMsg = err.response?.data || '⚠️ This Staff ID is already in use. Please choose a different one.';
      } else if (err.response?.data) {
        errorMsg = typeof err.response.data === 'object' ? JSON.stringify(err.response.data) : err.response.data;
      } else if (err.message) {
        errorMsg = err.message;
      }
      
      setMessage({ type: 'warning', text: errorMsg });
      setTimeout(() => setMessage({ type: '', text: '' }), 5000);
    }
  };

  const handleDelete = async (id, name) => {
    if (window.confirm(`Are you sure you want to delete ${name}? This will also delete all their expertise records.`)) {
      try {
        await authAxios.delete(`/staff/${id}`);
        setMessage({ type: 'success', text: 'Staff deleted successfully!' });
        fetchStaffByBranch(selectedBranch);
        setTimeout(() => setMessage({ type: '', text: '' }), 3000);
      } catch (err) {
        setMessage({ type: 'error', text: 'Failed to delete staff.' });
        setTimeout(() => setMessage({ type: '', text: '' }), 5000);
      }
    }
  };

  return (
    <div className="manage-staff-container">
      <div className="form-group" style={{ marginBottom: '20px' }}>
        <label>Select Branch to View Staff</label>
        <select value={selectedBranch} onChange={(e) => setSelectedBranch(e.target.value)}>
          <option value="">-- Select a Branch --</option>
          {BRANCH_DISPLAY.map(b => (<option key={b.value} value={b.value}>{b.label}</option>))}
        </select>
      </div>

      {message.text && <div className={`message ${message.type}`}>{message.text}</div>}
      
      {loading && <div className="loading">Loading staff...</div>}

      {!loading && !selectedBranch && <div className="no-data">Please select a branch to view staff.</div>}
      
      {!loading && selectedBranch && staffList.length === 0 && <div className="no-data">No staff members found for {selectedBranch}.</div>}

      {!loading && staffList.length > 0 && (
        <div className="staff-table-container">
          <table className="staff-table">
            <thead><tr><th>Staff ID</th><th>Name</th><th>Email</th><th>Phone</th><th>Actions</th></tr></thead>
            <tbody>
              {staffList.map(staff => (
                <tr key={staff.id}>
                  <td>{staff.staffId || '-'}</td>
                  <td>{staff.name}</td>
                  <td>{staff.email}</td>
                  <td>{staff.phone || '-'}</td>
                  <td>
                    <button className="edit-button" onClick={() => setEditingStaff({...staff})}>Edit</button>
                    <button className="delete-button" onClick={() => handleDelete(staff.id, staff.name)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {editingStaff && (
        <div className="modal-overlay">
          <div className="modal-content-large">
            <h3>Edit Staff Member</h3>
            {message.text && <div className={`message ${message.type}`}>{message.text}</div>}
            <div className="form-group"><label>Name*</label><input type="text" value={editingStaff.name} onChange={(e) => setEditingStaff({...editingStaff, name: e.target.value})} required /></div>
            <div className="form-group"><label>Staff ID (RFID)*</label><input type="text" value={editingStaff.staffId || ''} onChange={(e) => setEditingStaff({...editingStaff, staffId: e.target.value})} required /><small>Unique ID for RFID card - If changed, must be unique across all staff</small></div>
            <div className="form-group"><label>Email*</label><input type="email" value={editingStaff.email} onChange={(e) => setEditingStaff({...editingStaff, email: e.target.value})} required /></div>
            <div className="form-group"><label>Phone</label><input type="tel" value={editingStaff.phone || ''} onChange={(e) => setEditingStaff({...editingStaff, phone: e.target.value})} /></div>
            <div className="modal-buttons">
              <button onClick={handleUpdate} className="save-button">Update</button>
              <button onClick={() => setEditingStaff(null)} className="cancel-button">Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default StaffManagement;
