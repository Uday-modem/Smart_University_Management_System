import React, { createContext, useState, useCallback } from 'react';

export const NotificationContext = createContext();

export const NotificationProvider = ({ children }) => {
  const [notifications, setNotifications] = useState([]);

  const addNotification = useCallback((message, type = 'info', autoCloseTime = null) => {
    const id = Date.now();
    const notification = {
      id,
      message,
      type, // 'info', 'success', 'warning', 'error'
      autoCloseTime,
    };

    setNotifications((prev) => [...prev, notification]);

    // Auto-close after specified time or 4:00 PM
    if (autoCloseTime) {
      const timer = setTimeout(() => {
        removeNotification(id);
      }, autoCloseTime);
      
      return { id, timer };
    }

    // Auto-close at 4:00 PM (16:00)
    const now = new Date();
    const fourPM = new Date();
    fourPM.setHours(16, 0, 0, 0);

    if (now < fourPM) {
      const timeUntilFourPM = fourPM - now;
      const timer = setTimeout(() => {
        removeNotification(id);
      }, timeUntilFourPM);
      
      return { id, timer };
    }

    return { id };
  }, []);

  const removeNotification = useCallback((id) => {
    setNotifications((prev) => prev.filter((notif) => notif.id !== id));
  }, []);

  return (
    <NotificationContext.Provider value={{ notifications, addNotification, removeNotification }}>
      {children}
    </NotificationContext.Provider>
  );
};
