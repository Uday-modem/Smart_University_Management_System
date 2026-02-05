import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser, isAuthenticated } from '../../utils/storage';
import Navbar from '../Common/Navbar';

function StudentHome() {
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
            <Navbar title="Student Dashboard" />
            
            <div className="welcome-card">
                <h2>Welcome, {currentUser.name}! 👋</h2>
                
            </div>

            <div className="blank-content">
                <p>📊 Dashboard content will be added soon</p>
                <p style={{ fontSize: '0.9em', marginTop: '15px', color: '#999' }}>
                    Features coming: Location history, Attendance records, Schedule
                </p>
            </div>
        </div>
    );
}

export default StudentHome;
