import React, { useContext } from 'react';
import { NotificationContext } from '../context/NotificationContext';
import './Toast.css';

const Toast = () => {
  const { notifications, removeNotification } = useContext(NotificationContext);

  return (
    <div className="toast-container">
      {notifications.map((notification) => (
        <div key={notification.id} className={`toast toast-${notification.type}`}>
          <div className="toast-content">
            <span className="toast-message">{notification.message}</span>
            <button
              className="toast-close-btn"
              onClick={() => removeNotification(notification.id)}
              title="Close"
            >
              ✕
            </button>
          </div>
        </div>
      ))}
    </div>
  );
};

export default Toast;
