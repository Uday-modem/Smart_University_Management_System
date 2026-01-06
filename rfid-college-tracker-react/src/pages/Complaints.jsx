import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const Complaints = () => {
  const { token } = useContext(AuthContext);
  const navigate = useNavigate();
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL'); // ALL, PENDING, RESOLVED

  useEffect(() => {
    fetchComplaints();
  }, []);

  const fetchComplaints = async () => {
    setLoading(true);
    setError('');

    try {
      const response = await axios.get(`${API_BASE_URL}/complaints/all`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      console.log('✅ Fetched complaints:', response.data);
      setComplaints(response.data);
    } catch (err) {
      console.error('❌ Error fetching complaints:', err);
      setError('Failed to load complaints');
    } finally {
      setLoading(false);
    }
  };

  const handleResolve = async (id) => {
    if (!window.confirm('Mark this complaint as resolved?')) return;

    try {
      await axios.put(`${API_BASE_URL}/complaints/${id}/resolve`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      alert('✅ Complaint marked as resolved');
      fetchComplaints(); // Refresh list
    } catch (err) {
      console.error('❌ Error resolving complaint:', err);
      alert('Failed to resolve complaint');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this complaint? This action cannot be undone.')) return;

    try {
      await axios.delete(`${API_BASE_URL}/complaints/${id}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      alert('✅ Complaint deleted successfully');
      fetchComplaints(); // Refresh list
    } catch (err) {
      console.error('❌ Error deleting complaint:', err);
      alert('Failed to delete complaint');
    }
  };

  const getFilteredComplaints = () => {
    if (filter === 'PENDING') {
      return complaints.filter(c => c.status === 'PENDING');
    } else if (filter === 'RESOLVED') {
      return complaints.filter(c => c.status === 'RESOLVED');
    }
    return complaints;
  };

  const getIssueTypeDisplay = (type) => {
    const types = {
      'STAFF_RELATED': 'Staff Related',
      'ADMINISTRATION_RELATED': 'Administration Related',
      'CLASS_RELATED': 'Class Related',
      'APPLICATION_RELATED': 'Application Related',
      'INFRASTRUCTURE_RELATED': 'Infrastructure Related',
      'OTHER': 'Other'
    };
    return types[type] || type;
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-IN', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const filteredComplaints = getFilteredComplaints();

  return (
    <div style={{ minHeight: '100vh', background: '#f5f7fa', fontFamily: 'Segoe UI, sans-serif' }}>
      {/* Header */}
      <div style={{
        background: 'white',
        padding: '20px 30px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
        marginBottom: '30px'
      }}>
        <div style={{
          maxWidth: '1200px',
          margin: '0 auto',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center'
        }}>
          <h1 style={{ margin: 0, fontSize: '24px', fontWeight: 600, color: '#333' }}>
            📮 Student Complaints
          </h1>
          <button
            onClick={() => navigate('/admin/dashboard')}
            style={{
              padding: '10px 20px',
              background: '#764ba2',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              fontSize: '14px',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            ← Back to Dashboard
          </button>
        </div>
      </div>
      {/* Main Content */}
      <div style={{
        maxWidth: '1200px',
        margin: '0 auto',
        padding: '0 20px'
      }}>
        {/* Filter Tabs */}
        <div style={{
          display: 'flex',
          gap: '12px',
          marginBottom: '24px'
        }}>
          <button
            onClick={() => setFilter('ALL')}
            style={{
              padding: '10px 20px',
              background: filter === 'ALL' ? '#764ba2' : 'white',
              color: filter === 'ALL' ? 'white' : '#666',
              border: filter === 'ALL' ? 'none' : '1px solid #ddd',
              borderRadius: '8px',
              fontSize: '14px',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            All ({complaints.length})
          </button>
          <button
            onClick={() => setFilter('PENDING')}
            style={{
              padding: '10px 20px',
              background: filter === 'PENDING' ? '#ff9800' : 'white',
              color: filter === 'PENDING' ? 'white' : '#666',
              border: filter === 'PENDING' ? 'none' : '1px solid #ddd',
              borderRadius: '8px',
              fontSize: '14px',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            Pending ({complaints.filter(c => c.status === 'PENDING').length})
          </button>
          <button
            onClick={() => setFilter('RESOLVED')}
            style={{
              padding: '10px 20px',
              background: filter === 'RESOLVED' ? '#4caf50' : 'white',
              color: filter === 'RESOLVED' ? 'white' : '#666',
              border: filter === 'RESOLVED' ? 'none' : '1px solid #ddd',
              borderRadius: '8px',
              fontSize: '14px',
              fontWeight: 600,
              cursor: 'pointer'
            }}
          >
            Resolved ({complaints.filter(c => c.status === 'RESOLVED').length})
          </button>
        </div>
        {/* Loading State */}
        {loading && (
          <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
            Loading complaints...
          </div>
        )}
        {/* Error State */}
        {error && (
          <div style={{
            padding: '20px',
            background: '#ffebee',
            borderLeft: '4px solid #f44336',
            borderRadius: '8px',
            color: '#c62828'
          }}>
            {error}
          </div>
        )}
        {/* Complaints Table */}
        {!loading && !error && (
          <div style={{
            background: 'white',
            borderRadius: '12px',
            overflow: 'hidden',
            boxShadow: '0 2px 12px rgba(0,0,0,0.08)'
          }}>
            {filteredComplaints.length === 0 ? (
              <div style={{
                padding: '60px',
                textAlign: 'center',
                color: '#999'
              }}>
                <div style={{ fontSize: '48px', marginBottom: '16px' }}>📭</div>
                <p style={{ fontSize: '16px', margin: 0 }}>
                  No {filter.toLowerCase()} complaints found
                </p>
              </div>
            ) : (
              <table style={{
                width: '100%',
                borderCollapse: 'collapse'
              }}>
                <thead>
                  <tr style={{ background: '#f5f5f5' }}>
                    <th style={{ padding: '16px', textAlign: 'left', fontSize: '13px', fontWeight: 600, color: '#666' }}>Student</th>
                    <th style={{ padding: '16px', textAlign: 'left', fontSize: '13px', fontWeight: 600, color: '#666' }}>Issue Type</th>
                    <th style={{ padding: '16px', textAlign: 'left', fontSize: '13px', fontWeight: 600, color: '#666' }}>Description</th>
                    <th style={{ padding: '16px', textAlign: 'left', fontSize: '13px', fontWeight: 600, color: '#666' }}>Date</th>
                    <th style={{ padding: '16px', textAlign: 'center', fontSize: '13px', fontWeight: 600, color: '#666' }}>Status</th>
                    <th style={{ padding: '16px', textAlign: 'center', fontSize: '13px', fontWeight: 600, color: '#666' }}>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredComplaints.map((complaint) => (
                    <tr key={complaint.id} style={{ borderBottom: '1px solid #f0f0f0' }}>
                      <td style={{ padding: '16px' }}>
                        <div style={{ fontSize: '14px', fontWeight: 600, color: '#333', marginBottom: '4px' }}>
                          {complaint.studentName}
                        </div>
                        <div style={{ fontSize: '12px', color: '#666' }}>
                          {complaint.studentRegistrationNumber}
                        </div>
                        <div style={{ fontSize: '12px', color: '#999' }}>
                          {complaint.studentEmail}
                        </div>
                      </td>
                      <td style={{ padding: '16px' }}>
                        <span style={{
                          display: 'inline-block',
                          padding: '4px 12px',
                          background: '#e3f2fd',
                          color: '#1976d2',
                          borderRadius: '12px',
                          fontSize: '12px',
                          fontWeight: 600
                        }}>
                          {getIssueTypeDisplay(complaint.issueType)}
                        </span>
                      </td>
                      <td style={{ padding: '16px', maxWidth: '300px' }}>
                        <div style={{
                          fontSize: '13px',
                          color: '#555',
                          lineHeight: '1.6',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          display: '-webkit-box',
                          WebkitLineClamp: 3,
                          WebkitBoxOrient: 'vertical'
                        }}>
                          {complaint.description}
                        </div>
                      </td>
                      <td style={{ padding: '16px', fontSize: '13px', color: '#666', whiteSpace: 'nowrap' }}>
                        {formatDate(complaint.createdAt)}
                      </td>
                      <td style={{ padding: '16px', textAlign: 'center' }}>
                        <span style={{
                          display: 'inline-block',
                          padding: '6px 16px',
                          background: complaint.status === 'PENDING' ? '#fff3e0' : '#e8f5e9',
                          color: complaint.status === 'PENDING' ? '#f57c00' : '#2e7d32',
                          borderRadius: '16px',
                          fontSize: '12px',
                          fontWeight: 600
                        }}>
                          {complaint.status}
                        </span>
                      </td>
                      <td style={{ padding: '16px', textAlign: 'center' }}>
                        <div style={{ display: 'flex', gap: '8px', justifyContent: 'center' }}>
                          {complaint.status === 'PENDING' && (
                            <button
                              onClick={() => handleResolve(complaint.id)}
                              style={{
                                padding: '6px 12px',
                                background: '#4caf50',
                                color: 'white',
                                border: 'none',
                                borderRadius: '6px',
                                fontSize: '12px',
                                fontWeight: 600,
                                cursor: 'pointer'
                              }}
                              title="Mark as Resolved"
                            >
                              ✓ Resolve
                            </button>
                          )}
                          <button
                            onClick={() => handleDelete(complaint.id)}
                            style={{
                              padding: '6px 12px',
                              background: '#f44336',
                              color: 'white',
                              border: 'none',
                              borderRadius: '6px',
                              fontSize: '12px',
                              fontWeight: 600,
                              cursor: 'pointer'
                            }}
                            title="Delete Complaint"
                          >
                            🗑️ Delete
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default Complaints;
