import React, { useState, useMemo } from 'react';
import './MindMapTreeViewer.css';

/**
 * MindMapTreeViewer - NotebookLM style recursive tree component
 * PRODUCTION VERSION - WITH KEY POINTS AND DEFINITIONS DISPLAY
 * Features:
 * - Unlimited nesting levels
 * - Click to expand/collapse
 * - Search/filter on all fields
 * - KEY POINTS DISPLAY ✅
 * - DEFINITIONS DISPLAY ✅
 * - Smooth animations
 * - Responsive design
 * - Mobile friendly
 */
const MindMapTreeViewer = ({ mindMapData, searchQuery = '', onNodeClick = null }) => {
  const [expandedNodes, setExpandedNodes] = useState({});

  // Helper function to create unique node IDs
  const getNodeId = (node, path = '') => {
    return `${path}/${node.title}`.replace(/\//g, '-');
  };

  // Filter nodes based on search query
  const filterNodes = (node, path = '') => {
    if (!searchQuery.trim()) {
      return true;
    }
    const query = searchQuery.toLowerCase();
    const nodeId = getNodeId(node, path);

    // Check if title matches
    const titleMatch = node.title?.toLowerCase().includes(query) || false;

    // Check if any key point matches
    const keyPointsMatch = node.keyPoints?.some(point =>
      String(point).toLowerCase().includes(query)
    ) || false;

    // Check if definition matches
    const definitionMatch = node.definition?.toLowerCase().includes(query) || false;

    // Check if any child matches
    const childMatch = node.children?.some(child =>
      filterNodes(child, nodeId)
    ) || false;

    return titleMatch || keyPointsMatch || definitionMatch || childMatch;
  };

  // Get filtered tree
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

  // Highlight search results in text
  const highlightText = (text) => {
    if (!searchQuery.trim() || !text) return text;
    const query = searchQuery;
    const parts = String(text).split(new RegExp(`(${query})`, 'gi'));
    return parts.map((part, idx) =>
      part?.toLowerCase() === query.toLowerCase() ?
        <mark key={idx}>{part}</mark> : part
    );
  };

  // Recursive node renderer
  const renderNode = (node, depth = 0, path = '') => {
    if (!node) return null;

    const nodeId = getNodeId(node, path);
    const isExpanded = expandedNodes[nodeId];
    const hasChildren = node.children && node.children.length > 0;
    const hasKeyPoints = node.keyPoints && node.keyPoints.length > 0;
    const hasDefinition = node.definition && String(node.definition).trim().length > 0;
    const level = getDepthLevel(path);

    return (
      <div key={nodeId} className={`tree-node tree-level-${Math.min(level, 5)}`}>
        {/* Node Header */}
        <div
          className={`node-header ${!hasChildren && !hasKeyPoints && !hasDefinition ? 'no-children' : ''}`}
          onClick={() => {
            toggleNode(nodeId);
            if (onNodeClick) {
              onNodeClick(node, nodeId);
            }
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
          {(hasChildren || hasKeyPoints || hasDefinition) && (
            <span className={`toggle-icon ${isExpanded ? 'expanded' : ''}`}>
              ▶
            </span>
          )}
          {!(hasChildren || hasKeyPoints || hasDefinition) && <span className="toggle-placeholder" />}

          {/* Node Title */}
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
            {hasKeyPoints && (
              <div className="key-points-section">
                <div className="key-points-label">📌 KEY POINTS:</div>
                <ul className="key-points-list">
                  {node.keyPoints.map((point, idx) => (
                    <li key={idx} className="key-point-item">
                      <span className="point-bullet">●</span>
                      <span className="point-text">
                        {highlightText(String(point))}
                      </span>
                    </li>
                  ))}
                </ul>
              </div>
            )}

            {/* Definition Section */}
            {hasDefinition && (
              <div className="definition-section">
                <div className="definition-label">📚 Definition</div>
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
  if (!filteredData) {
    return (
      <div className="mind-map-tree-viewer">
        <div className="tree-viewer-empty">
          <div className="empty-icon">📭</div>
          <p>{searchQuery.trim() ? `No results found for "${searchQuery}"` : 'No mind map data available'}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="mind-map-tree-viewer">
      <div className="tree-root">
        <div className="tree-container">
          {renderNode(filteredData, 0, '')}
        </div>
      </div>
    </div>
  );
};

export default MindMapTreeViewer;