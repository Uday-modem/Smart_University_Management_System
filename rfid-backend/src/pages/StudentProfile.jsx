import React, { useContext, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import apiClient from '../config/apiClient';
import { getBranchName } from '../utils/branchMapping';


const StudentProfile = () => {
  const { user, token } = useContext(AuthContext); 
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  useEffect(() => {
    if (token) {
      apiClient.getStudentProfile(token)
        .then(data => {
          console.log('Profile Data Received:', data);
          if (data && data.id) { 
            setProfile(data);
            setErr('');
          } else {
            setErr(data.message || 'Invalid profile data from server');
          }
          setLoading(false);
        })
        .catch(error => {
          console.error('Error fetching profile:', error);
          setErr('Failed to fetch profile. ' + error.message);
          setLoading(false);
        });
    } else {
      setErr("You are not logged in. Please log in to view your profile.");
      setLoading(false);
    }
  }, [token]);

  // Helper function to get regulation name
  const getRegulationName = (regulationId) => {
    const regulations = {
      1: 'R20',
      2: 'R23',
      3: 'R25'
    };
    return regulations[regulationId] || 'N/A';
  };

  // Helper function to get entry type display
  const getEntryTypeDisplay = (entryType) => {
    if (entryType === 'LATERAL') {
      return 'Lateral Entry (2nd Year)';
    }
    return 'Regular Entry (1st Year)';
  };

  if (loading) return <div style={{ padding: 32 }}>Loading profile...</div>;
  if (err) return (
    <div style={{ padding: 32, color: 'red' }}>
      {err}
      <br />
      <button onClick={() => navigate('/student/dashboard')}>Back to Dashboard</button>
    </div>
  );
  if (!profile) return <div style={{ padding: 32 }}>No profile found. Please try logging in again.</div>;

  return (
    <div style={{ minHeight: '100vh', background: '#f5f7fa', fontFamily: 'Segoe UI, sans-serif' }}>
      {/* Top bar */}
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '16px 30px',
        background: '#fff',
        boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
        marginBottom: '30px'
      }}>
        <h1 style={{ margin: 0, fontSize: '20px', fontWeight: 600, color: '#1e88e5' }}>
          My Profile
        </h1>
        <button onClick={() => navigate('/student/dashboard')} style={{
          padding: '8px 16px',
          background: '#1e88e5',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          cursor: 'pointer'
        }}>
          ← Back to Dashboard
        </button>
      </div>

      {/* Profile Card */}
      <div style={{
        maxWidth: '600px',
        margin: '0 auto',
        background: '#fff',
        padding: '30px',
        borderRadius: '8px',
        boxShadow: '0 2px 12px rgba(0,0,0,0.08)'
      }}>
        {/* Avatar */}
        <div style={{
          width: '80px',
          height: '80px',
          borderRadius: '50%',
          background: '#1e88e5',
          color: '#fff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: '36px',
          fontWeight: 'bold',
          margin: '0 auto 20px'
        }}>
          {profile.name ? profile.name.charAt(0).toUpperCase() : 'S'}
        </div>

        {/* Profile Details */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
          <div style={{ paddingBottom: '15px', borderBottom: '1px solid #eee' }}>
            <label style={{ fontWeight: 600, color: '#666', fontSize: '13px', display: 'block' }}>Name</label>
            <span style={{ fontSize: '16px', color: '#333' }}>{profile.name || 'N/A'}</span>
          </div>

          <div style={{ paddingBottom: '15px', borderBottom: '1px solid #eee' }}>
            <label style={{ fontWeight: 600, color: '#666', fontSize: '13px', display: 'block' }}>Registration Number</label>
            <span style={{ fontSize: '16px', color: '#333' }}>{profile.registrationNumber || 'N/A'}</span>
          </div>

          <div style={{ paddingBottom: '15px', borderBottom: '1px solid #eee' }}>
            <label style={{ fontWeight: 600, color: '#666', fontSize: '13px', display: 'block' }}>Email</label>
            <span style={{ fontSize: '16px', color: '#333' }}>{profile.email || 'N/A'}</span>
          </div>

          <div style={{ paddingBottom: '15px', borderBottom: '1px solid #eee' }}>
            <label style={{ fontWeight: 600, color: '#666', fontSize: '13px', display: 'block' }}>Branch</label>
            <span style={{ fontSize: '16px', color: '#333' }}>
              {getBranchName(profile.branch) || 'N/A'}
            </span>
          </div>


          <div style={{ paddingBottom: '15px', borderBottom: '1px solid #eee' }}>
            <label style={{ fontWeight: 600, color: '#666', fontSize: '13px', display: 'block' }}>Regulation</label>
            <span style={{ fontSize: '16px', color: '#333' }}>{getRegulationName(profile.regulationId)}</span>
          </div>

          <div style={{ paddingBottom: '15px', borderBottom: '1px solid #eee' }}>
            <label style={{ fontWeight: 600, color: '#666', fontSize: '13px', display: 'block' }}>Entry Type</label>
            <span style={{ fontSize: '16px', color: '#333' }}>{getEntryTypeDisplay(profile.entryType)}</span>
          </div>

          <div style={{ paddingBottom: '15px', borderBottom: '1px solid #eee' }}>
            <label style={{ fontWeight: 600, color: '#666', fontSize: '13px', display: 'block' }}>Current Year</label>
            <span style={{ fontSize: '16px', color: '#333' }}>Year {profile.year || 'N/A'}</span>
          </div>

          <div>
            <label style={{ fontWeight: 600, color: '#666', fontSize: '13px', display: 'block' }}>Current Semester</label>
            <span style={{ fontSize: '16px', color: '#333' }}>Semester {profile.semester || 'N/A'}</span>
          </div>

          {/* Student's Section */}
<div style={{ paddingBottom: '15px', borderBottom: '1px solid #eee' }}>
  <label style={{ fontWeight: 600, color: '#666', fontSize: '13px', display: 'block' }}>Section</label>
  <span style={{ fontSize: '16px', color: '#333' }}>
    {profile.section ? `${profile.section} (${profile.sectionId})` : 'N/A'}
  </span>
</div>

        </div>
      </div>
    </div>
  );
};

export default StudentProfile;
