/**
 * mindmapService.js
 * API service for mind map operations
 * YouTube to Mind Map Integration (3-Pipeline System)
 */

import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const mindmapService = {
  /**
   * Generate mind map from YouTube video
   * @param {string} youtubeUrl - YouTube video URL
   * @param {string} subject - Subject/topic of the video
   * @param {string} token - JWT authentication token
   * @param {string} studentId - Student ID
   * @returns {Promise<Object>} - Mind map data
   */
  generateMindMapFromYoutube: async (youtubeUrl, subject, token, studentId) => {
    try {
      console.log('🚀 [mindmapService] Generating mind map...', {
        youtubeUrl: youtubeUrl.substring(0, 50) + '...',
        subject,
        studentId,
      });

      const response = await axios.post(
        `${API_BASE_URL}/mindmap/generate-from-youtube`,
        {
          youtubeUrl: youtubeUrl.trim(),
          subject: subject.trim(),
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
            'X-Student-Id': studentId,
          },
          timeout: 300000, // 5 minutes timeout for processing
        }
      );

      console.log('✅ [mindmapService] Mind map generated successfully');

      // Parse response
      const mindMap = {
        mindMapJson: response.data.mindMapJson || response.data.data?.mindMapJson,
        mermaidCode: response.data.mermaidCode,
        nodes: response.data.nodes || [],
        subject: subject.trim(),
        youtubeUrl: youtubeUrl.trim(),
        createdAt: new Date().toISOString(),
        status: response.data.status || 'COMPLETED',
        message: response.data.message,
      };

      return {
        success: true,
        data: mindMap,
      };
    } catch (error) {
      console.error('❌ [mindmapService] Error generating mind map:', error);

      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        'Failed to generate mind map';

      return {
        success: false,
        error: errorMessage,
        status: error.response?.status,
      };
    }
  },

  /**
   * Validate YouTube URL format
   * @param {string} url - URL to validate
   * @returns {boolean} - True if valid YouTube URL
   */
  validateYoutubeUrl: (url) => {
    const regex = /^(https?:\/\/)?(www\.)?(youtube|youtu|youtube-nocookie)\.(com|be)\/.+$/;
    return regex.test(url);
  },

  /**
   * Extract video ID from YouTube URL
   * @param {string} url - YouTube URL
   * @returns {string|null} - Video ID or null
   */
  extractVideoId: (url) => {
    try {
      // Handle different YouTube URL formats
      const patterns = [
        /(?:https?:\/\/)?(?:www\.)?youtube\.com\/watch\?v=([^&]+)/,
        /(?:https?:\/\/)?(?:www\.)?youtu\.be\/([^?]+)/,
        /(?:https?:\/\/)?(?:www\.)?youtube\.com\/embed\/([^?]+)/,
      ];

      for (const pattern of patterns) {
        const match = url.match(pattern);
        if (match && match[1]) {
          return match[1];
        }
      }

      return null;
    } catch (error) {
      console.error('Error extracting video ID:', error);
      return null;
    }
  },

  /**
   * Format error message for display
   * @param {string} error - Error message
   * @returns {string} - Formatted error
   */
  formatError: (error) => {
    const messages = {
      'Network Error': 'Network error. Please check your connection.',
      'Timeout': 'Request timeout. Please try again.',
      'Invalid URL': 'Invalid YouTube URL. Please check and try again.',
      'No transcript': 'Could not extract transcript from this video.',
      'Generation failed': 'Failed to generate mind map. Please try again.',
    };

    for (const [key, value] of Object.entries(messages)) {
      if (error.includes(key)) {
        return value;
      }
    }

    return error;
  },

  /**
   * Parse mind map JSON with error handling
   * @param {string|Object} data - Mind map data (string or object)
   * @returns {Object|null} - Parsed mind map or null
   */
  parseMindMap: (data) => {
    try {
      if (typeof data === 'string') {
        return JSON.parse(data);
      }
      if (typeof data === 'object' && data !== null) {
        return data;
      }
      return null;
    } catch (error) {
      console.error('Error parsing mind map:', error);
      return null;
    }
  },

  /**
   * Calculate statistics from mind map
   * @param {Object} mindMap - Mind map object
   * @returns {Object} - Statistics
   */
  calculateStats: (mindMap) => {
    let nodes = 0;
    let keyPoints = 0;
    let definitions = 0;

    const traverse = (node) => {
      if (!node) return;

      nodes++;

      if (node.keyPoints && Array.isArray(node.keyPoints)) {
        keyPoints += node.keyPoints.length;
      }

      if (node.definition && String(node.definition).trim()) {
        definitions++;
      }

      if (node.children && Array.isArray(node.children)) {
        node.children.forEach(traverse);
      }
    };

    if (mindMap) {
      traverse(mindMap);
    }

    return {
      totalNodes: nodes,
      totalKeyPoints: keyPoints,
      totalDefinitions: definitions,
    };
  },
};

export default mindmapService;