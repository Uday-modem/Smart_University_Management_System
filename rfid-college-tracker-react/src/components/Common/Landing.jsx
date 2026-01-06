import React from 'react';
import { useNavigate } from 'react-router-dom';

function Landing() {
    const navigate = useNavigate();

    return (
        <div className="landing-page">
            <div className="landing-content">
                <h1>🎓 College RFID Staff Tracking System</h1>
                <p>Track staff location in real-time across campus using RFID technology</p>
                <div className="button-group">
                    <button 
                        className="btn btn-primary" 
                        onClick={() => navigate('/student-login')}
                    >
                        Student Login
                    </button>
                    <button 
                        className="btn btn-primary" 
                        onClick={() => navigate('/admin-login')}
                    >
                        Admin Login
                    </button>
                </div>
            </div>
        </div>
    );
}

export default Landing;
