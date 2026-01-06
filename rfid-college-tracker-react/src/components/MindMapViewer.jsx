import React, { useState, useMemo } from 'react';
import './MindMapTreeViewer.css';

/**
 * MindMapTreeViewer - NotebookLM style recursive tree component
 * FIXED VERSION - NOW INCLUDES DEFINITIONS RENDERING
 * Features:
 * - Unlimited nesting levels
 * - Click to expand/collapse
 * - Search/filter functionality
 * - Key points display
 * - DEFINITIONS DISPLAY ✅ (NEW!)
 * - Smooth animations
 * - Responsive design
 */
const MindMapTreeViewer = ({ mindMapData, searchQuery = '', onNodeClick = null }) => {
  const [expandedNodes, setExpandedNodes] = useState({});
  const [breadcrumb, setBreadcrumb] = useState([]);

  // Helper function to create unique node IDs
  const getNodeId = (node, path = '') => {
    return `${path}/${node.title}`.replace(/\//g, '-');
  };

  // Filter nodes based on search query - NOW INCLUDES DEFINITIONS!
  const filterNodes = (node, path = '') => {
    if (!searchQuery.trim()) {
      return true;
    }
    const query = searchQuery.toLowerCase();
    const nodeId = getNodeId(node, path);

    // Check if title or any key point matches
    const titleMatch = node.title.toLowerCase().includes(query);
    const keyPointsMatch = node.keyPoints?.some(point => 
      point.toLowerCase().includes(query)
    ) || false;
    
    // NEW: Check if definition matches
    const definitionMatch = node.definition?.toLowerCase().includes(query) || false;

    // Check if any child matches
    const childMatch = node.children?.some(child => 
      filterNodes(child, nodeId)
    ) || false;

    return titleMatch || keyPointsMatch || definitionMatch || childMatch;
  };

  // Get filtered and processed tree
  const filteredData = useMemo(() => {
    if (!mindMapData) return null;
    if (searchQuery.trim()) {
      return filterNodes(mindMapData) ? mindMapData : null;
    }
    return mindMapData;
  }, [mindMapData, searchQuery]);

  // Toggle node expansion
  const toggleNode = (nodeId) => {
    setExpandedNodes(prev => ({
      ...prev,
      [nodeId]: !prev[nodeId]
    }));
  };

  // Calculate depth level for styling
  const getDepthLevel = (path) => {
    return path.split('/').filter(p => p).length;
  };

  // Highlight search results
  const highlightText = (text) => {
    if (!searchQuery.trim()) return text;
    const query = searchQuery;
    const parts = text.split(new RegExp(`(${query})`, 'gi'));
    return parts.map((part, idx) =>
      part.toLowerCase() === query.toLowerCase() ?
        <mark key={idx}>{part}</mark> : part
    );
  };

  // Recursive node renderer
  const renderNode = (node, depth = 0, path = '') => {
    const nodeId = getNodeId(node, path);
    const isExpanded = expandedNodes[nodeId];
    const hasChildren = node.children && node.children.length > 0;
    const level = getDepthLevel(path);
    const depthLevel = Math.min(level, 5);

    return (
      <div key={nodeId} className={`tree-node tree-level-${depthLevel} ${hasChildren ? 'has-children' : ''}`}>
        {/* Node Header */}
        <div
          className={`node-header ${!hasChildren ? 'no-children' : ''} ${isExpanded ? 'expanded' : 'collapsed'}`}
          onClick={() => {
            toggleNode(nodeId);
            if (onNodeClick) {
              onNodeClick(node, nodeId);
            }
            console.log('Node clicked:', node, nodeId);
          }}
          role="button"
          tabIndex={0}
          onKeyDown={(e) => {
            if (e.key === 'Enter' || e.key === ' ') {
              e.preventDefault();
              toggleNode(nodeId);
              if (onNodeClick) {
                onNodeClick(node, nodeId);
              }
            }
          }}
        >
          {/* Toggle Icon */}
          {hasChildren && (
            <span className={`toggle-icon ${isExpanded ? 'expanded' : 'collapsed'}`}>
              ▶
            </span>
          )}
          {!hasChildren && <span className="toggle-placeholder" />}

          {/* Node Indicator */}
          <div className={`node-indicator level-${depthLevel}`} />

          {/* Title */}
          <div className="node-title-wrapper">
            <h3 className="node-title">
              {highlightText(node.title || 'Untitled')}
            </h3>
            {hasChildren && (
              <span className="children-count">{node.children.length}</span>
            )}
          </div>
        </div>

        {/* Expanded Content */}
        {isExpanded && (
          <div className="node-content">
            {/* Key Points Section */}
            {node.keyPoints && node.keyPoints.length > 0 && (
              <div className="key-points-section">
                <div className="key-points-label">
                  📌 Key Points
                </div>
                <ul className="key-points-list">
                  {node.keyPoints.map((point, idx) => (
                    <li key={idx} className="key-point-item">
                      <span className="point-bullet">•</span>
                      <span className="point-text">
                        {typeof point === 'string' 
                          ? highlightText(point)
                          : JSON.stringify(point)
                        }
                      </span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Definition Section - CRITICAL! THIS IS NEW! */}
            {node.definition && (
              <div className="definition-section">
                <div className="definition-label">
                  📚 Definition
                </div>
                <p className="definition-text">
                  {highlightText(node.definition)}
                </p>
              </div>
            )}

            {/* Children Section */}
            {hasChildren && (
              <div className="children-container">
                {node.children.map((child, idx) =>
                  renderNode(child, depth + 1, nodeId)
                )}
              </div>
            )}
          </div>
        )}
      </div>
    );
  };

  // Main render
  if (!mindMapData) {
    return (
      <div className="mind-map-tree-viewer">
        <div className="tree-viewer-empty">
          <div className="empty-icon">📚</div>
          <p>No mind map data available</p>
        </div>
      </div>
    );
  }

  if (searchQuery.trim() && !filteredData) {
    return (
      <div className="mind-map-tree-viewer">
        <div className="tree-viewer-empty">
          <div className="empty-icon">🔍</div>
          <p>No results found for "{searchQuery}"</p>
        </div>
      </div>
    );
  }

  return (
    <div className="mind-map-tree-viewer">
      <div className="tree-root">
        <div className="root-header">
          <h2 className="root-title">
            {highlightText(mindMapData.title || 'Mind Map')}
          </h2>
        </div>
        <div className="tree-container">
          {renderNode(mindMapData, 0, '')}
        </div>
      </div>
    </div>
  );
};

export default MindMapTreeViewer;