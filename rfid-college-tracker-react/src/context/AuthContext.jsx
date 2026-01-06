import React, { createContext, useState, useEffect } from 'react';

export const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);
  const [token, setToken] = useState(null); // <-- FIX: Add state for the token
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    try {
      const storedAuth = localStorage.getItem('auth');
      if (storedAuth) {
        // FIX: Also load the token from storage
        const { user: storedUser, role: storedRole, token: storedToken } = JSON.parse(storedAuth);
        setUser(storedUser);
        setRole(storedRole);
        setToken(storedToken); // <-- FIX: Set the token state
      }
    } catch (e) {
      localStorage.removeItem('auth');
    } finally {
      setLoading(false);
    }
  }, []);

  // FIX: Update the login function to accept and store the token
  const login = (userData, userRole, userToken) => {
    setUser(userData);
    setRole(userRole);
    setToken(userToken); // <-- FIX: Save the token
    localStorage.setItem('auth', JSON.stringify({ user: userData, role: userRole, token: userToken }));
  };

  // FIX: Update the logout function to also clear the token
  const logout = () => {
    setUser(null);
    setRole(null);
    setToken(null); // <-- FIX: Clear the token
    localStorage.removeItem('auth');
  };

  // FIX: Provide the token through the context value
  return (
    <AuthContext.Provider value={{ user, role, token, login, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
