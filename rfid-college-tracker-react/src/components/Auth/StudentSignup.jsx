import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { initialStudents } from '../../data/initialData';
import { validateStudentSignup } from '../../utils/auth';

function StudentSignup() {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        name: '',
        registrationNumber: '',
        email: '',
        password: '',
        confirmPassword: ''
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

    const handleTogglePassword = (fieldId) => {
        const field = document.getElementById(fieldId);
        field.type = field.type === 'password' ? 'text' : 'password';
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        const validationErrors = validateStudentSignup(formData);
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        if (initialStudents.find(s => s.email === formData.email)) {
            setErrors({ email: 'Email already registered' });
            return;
        }

        if (initialStudents.find(s => s.registrationNumber === formData.registrationNumber)) {
            setErrors({ registrationNumber: 'Registration number already exists' });
            return;
        }

        const newStudent = {
            id: initialStudents.length + 1,
            name: formData.name,
            registrationNumber: formData.registrationNumber,
            email: formData.email,
            password: formData.password
        };
        initialStudents.push(newStudent);

        setMessage('Account created successfully! Redirecting to login...');
        setTimeout(() => navigate('/student-login'), 2000);
    };

    return (
        <div className="auth-page">
            <div className="auth-form">
                <button 
                    className="back-btn" 
                    onClick={() => navigate('/student-login')}
                >
                    ← Back
                </button>
                <h2>Student Sign Up</h2>

                {message && <div className="message success">{message}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="name">Student Name</label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            placeholder="Enter your full name"
                            value={formData.name}
                            onChange={handleChange}
                            className={errors.name ? 'error' : ''}
                        />
                        {errors.name && <span className="error-message">{errors.name}</span>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="registrationNumber">Registration Number</label>
                        <input
                            type="text"
                            id="registrationNumber"
                            name="registrationNumber"
                            placeholder="Enter your registration number"
                            value={formData.registrationNumber}
                            onChange={handleChange}
                            className={errors.registrationNumber ? 'error' : ''}
                        />
                        {errors.registrationNumber && 
                            <span className="error-message">{errors.registrationNumber}</span>
                        }
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">College Mail ID</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            placeholder="Enter your college email"
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
                                placeholder="Minimum 6 characters"
                                value={formData.password}
                                onChange={handleChange}
                                className={errors.password ? 'error' : ''}
                            />
                            <span 
                                className="toggle-password"
                                onClick={() => handleTogglePassword('password')}
                            >
                                👁️
                            </span>
                        </div>
                        {errors.password && <span className="error-message">{errors.password}</span>}
                    </div>

                    <div className="form-group">
                        <label htmlFor="confirmPassword">Confirm Password</label>
                        <div className="password-container">
                            <input
                                type="password"
                                id="confirmPassword"
                                name="confirmPassword"
                                placeholder="Re-enter your password"
                                value={formData.confirmPassword}
                                onChange={handleChange}
                                className={errors.confirmPassword ? 'error' : ''}
                            />
                            <span 
                                className="toggle-password"
                                onClick={() => handleTogglePassword('confirmPassword')}
                            >
                                <i className="fa fa-eye"></i> 
                            </span>
                        </div>
                        {errors.confirmPassword && 
                            <span className="error-message">{errors.confirmPassword}</span>
                        }
                    </div>

                    <button type="submit" className="submit-btn">Sign Up</button>
                </form>

                <div className="form-link">
                    Already have an account? 
                    <a onClick={() => navigate('/student-login')}> Login</a>
                </div>
            </div>
        </div>
    );
}

export default StudentSignup;
