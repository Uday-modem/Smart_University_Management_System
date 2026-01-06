import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { initialAdmins } from '../../data/initialData';
import { validateAdminLogin } from '../../utils/auth';
import { setCurrentUser } from '../../utils/storage';

function AdminLogin() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        email: '',
        password: ''
    });
    const [errors, setErrors] = useState({});
    const [message, setMessage] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        if (errors[name]) {
            setErrors(prev => ({
                ...prev,
                [name]: ''
            }));
        }
    };

    const handleTogglePassword = () => {
        const field = document.getElementById('password');
        field.type = field.type === 'password' ? 'text' : 'password';
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        const validationErrors = validateAdminLogin(formData.email, formData.password);
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        const admin = initialAdmins.find(a =>
            a.email === formData.email && a.password === formData.password
        );

        if (admin) {
            setCurrentUser({ email: admin.email }, 'admin');
            setMessage('Admin login successful! Redirecting...');
            setTimeout(() => navigate('/admin-home'), 1500);
        } else {
            setMessage('Invalid admin credentials');
            setErrors({ submit: true });
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-form">
                <button 
                    className="back-btn" 
                    onClick={() => navigate('/')}
                >
                    ← Back
                </button>
                <h2>Admin Login</h2>

                {message && (
                    <div className={`message ${errors.submit ? 'error' : 'success'}`}>
                        {message}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="email">Admin Email</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            placeholder="Enter admin email"
                            value={formData.email}
                            onChange={handleChange}
                            className={errors.email ? 'error' : ''}
                        />
                        {errors.email && <span className="error-message">{errors.email}</span>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <div className="password-container">
                            <input
                                type="password"
                                id="password"
                                name="password"
                                placeholder="Enter password"
                                value={formData.password}
                                onChange={handleChange}
                                className={errors.password ? 'error' : ''}
                            />
                            <span 
                                className="toggle-password"
                                onClick={handleTogglePassword}
                            >
                                👁️
                            </span>
                        </div>
                        {errors.password && <span className="error-message">{errors.password}</span>}
                    </div>

                    <button type="submit" className="submit-btn">Login</button>
                </form>
            </div>
        </div>
    );
}

export default AdminLogin;
