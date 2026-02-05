/**
 * apiClient.js
 * Centralized API client for all backend communication
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const apiClient = {
  // ✅ AUTHENTICATION ENDPOINTS

  /**
   * Student Login
   */
  studentLogin: async (email, password) => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/student/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });
      return await response.json();
    } catch (error) {
      console.error('Login error:', error);
      return { success: false, message: error.message };
    }
  },

  /**
   * Admin Login
   */
  adminLogin: async (email, password) => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/admin/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      });
      return await response.json();
    } catch (error) {
      console.error('Admin login error:', error);
      return { success: false, message: error.message };
    }
  },

  /**
   * Student Signup
   */
  studentSignup: async (signupData) => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/student/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(signupData),
      });
      return await response.json();
    } catch (error) {
      console.error('Signup error:', error);
      return { success: false, message: error.message };
    }
  },

  // ✅ STUDENT PROFILE ENDPOINTS

  /**
   * Get Student Profile
   */
  getStudentProfile: async (token) => {
    try {
      const response = await fetch(`${API_BASE_URL}/students/profile`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('Profile fetch error:', error);
      return { success: false, message: error.message };
    }
  },

  /**
   * Get Admin Profile
   */
  getAdminProfile: async (token) => {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/admin-profile`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('Admin profile fetch error:', error);
      return { success: false, message: error.message };
    }
  },

  // ✅ TIMETABLE ENDPOINTS

  /**
   * Get Student Timetable
   */
  getStudentTimetable: async (token) => {
    try {
      const response = await fetch(`${API_BASE_URL}/timetable/student`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return await response.json();
    } catch (error) {
      console.error('Timetable fetch error:', error);
      return { success: false, message: error.message };
    }
  },

  // ✅ NEW: MIND MAP ENDPOINTS (3-PIPELINE YOUTUBE INTEGRATION)

  /**
   * Generate Mind Map from YouTube Video
   * 
   * 3-Pipeline System:
   * - Pipeline 1: Ingestion (Extract YouTube transcript)
   * - Pipeline 2: Synthesis (Generate mind map + explanations with Ollama)
   * - Pipeline 3: Rendering (Package and return response)
   */
  generateMindMapFromYoutube: async (youtubeUrl, subject, token, studentId) => {
    try {
      console.log('🚀 [apiClient] Generating mind map from YouTube...');

      const response = await fetch(
        `${API_BASE_URL}/mindmap/generate-from-youtube`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
            'X-Student-Id': studentId,
          },
          body: JSON.stringify({
            youtubeUrl: youtubeUrl.trim(),
            subject: subject.trim(),
          }),
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('✅ [apiClient] Mind map generated successfully');

      return {
        success: true,
        ...data,
      };
    } catch (error) {
      console.error('❌ [apiClient] Mind map generation error:', error);
      return {
        success: false,
        message: error.message || 'Failed to generate mind map',
      };
    }
  },

  /**
   * Get Mind Map Status (for polling)
   */
  getMindMapStatus: async (mindMapId, token, studentId) => {
    try {
      const response = await fetch(
        `${API_BASE_URL}/mindmap/${mindMapId}?studentId=${studentId}`,
        {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
            'X-Student-Id': studentId,
          },
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching mind map status:', error);
      return { success: false, message: error.message };
    }
  },

  /**
   * Get All User Mind Maps
   */
  getUserMindMaps: async (token, studentId) => {
    try {
      const response = await fetch(
        `${API_BASE_URL}/mindmap/user-mindmaps?studentId=${studentId}`,
        {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`,
            'X-Student-Id': studentId,
          },
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching user mind maps:', error);
      return { success: false, message: error.message };
    }
  },

  /**
   * Delete Mind Map
   */
  deleteMindMap: async (mindMapId, token, studentId) => {
    try {
      const response = await fetch(
        `${API_BASE_URL}/mindmap/${mindMapId}?studentId=${studentId}`,
        {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${token}`,
            'X-Student-Id': studentId,
          },
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Error deleting mind map:', error);
      return { success: false, message: error.message };
    }
  },

  /**
   * Health Check for Mind Map Service
   */
  mindMapHealthCheck: async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/mindmap/health`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' },
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('Mind map health check failed:', error);
      return { success: false, message: error.message };
    }
  },
};

export default apiClient;