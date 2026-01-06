import React, { useContext, useState } from 'react';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import './LoginPage.css';

const API_BASE_URL = 'http://localhost:8080/api';

const LoginPage = () => {
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const [emailOrRegNo, setEmailOrRegNo] = useState('');
  const [password, setPassword] = useState('');
  const [isAdminLogin, setIsAdminLogin] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const loginUrl = isAdminLogin
      ? `${API_BASE_URL}/auth/admin/login`
      : `${API_BASE_URL}/auth/student/login`;

    const loginData = { email: emailOrRegNo, password };

    try {
      const response = await axios.post(loginUrl, loginData);
      const data = response.data; // Access data properly

      if (data.success) {
        const role = isAdminLogin ? 'ADMIN' : 'STUDENT';
        
        // ✅ CRITICAL FIX: Backend returns 'student' or 'admin' object
        const userData = isAdminLogin ? data.admin : data.student;
        const token = data.token;

        console.log("✅ Login Successful:", userData);

        // Store token and user data via Context
        login(userData, role, token);
        
        // Navigate to correct dashboard
        navigate(isAdminLogin ? '/admin/dashboard' : '/student/dashboard');
      } else {
        setError(data.message || 'Invalid credentials');
      }
    } catch (err) {
      console.error("Login Error:", err);
      // Safely access error message if available
      const errorMsg = err.response?.data?.message || 'Login failed. Please check credentials or server status.';
      setError(errorMsg);
    }
  };

  // --- UI REMAINS EXACTLY AS YOU DESIGNED ---
  return (
    <div className="login-container">
      <div className="login-card">
        <h2 className="login-title">Smart University Management System</h2>
        <form onSubmit={handleSubmit}>
          <div className="side-by-side-group">
            <label htmlFor="loginId">Email / Reg No.</label>
            <input
              type="text"
              id="loginId"
              value={emailOrRegNo}
              onChange={(e) => setEmailOrRegNo(e.target.value)}
              placeholder="Enter email or registration no."
              required
            />
          </div>
          <div className="side-by-side-group">
            <label htmlFor="password">Password</label>
            <div className="password-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                id="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter password"
                required
              />
              <span onClick={() => setShowPassword(!showPassword)} className="password-toggle-icon">
                {showPassword ? '👁️' : '👁️‍🗨️'}
              </span>
            </div>
          </div>
          <div className="user-type-group">
            <label className="user-type-label">User Type</label>
            <div className="login-toggle">
              <button 
                type="button" 
                className={!isAdminLogin ? 'active' : ''} 
                onClick={() => setIsAdminLogin(false)}
              >
                Student
              </button>
              <button 
                type="button" 
                className={isAdminLogin ? 'active' : ''} 
                onClick={() => setIsAdminLogin(true)}
              >
                Admin
              </button>
            </div>
          </div>
          {error && <p className="error-message">{error}</p>}
          <button type="submit" className="login-button">
            Login
          </button>
        </form>
        <div className="signup-section">
          <p>Don't have an account?</p>
          <button onClick={() => navigate('/signup')} className="signup-button">
            Sign Up
          </button>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
