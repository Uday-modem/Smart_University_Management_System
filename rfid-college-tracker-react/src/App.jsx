import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ThemeProvider } from "./context/ThemeContext";
import LoginPage from './pages/LoginPage';
import SignUpPage from './pages/SignupPage';
import StudentDashboard from './pages/StudentDashboard';
import AdminDashboard from './pages/AdminDashboard';
import StudentProfile from './pages/StudentProfile';
import StudentTimetable from './pages/StudentTimetable';
import AdminTimetable from './pages/AdminTimetable';
import StaffManagement from './pages/StaffManagement';
import AttendanceManagement from './pages/AttendanceManagement';
import StudentAttendanceView from './pages/StudentAttendanceView';
import AdminMarksManagement from './pages/AdminMarksManagement';
import StudentMarksView from './pages/StudentMarksView';
import SemesterDateManagement from './pages/SemesterDateManagement';
import Complaints from './pages/Complaints'; // ✅ NEW
import ProtectedRoute from './components/ProtectedRoute';
import './App.css';

// ✅ Only one App component, wrapping everything needed
function App() {
  return (
    <AuthProvider>
      <ThemeProvider>
        <Router>
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<LoginPage />} />
            <Route path="/signup" element={<SignUpPage />} />

            {/* Student Routes */}
            <Route 
              path="/student/dashboard" 
              element={
                <ProtectedRoute>
                  <StudentDashboard />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/student/profile" 
              element={
                <ProtectedRoute>
                  <StudentProfile />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/student/timetable" 
              element={
                <ProtectedRoute>
                  <StudentTimetable />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/student/attendance" 
              element={
                <ProtectedRoute>
                  <StudentAttendanceView />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/student/marks" 
              element={
                <ProtectedRoute>
                  <StudentMarksView />
                </ProtectedRoute>
              } 
            />

            {/* Admin Routes */}
            <Route 
              path="/admin/dashboard" 
              element={
                <ProtectedRoute>
                  <AdminDashboard />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/admin/timetable" 
              element={
                <ProtectedRoute>
                  <AdminTimetable />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/admin/staff" 
              element={
                <ProtectedRoute>
                  <StaffManagement />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/admin/attendance" 
              element={
                <ProtectedRoute>
                  <AttendanceManagement />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/admin/marks" 
              element={
                <ProtectedRoute>
                  <AdminMarksManagement />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/admin/semester-config" 
              element={
                <ProtectedRoute>
                  <SemesterDateManagement />
                </ProtectedRoute>
              } 
            />
            {/* ✅ NEW: Admin Complaints Route */}
            <Route 
              path="/admin/complaints" 
              element={
                <ProtectedRoute>
                  <Complaints />
                </ProtectedRoute>
              } 
            />
            
            {/* Catch-all Route */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Router>
      </ThemeProvider>
    </AuthProvider>
  );
}

export default App;
