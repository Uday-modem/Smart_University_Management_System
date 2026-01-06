import React, { useContext } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const ProtectedRoute = ({ children }) => {
  const { user, loading } = useContext(AuthContext);
  const location = useLocation();

  // **THE FIX IS HERE:** If the context is still loading, don't do anything yet.
  if (loading) {
    return <div>Loading session...</div>; // Or a spinner component
  }

  // If loading is finished and there's still no user, redirect to login.
  if (!user) {
    return <Navigate to="/" state={{ from: location }} replace />;
  }

  // If loading is finished and there is a user, show the requested page.
  return children;
};

export default ProtectedRoute;
