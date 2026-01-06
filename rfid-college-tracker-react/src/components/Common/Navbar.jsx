import React from 'react';
import { useNavigate } from 'react-router-dom';
import { clearSession } from '../../utils/storage';

function Navbar({ title }) {
    const navigate = useNavigate();

    const handleLogout = () => {
        clearSession();
        navigate('/');
    };

    return (
        <div className="navbar">
            <h1>{title}</h1>
            <button className="logout-btn" onClick={handleLogout}>
                Logout
            </button>
        </div>
    );
}

export default Navbar;
