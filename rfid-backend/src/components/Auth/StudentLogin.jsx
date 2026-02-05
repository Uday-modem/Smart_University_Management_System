import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { initialStudents } from '../../data/initialData';
import { validateStudentLogin } from '../../utils/auth';
import { setCurrentUser } from '../../utils/storage';

function StudentLogin() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        emailOrReg: '',
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
        
        const validationErrors = validateStudentLogin(
            formData.emailOrReg, 
            formData.password
        );
        
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        const student = initialStudents.find(s =>
            (s.email === formData.emailOrReg || 
             s.registrationNumber === formData.emailOrReg) &&
            s.password === formData.password
        );

        if (student) {
            setCurrentUser(student, 'student');
            setMessage('Login successful! Redirecting...');
            setTimeout(() => navigate('/student-home'), 1500);
        } else {
            setMessage('Invalid email/registration number or password');
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
                <h2>Student Login</h2>
                
                {message && (
                    <div className={`message ${errors.submit ? 'error' : 'success'}`}>
                        {message}
                    </div>
                )}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="emailOrReg">Email ID or Registration Number</label>
                        <input
                            type="text"
                            id="emailOrReg"
                            name="emailOrReg"
                            placeholder="Enter your email or registration number"
                            value={formData.emailOrReg}
                            onChange={handleChange}
                            className={errors.emailOrReg ? 'error' : ''}
                        />
                        {errors.emailOrReg && (
                            <span className="error-message">{errors.emailOrReg}</span>
                        )}
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <div className="password-container">
                            <input
                                type="password"
                                id="password"
                                name="password"
                                placeholder="Enter your password"
                                value={formData.password}
                                onChange={handleChange}
                                className={errors.password ? 'error' : ''}
                            />
                            <span 
                                className="toggle-password"
                                onClick={handleTogglePassword}
                            >
                                <i className="fa fa-eye"></i> 
                            </span>
                        </div>
                        {errors.password && (
                            <span className="error-message">{errors.password}</span>
                        )}
                    </div>

                    <button type="submit" className="submit-btn">Login</button>
                </form>

                <div className="form-link">
                    Don't have an account? 
                    <a onClick={() => navigate('/student-signup')}> Sign Up</a>
                </div>
            </div>
        </div>
    );
}

export default StudentLogin;
