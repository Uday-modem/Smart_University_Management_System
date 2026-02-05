import React, { useState, useEffect } from 'react';
import mermaid from 'mermaid';
import './MindMapViewerModal.css';

/**
 * ✅ FINAL FIXED MindMapViewerModal - Direct Mermaid rendering
 * 
 * FIX: Render Mermaid directly without relying on pre-existing DOM
 * Uses mermaid.render() instead of looking for container
 */

const MindMapViewerModal = ({ isOpen, onClose, mindMap = null }) => {
  const [mermaidRendered, setMermaidRendered] = useState(false);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [svgContent, setSvgContent] = useState(null);

  // Initialize Mermaid on component mount
  useEffect(() => {
    if (typeof mermaid !== 'undefined') {
      mermaid.initialize({
        startOnLoad: false,
        theme: 'default',
        securityLevel: 'loose'
      });
    }
  }, []);

  // Render Mermaid graph when mindMap changes
  useEffect(() => {
    if (!isOpen || !mindMap) {
      setMermaidRendered(false);
      setSvgContent(null);
      return;
    }

    console.log('📚 Mind map received:', mindMap);

    // Check if mermaidCode exists
    if (!mindMap.mermaidCode) {
      console.warn('❌ No mermaidCode in mindMap');
      setError('No Mermaid code available. Backend may not have generated the graph.');
      return;
    }

    const renderGraph = async () => {
      try {
        setLoading(true);
        setError(null);

        console.log('🔄 Starting Mermaid render...');
        console.log('📊 Mermaid code length:', mindMap.mermaidCode.length);
        console.log('📝 Mermaid code preview:', mindMap.mermaidCode.substring(0, 100));

        // Use mermaid.render() to get SVG directly
        const { svg, bindFunctions } = await mermaid.render(
          `mermaid-graph-${Date.now()}`,
          mindMap.mermaidCode
        );

        console.log('✅ Mermaid render successful, SVG generated');
        console.log('📏 SVG size:', svg.length, 'characters');

        setSvgContent(svg);
        setMermaidRendered(true);
        setLoading(false);

        console.log('✅ Mermaid graph rendered successfully!');
      } catch (err) {
        console.error('❌ Error rendering Mermaid graph:', err);
        setError(`Failed to render graph: ${err.message}`);
        setSvgContent(null);
        setLoading(false);
      }
    };

    renderGraph();
  }, [isOpen, mindMap]);

  if (!isOpen) return null;

  return (
    <>
      {/* Backdrop */}
      <div
        className="mind-map-modal-backdrop"
        onClick={(e) => {
          if (e.target === e.currentTarget) {
            onClose();
          }
        }}
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          zIndex: 999
        }}
      />

      {/* Modal Container */}
      <div
        className="mind-map-modal-container"
        style={{
          position: 'fixed',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          backgroundColor: 'white',
          borderRadius: '12px',
          boxShadow: '0 20px 60px rgba(0, 0, 0, 0.3)',
          width: '90%',
          maxWidth: '1200px',
          height: '90vh',
          maxHeight: '900px',
          display: 'flex',
          flexDirection: 'column',
          zIndex: 1000,
          overflow: 'hidden'
        }}
      >
        {/* Header */}
        <div
          style={{
            padding: '20px',
            borderBottom: '1px solid #e0e0e0',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexShrink: 0
          }}
        >
          <div>
            <h2 style={{ margin: 0, fontSize: '20px', color: '#333' }}>
              📊 Mind Map Visualization
            </h2>
            {mindMap?.subject && (
              <p style={{ margin: '5px 0 0 0', fontSize: '14px', color: '#666' }}>
                {mindMap.subject}
              </p>
            )}
          </div>
          <button
            onClick={onClose}
            style={{
              background: 'none',
              border: 'none',
              fontSize: '28px',
              cursor: 'pointer',
              color: '#666',
              padding: '0',
              width: '40px',
              height: '40px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
            title="Close (Press Esc)"
          >
            ✕
          </button>
        </div>

        {/* Loading State */}
        {loading && (
          <div
            style={{
              flex: 1,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '20px',
              backgroundColor: '#f9f9f9'
            }}
          >
            <div
              style={{
                width: '50px',
                height: '50px',
                border: '4px solid #f3f3f3',
                borderTop: '4px solid #3498db',
                borderRadius: '50%',
                animation: 'spin 1s linear infinite'
              }}
            />
            <p style={{ color: '#666', fontSize: '16px', margin: 0 }}>
              🔄 Rendering mind map from Mermaid...
            </p>
            <style>{`
              @keyframes spin {
                0% { transform: rotate(0deg); }
                100% { transform: rotate(360deg); }
              }
            `}</style>
          </div>
        )}

        {/* Error State */}
        {error && !loading && (
          <div
            style={{
              flex: 1,
              padding: '30px',
              backgroundColor: '#ffebee',
              borderRadius: '0',
              margin: '0',
              display: 'flex',
              flexDirection: 'column',
              gap: '15px',
              overflow: 'auto'
            }}
          >
            <p style={{ color: '#c62828', fontSize: '16px', margin: 0 }}>
              ⚠️ {error}
            </p>
            <details style={{ cursor: 'pointer' }}>
              <summary style={{ color: '#666', fontWeight: 'bold', marginBottom: '10px' }}>
                Debug Information
              </summary>
              <pre
                style={{
                  backgroundColor: '#f5f5f5',
                  padding: '10px',
                  borderRadius: '4px',
                  fontSize: '12px',
                  overflow: 'auto',
                  margin: 0
                }}
              >
                {JSON.stringify(
                  {
                    mermaidCodeExists: !!mindMap?.mermaidCode,
                    mermaidCodeLength: mindMap?.mermaidCode?.length,
                    videoTitle: mindMap?.videoTitle,
                    subject: mindMap?.subject,
                    transcriptLength: mindMap?.transcriptLength,
                    mermaidCodePreview: mindMap?.mermaidCode?.substring(0, 100)
                  },
                  null,
                  2
                )}
              </pre>
            </details>
          </div>
        )}

        {/* Mermaid SVG Container */}
        {!error && !loading && mermaidRendered && svgContent && (
          <div
            style={{
              flex: 1,
              padding: '20px',
              overflow: 'auto',
              display: 'flex',
              justifyContent: 'center',
              alignItems: 'flex-start',
              backgroundColor: '#fff'
            }}
          >
            <div
              dangerouslySetInnerHTML={{ __html: svgContent }}
              style={{
                display: 'flex',
                justifyContent: 'center',
                width: '100%'
              }}
            />
          </div>
        )}

        {/* No render yet message */}
        {!mermaidRendered && !error && !loading && (
          <div
            style={{
              flex: 1,
              padding: '40px',
              textAlign: 'center',
              color: '#999',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}
          >
            <p style={{ margin: 0 }}>Waiting for mind map data...</p>
          </div>
        )}

        {/* Footer */}
        <div
          style={{
            padding: '15px 20px',
            borderTop: '1px solid #e0e0e0',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexShrink: 0,
            backgroundColor: '#f9f9f9'
          }}
        >
          <div style={{ display: 'flex', gap: '20px', fontSize: '13px', color: '#666' }}>
            {mindMap?.mermaidCode && (
              <>
                <span>📝 Graph: {mindMap.mermaidCode.length} chars</span>
                {mindMap?.transcriptLength && (
                  <span>🎬 Transcript: {mindMap.transcriptLength} chars</span>
                )}
              </>
            )}
          </div>
          <button
            onClick={onClose}
            style={{
              padding: '8px 16px',
              backgroundColor: '#3498db',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: '500'
            }}
          >
            Close (Esc)
          </button>
        </div>
      </div>
    </>
  );
};

export default MindMapViewerModal;