import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../App.css';
import { getBranchesForDropdown } from '../utils/branchMapping';

const API_BASE_URL = 'http://localhost:8080/api';

const SignupPage = () => {
  const [registrationNumber, setRegistrationNumber] = useState('');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [branch, setBranch] = useState('');
  const [entryType, setEntryType] = useState('REGULAR');
  const [branches, setBranches] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  // Fetch branches (with mapping to full name)
  useEffect(() => {
    const fetchBranches = async () => {
      try {
        const response = await axios.get(`${API_BASE_URL}/sections/branches`);
        setBranches(getBranchesForDropdown(response.data || []));
      } catch (err) {
        console.error("Branch fetch error:", err);
        setError("Could not load branch data.");
        setBranches([]);
      }
    };
    fetchBranches();
  }, []);

  // Auto-generate email when registration number changes
  useEffect(() => {
    if (registrationNumber) {
      setEmail(`${registrationNumber.toLowerCase()}@audisankara.ac.in`);
    } else {
      setEmail('');
    }
  }, [registrationNumber]);

  const validateEmailDomain = (emailValue) => /^[a-zA-Z0-9]+@audisankara\.ac\.in$/.test(emailValue);

  const handleSignup = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    // --- FRONTEND VALIDATIONS ---
    if (registrationNumber.length < 2) {
      setError('Invalid registration number format');
      setLoading(false);
      return;
    }
    if (!validateEmailDomain(email)) {
      setError('Email must be in the format: registrationnumber@audisankara.ac.in');
      setLoading(false);
      return;
    }
    const expectedEmail = `${registrationNumber.toLowerCase()}@audisankara.ac.in`;
    if (email.toLowerCase() !== expectedEmail) {
      setError(`Email must be: ${expectedEmail}`);
      setLoading(false);
      return;
    }
    if (!branch) {
      setError('Please select a branch.');
      setLoading(false);
      return;
    }
    if (!password) {
      setError('Please enter a password.');
      setLoading(false);
      return;
    }

    // ✅ Calculate year based on entryType
    const year = entryType === 'LATERAL' ? 2 : 1;

    // --- SEND SIGNUP DATA (Backend will auto-assign section) ---
    const signupData = { 
      registrationNumber, 
      name, 
      email: email.toLowerCase(), 
      password, 
      branch, 
      entryType,
      year
    };

    console.log('📤 Sending signup data (Auto-Section Mode):', signupData);

    try {
      const response = await axios.post(`${API_BASE_URL}/auth/student/signup`, signupData);

      if (response.data.success) {
        const assignedSection = response.data.student?.section || "your assigned section";
        alert('✅ Signup successful! You have been enrolled in ' +
              (entryType === 'LATERAL' ? 'Year 2, Semester 3' : 'Year 1, Semester 1') +
              `\n\nAssigned Section: ${assignedSection}` +
              '. Please login with your email: ' + email);
        navigate('/');
      } else {
        setError(response.data.message || 'Signup failed');
      }
    } catch (err) {
      // Enhanced error extraction
      const backendMessage = err.response?.data?.message;
      setError(backendMessage || 'An error occurred during signup. Please try again.');
      console.error('Signup error:', err);
    }
    setLoading(false);
  };

  const togglePasswordVisibility = () => setShowPassword(!showPassword);

  const getYearSemesterDisplay = () => (entryType === 'LATERAL' ? 'Year 2, Semester 3' : 'Year 1, Semester 1');
  
  const getRegulationDisplay = () => {
    if (registrationNumber.length >= 2) {
      const yearDigits = registrationNumber.substring(0, 2);
      const admissionYear = 2000 + parseInt(yearDigits);
      if (admissionYear >= 2022 && admissionYear <= 2025) return 'R20';
      if (admissionYear >= 2024 && admissionYear <= 2028) return 'R23';
      if (admissionYear >= 2027 && admissionYear <= 2030) return 'R25';
      return 'R20 (default)';
    }
    return 'Enter registration number';
  };

  return (
    <div className="auth-page student-bg">
      <div className="auth-card">
        <h2 className="auth-title">Create Account</h2>

        {error && <div className="auth-error-box">{error}</div>}

        <form onSubmit={handleSignup}>
          {/* Registration Number */}
          <div className="auth-field-group">
            <label className="auth-label">Registration Number</label>
            <input
              className="auth-input"
              type="text"
              value={registrationNumber}
              onChange={e => setRegistrationNumber(e.target.value)}
              placeholder="e.g., 232H5A0411"
              required
            />
            <small style={{ color: '#666', fontSize: '12px', display: 'block', marginTop: '4px' }}>
              First 2 digits determine your regulation: <strong>{getRegulationDisplay()}</strong>
            </small>
          </div>

          {/* Full Name */}
          <div className="auth-field-group">
            <label className="auth-label">Full Name</label>
            <input
              className="auth-input"
              type="text"
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="Enter your full name"
              required
            />
          </div>

          {/* Email Address - Auto-generated, Read-only */}
          <div className="auth-field-group">
            <label className="auth-label">College Email Address</label>
            <input
              className="auth-input"
              type="email"
              value={email}
              readOnly
              style={{
                backgroundColor: '#f5f5f5',
                cursor: 'not-allowed',
                color: '#333'
              }}
              placeholder="Enter registration number first"
              required
            />
            <small style={{ color: '#1e88e5', fontSize: '12px', display: 'block', marginTop: '4px' }}>
              ✓ Auto-generated based on registration number
            </small>
          </div>

          {/* Password */}
          <div className="auth-field-group">
            <label className="auth-label">Password</label>
            <div style={{ position: 'relative' }}>
              <input
                className="auth-input"
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={e => setPassword(e.target.value)}
                placeholder="Enter password"
                required
              />
              <span
                onClick={togglePasswordVisibility}
                style={{
                  position: 'absolute', right: '10px', top: '50%',
                  transform: 'translateY(-50%)', cursor: 'pointer', fontSize: '20px'
                }}
              >
                {showPassword ? '👁️' : '👁️‍🗨️'}
              </span>
            </div>
          </div>

          {/* Branch */}
          <div className="auth-field-group">
            <label className="auth-label">Branch</label>
            <select
              className="auth-input"
              value={branch}
              onChange={e => setBranch(e.target.value)}
              required
            >
              <option value="">Select Branch</option>
              {branches.map(b => (
                <option key={b.code} value={b.code}>{b.name}</option>
              ))}
            </select>
          </div>

          {/* Entry Type Selection */}
          <div className="auth-field-group">
            <label className="auth-label">Entry Type</label>
            <div style={{ display: 'flex', gap: '20px', marginTop: '8px' }}>
              <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <input
                  type="radio"
                  name="entryType"
                  value="REGULAR"
                  checked={entryType === 'REGULAR'}
                  onChange={() => setEntryType('REGULAR')}
                  style={{ marginRight: '8px' }}
                />
                <span style={{ fontSize: '14px', color: '#333' }}>
                  Regular Entry (1st Year)
                </span>
              </label>
              <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <input
                  type="radio"
                  name="entryType"
                  value="LATERAL"
                  checked={entryType === 'LATERAL'}
                  onChange={() => setEntryType('LATERAL')}
                  style={{ marginRight: '8px' }}
                />
                <span style={{ fontSize: '14px', color: '#333' }}>
                  Lateral Entry (2nd Year)
                </span>
              </label>
            </div>
            <div style={{
              marginTop: '12px',
              padding: '10px',
              background: '#e3f2fd',
              borderRadius: '6px',
              fontSize: '13px',
              color: '#1565c0'
            }}>
              ℹ️ You will be enrolled in: <strong>{getYearSemesterDisplay()}</strong>
              <br />
              <small>Your section will be automatically assigned based on your registration number.</small>
            </div>
          </div>

          {/* Submit */}
          <button type="submit" className="auth-login-btn" disabled={loading}>
            {loading ? 'Creating Account...' : 'Sign Up'}
          </button>
        </form>

        <p className="auth-footer-text">
          Already have an account? <span className="auth-link" onClick={() => navigate('/')}>Login</span>
        </p>
      </div>
    </div>
  );
};

export default SignupPage;
