import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser, isAuthenticated } from '../../utils/storage';
import Navbar from '../Common/Navbar';

function AdminHome() {
    const navigate = useNavigate();
    const currentUser = getCurrentUser();

    useEffect(() => {
        if (!isAuthenticated()) {
            navigate('/');
        }
    }, [navigate]);

    if (!currentUser) return null;

    return (
        <div className="home-page">
            <Navbar title="Admin Dashboard" />
            
            <div className="welcome-card">
                <h2>Admin Control Panel</h2>
                <div className="user-details">
                    <p><strong>Logged in as:</strong> {currentUser.email}</p>
                    <p><strong>Role:</strong> Administrator</p>
                    <p><strong>Status:</strong> ✅ Active</p>
                </div>
            </div>

            <div className="blank-content">
                <p>⚙️ Admin features will be added soon</p>
                <p style={{ fontSize: '0.9em', marginTop: '15px', color: '#999' }}>
                    Features coming: RFID Reader Management, Staff Location Tracking, Attendance Reports
                </p>
            </div>
        </div>
    );
}

export default AdminHome;
