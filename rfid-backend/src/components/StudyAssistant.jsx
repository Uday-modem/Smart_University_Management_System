import React, { useState, useContext } from 'react';
import axios from 'axios';
import { AuthContext } from '../context/AuthContext';
import MindMapViewerModal from './MindMapViewerModal';

import './StudyAssistant.css';

const API_BASE_URL = 'http://localhost:8080/api';
const TRANSCRIPT_API = 'http://localhost:5000/api/transcript/extract';

/**
 * StudyAssistant Component - 2-STEP FLOW FIX
 * 
 * FIXES APPLIED:
 * 1. ✅ STEP 1: Call Python service to extract transcript
 * 2. ✅ STEP 2: Call Java service with subject to generate mind map
 * 3. ✅ Better error handling for each step
 * 4. ✅ Clear progress indicators
 * 5. ✅ Proper console logging
 */
const StudyAssistant = ({ isOpen, onClose, token }) => {
  const { user } = useContext(AuthContext);

  // Form state
  const [youtubeUrl, setYoutubeUrl] = useState('');
  const [subject, setSubject] = useState('');
  const [activeTab, setActiveTab] = useState('create');

  // UI state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [mindMapData, setMindMapData] = useState(null);
  const [showViewer, setShowViewer] = useState(false);
  const [progressMessage, setProgressMessage] = useState('');

  // History state
  const [mindMapHistory, setMindMapHistory] = useState(
    JSON.parse(localStorage.getItem('mindMapHistory') || '[]')
  );

  // Get student ID
  const getStudentId = () => {
    return user?.id || user?.studentId || user?.userId || '';
  };

  // Validate YouTube URL
  const validateYoutubeUrl = (url) => {
    const regex = /^(https?:\/\/)?(www\.)?(youtube|youtu|youtube-nocookie)\.(com|be)\/.+$/;
    return regex.test(url);
  };

  /**
   * ADVANCED PARSER: Handles malformed Mermaid syntax
   */
  const convertMermaidToMindMap = (mermaidCode, subject) => {
    console.log('🔄 Converting Mermaid code to mind map structure...');
    console.log('📄 Raw Mermaid Code:', mermaidCode);

    const lines = mermaidCode.split('\n').filter(line => line.trim());
    const nodePattern = /(\w+)\s*[(\[]+["']?([^)\]"]+)["']?[)\]]+/g;
    const nodes = {};
    const relationships = [];

    // STEP 1: Extract all nodes
    let match;
    while ((match = nodePattern.exec(mermaidCode)) !== null) {
      const nodeId = match[1];
      const nodeText = match[2].trim();

      if (nodeId && nodeText && !nodes[nodeId]) {
        nodes[nodeId] = {
          id: nodeId,
          title: nodeText,
          definition: nodeText,
          keyPoints: [`Key point about ${nodeText}`],
          children: [],
        };
        console.log(`✅ Node: ${nodeId} = "${nodeText}"`);
      }
    }

    // STEP 2: Extract relationships (including comma-separated ones)
    mermaidCode.split('\n').forEach(line => {
      const matches = line.matchAll(/(\w+)\s*-->\s*(\w+)/g);
      for (const match of matches) {
        const parent = match[1];
        const child = match[2];
        if (nodes[parent] && nodes[child]) {
          relationships.push({ parent, child });
          console.log(`🔗 ${parent} --> ${child}`);
        }
      }
    });

    console.log(`📊 Found ${Object.keys(nodes).length} nodes and ${relationships.length} relationships`);

    // STEP 3: Build tree hierarchy
    relationships.forEach(rel => {
      if (nodes[rel.parent] && nodes[rel.child]) {
        if (!nodes[rel.parent].children.some(c => c.id === nodes[rel.child].id)) {
          nodes[rel.parent].children.push(nodes[rel.child]);
        }
      }
    });

    // STEP 4: Find root node
    const childIds = new Set(relationships.map(r => r.child));
    let rootId = null;

    for (const nodeId in nodes) {
      if (!childIds.has(nodeId)) {
        rootId = nodeId;
        break;
      }
    }

    let mindMap;
    if (rootId && nodes[rootId]) {
      mindMap = nodes[rootId];
      console.log(`✅ Root node: ${rootId} - "${nodes[rootId].title}"`);
    } else {
      mindMap = {
        id: 'ROOT',
        title: subject,
        definition: `Mind map for ${subject}`,
        keyPoints: ['Generated from YouTube video'],
        children: Object.values(nodes),
      };
    }

    console.log('✅ Final structure:', mindMap);
    return mindMap;
  };

  // Save to history
  const saveToHistory = (mindMapObject) => {
    const historyItem = {
      id: Date.now().toString(),
      ...mindMapObject,
      savedAt: new Date().toISOString(),
    };

    const updated = [historyItem, ...mindMapHistory];
    setMindMapHistory(updated);
    localStorage.setItem('mindMapHistory', JSON.stringify(updated));
    console.log('💾 Saved to history:', historyItem);
    return historyItem;
  };

  // Load from history
  const loadFromHistory = (id) => {
    const item = mindMapHistory.find(m => m.id === id);
    if (item) {
      setMindMapData(item);
      setShowViewer(true);
      console.log('📂 Loaded from history:', item);
    }
  };

  // Delete from history
  const deleteFromHistory = (id) => {
    const updated = mindMapHistory.filter(m => m.id !== id);
    setMindMapHistory(updated);
    localStorage.setItem('mindMapHistory', JSON.stringify(updated));
    console.log('🗑️ Deleted from history:', id);
  };

  // Handle clear form
  const handleClear = () => {
    setYoutubeUrl('');
    setSubject('');
    setError('');
    setSuccess('');
    setProgressMessage('');
  };

  /**
   * ✨ MAIN FIX: 2-STEP PROCESS ✨
   * 
   * Step 1: Python service extracts transcript
   * Step 2: Java service generates mind map
   */
  const handleGenerateMindMap = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setProgressMessage('');

    // Validation
    if (!youtubeUrl.trim()) {
      setError('❌ Please enter a YouTube URL');
      return;
    }

    if (!validateYoutubeUrl(youtubeUrl)) {
      setError('❌ Invalid YouTube URL. Please enter a valid YouTube link');
      return;
    }

    if (!subject.trim()) {
      setError('❌ Please enter a subject/topic');
      return;
    }

    setLoading(true);
    setProgressMessage('📥 Extracting transcript from YouTube...');

    try {
      const studentId = getStudentId();
      if (!studentId) {
        setError('❌ Student ID not found. Please log in again.');
        setLoading(false);
        return;
      }

      console.log('🚀 Starting 2-step process:');
      console.log({ youtubeUrl, subject });

      // ============================================
      // STEP 1: Call Python Service to Extract Transcript
      // ============================================
      console.log('📥 STEP 1: Calling Python Transcript Service...');
      setProgressMessage('📥 Extracting transcript from YouTube...');

      let transcriptData = null;
      try {
        const transcriptResponse = await axios.post(
          TRANSCRIPT_API,
          { youtubeUrl: youtubeUrl.trim() },
          {
            headers: {
              'Content-Type': 'application/json',
            },
          }
        );

        console.log('✅ Transcript API Response:', transcriptResponse.data);

        if (transcriptResponse.data.success) {
          transcriptData = transcriptResponse.data.transcript;
          console.log(`✅ Transcript extracted: ${transcriptResponse.data.length} characters`);
        } else {
          throw new Error(transcriptResponse.data.message || 'Failed to extract transcript');
        }
      } catch (transcriptError) {
        console.error('❌ Step 1 Error - Python Service:', transcriptError);
        setError(`❌ Failed to extract transcript: ${transcriptError.response?.data?.message || transcriptError.message}`);
        setLoading(false);
        return;
      }

      // ============================================
      // STEP 2: Call Java Service to Generate Mind Map
      // ============================================
      console.log('🧠 STEP 2: Calling Java Mind Map API...');
      setProgressMessage('🧠 Generating mind map with AI...');

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
        }
      );

      console.log('✅ Java API Response:', response.data);

      if (response.data.success) {
        let mindMapJson = response.data.mindMapJson;

        // PRIMARY: Use mindMapJson if available
        if (!mindMapJson && response.data.mermaidCode) {
          console.log('📝 Converting Mermaid to mind map...');
          mindMapJson = convertMermaidToMindMap(
            response.data.mermaidCode,
            subject.trim()
          );
        }

        // FALLBACK: Create basic structure
        if (!mindMapJson) {
          mindMapJson = {
            title: subject.trim(),
            definition: `Mind map for ${subject.trim()}`,
            keyPoints: ['Generated from YouTube video'],
            children: [],
          };
        }

        // Ensure structure
        mindMapJson.children = mindMapJson.children || [];
        mindMapJson.keyPoints = mindMapJson.keyPoints || [];

        // Create mind map object
        const mindMap = {
          mindMapJson: mindMapJson,
          subject: subject.trim(),
          youtubeUrl: youtubeUrl.trim(),
          createdAt: new Date().toISOString(),
          mermaidCode: response.data.mermaidCode,
          videoId: response.data.videoId,
          transcriptLength: response.data.transcriptLength || transcriptData?.length || 0,
        };

        console.log('📚 Mind map created:', mindMap);
        console.log(`📊 Total nodes: ${(mindMapJson.children || []).length + 1}`);

        // Save to history and show
        saveToHistory(mindMap);
        setMindMapData(mindMap);
        setShowViewer(true);
        setSuccess('✅ Mind map generated successfully!');
        setProgressMessage('');

        // Clear form and switch to history
        setTimeout(() => {
          setYoutubeUrl('');
          setSubject('');
          setSuccess('');
          setActiveTab('history');
        }, 1500);
      } else {
        setError(`❌ ${response.data.message || 'Failed to generate mind map'}`);
      }
    } catch (err) {
      console.error('❌ Error:', err);
      const errorMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        'Failed to generate mind map. Please try again.';
      setError(`❌ ${errorMsg}`);
    } finally {
      setLoading(false);
      setProgressMessage('');
    }
  };

  if (!isOpen) return null;

  return (
    <>
      <div className="study-assistant-overlay" onClick={onClose} />

      <div className="study-assistant-container">
        {/* Header */}
        <div className="study-assistant-header">
          <h2>
            <span className="brain-icon">🧠</span>
            Understand Anything
          </h2>
          <p className="study-assistant-subtitle">
            Generate AI-powered mind maps from YouTube videos
          </p>
          <button className="close-btn" onClick={onClose} title="Close (Esc)">
            ✕
          </button>
        </div>

        {/* Tabs */}
        <div className="study-assistant-tabs">
          <button
            className={`tab-btn ${activeTab === 'create' ? 'active' : ''}`}
            onClick={() => setActiveTab('create')}
          >
            ➕ Create New
          </button>
          <button
            className={`tab-btn ${activeTab === 'history' ? 'active' : ''}`}
            onClick={() => setActiveTab('history')}
          >
            📚 History ({mindMapHistory.length})
          </button>
        </div>

        {/* Main Content */}
        <div className="study-assistant-content">
          {/* CREATE TAB */}
          {activeTab === 'create' && (
            <form onSubmit={handleGenerateMindMap} className="mindmap-form">
              <div className="form-section">
                {/* YouTube URL Input */}
                <div className="form-group">
                  <label htmlFor="youtube-url" className="form-label">
                    📹 YouTube URL
                  </label>
                  <div className="input-wrapper">
                    <input
                      id="youtube-url"
                      type="url"
                      placeholder="https://youtu.be/dQw4w9WgXcQ"
                      value={youtubeUrl}
                      onChange={(e) => setYoutubeUrl(e.target.value)}
                      disabled={loading}
                      className="form-input"
                    />
                    <button
                      type="button"
                      onClick={() => setYoutubeUrl('')}
                      className="input-clear-btn"
                      title="Clear URL"
                      disabled={!youtubeUrl || loading}
                    >
                      ✕
                    </button>
                  </div>
                  <small className="form-hint">Paste any YouTube video link</small>
                </div>

                {/* Subject Input */}
                <div className="form-group">
                  <label htmlFor="subject" className="form-label">
                    📚 Subject / Topic
                  </label>
                  <div className="input-wrapper">
                    <input
                      id="subject"
                      type="text"
                      placeholder="e.g., Web Development, Machine Learning"
                      value={subject}
                      onChange={(e) => setSubject(e.target.value)}
                      disabled={loading}
                      className="form-input"
                    />
                    <button
                      type="button"
                      onClick={() => setSubject('')}
                      className="input-clear-btn"
                      title="Clear subject"
                      disabled={!subject || loading}
                    >
                      ✕
                    </button>
                  </div>
                  <small className="form-hint">What topic is this video about?</small>
                </div>

                {/* Action Buttons */}
                <div className="form-actions">
                  <button
                    type="submit"
                    disabled={loading || !youtubeUrl.trim() || !subject.trim()}
                    className="btn btn-primary btn-generate"
                  >
                    {loading ? (
                      <>
                        <span className="spinner" />
                        Processing...
                      </>
                    ) : (
                      <>🔄 Generate Mind Map</>
                    )}
                  </button>

                  <button
                    type="button"
                    onClick={handleClear}
                    disabled={loading}
                    className="btn btn-secondary"
                  >
                    🔄 Clear
                  </button>
                </div>
              </div>

              {/* Progress Message */}
              {progressMessage && (
                <div className="message-box message-info">
                  <span className="spinner" style={{ marginRight: '8px' }} />
                  <span>{progressMessage}</span>
                </div>
              )}

              {/* Error Message */}
              {error && (
                <div className="message-box message-error">
                  <span className="message-icon">⚠️</span>
                  <span>{error}</span>
                </div>
              )}

              {/* Success Message */}
              {success && (
                <div className="message-box message-success">
                  <span className="message-icon">✓</span>
                  <span>{success}</span>
                </div>
              )}

              {/* Loading State */}
              {loading && (
                <div className="processing-state">
                  <div className="processing-spinner">
                    <div className="spinner-ring"></div>
                  </div>
                  <p className="processing-text">{progressMessage || 'Generating your mind map...'}</p>
                  <p className="processing-hint">
                    This may take a few moments (extracting transcript, generating with AI)
                  </p>
                </div>
              )}
            </form>
          )}

          {/* HISTORY TAB */}
          {activeTab === 'history' && (
            <div className="history-section">
              {mindMapHistory.length === 0 ? (
                <div className="empty-state">
                  <p>📂 No mind maps yet</p>
                  <p>Create one in the "Create New" tab!</p>
                </div>
              ) : (
                <div className="history-list">
                  {mindMapHistory.map((item) => (
                    <div key={item.id} className="history-item">
                      <div className="history-item-content">
                        <h4>📚 {item.subject}</h4>
                        <p className="history-url">{item.youtubeUrl}</p>
                        <small>
                          📅 {new Date(item.savedAt).toLocaleDateString()}
                        </small>
                      </div>
                      <div className="history-item-actions">
                        <button
                          className="btn btn-small btn-primary"
                          onClick={() => loadFromHistory(item.id)}
                        >
                          👁️ View
                        </button>
                        <button
                          className="btn btn-small btn-secondary"
                          onClick={() => deleteFromHistory(item.id)}
                        >
                          🗑️ Delete
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Mind Map Viewer Modal */}
      <MindMapViewerModal
        isOpen={showViewer}
        onClose={() => setShowViewer(false)}
        mindMap={mindMapData}
      />
    </>
  );
};

export default StudyAssistant;