import os
import re
import json
import logging
import datetime
from flask import Flask, request, jsonify
from flask_cors import CORS
import subprocess
import ssl

# ===== SSL FIX FOR MAC =====
ssl._create_default_https_context = ssl._create_unverified_context

# ===== LOGGING SETUP =====
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('transcript-service.log'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)

# ===== FLASK APP SETUP =====
app = Flask(__name__)
CORS(app)

logger.info("=" * 80)
logger.info("✅ Transcript Extraction Service v10.0 - STARTING")
logger.info("🎯 Using yt-dlp + Whisper (Speech-to-Text AI!)")
logger.info("=" * 80)

# ===== HELPER FUNCTIONS =====

def clean_text(text):
    """Clean and normalize text"""
    if not text:
        return ""
    text = re.sub(r'\s+', ' ', text)
    return text.strip()

def parse_vtt(content):
    """
    ✅ FIX: Clean WebVTT parsing with deduplication
    """
    lines = content.split('\n')
    text_lines = []
    seen_lines = set() # Dedup set
    
    for line in lines:
        line = line.strip()
        # Skip Headers/Metadata
        if line.startswith('WEBVTT') or line.startswith('Kind:') or line.startswith('Language:'):
            continue
        # Skip Timestamps
        if '-->' in line:
            continue
        # Skip Empty or Numbers
        if not line or line.isdigit():
            continue
        
        # Simple Deduplication Strategy
        # If line is exactly same as last added, skip
        if text_lines and text_lines[-1] == line:
            continue
            
        text_lines.append(line)

    # Join and second pass cleanup
    full_text = " ".join(text_lines)
    # Remove excessive repetition (simple heuristic)
    return re.sub(r'\s+', ' ', full_text).strip()

def extract_transcript_captions(video_id: str):
    """
    Try to extract captions first (fast method)
    """
    try:
        logger.info(f"🔍 Trying caption extraction for {video_id}...")
        url = f"https://www.youtube.com/watch?v={video_id}"
        
        # Build yt-dlp command for captions
        cmd = [
            "yt-dlp",
            "--write-subs",
            "--write-auto-subs",
            "--sub-langs", "en",
            "--skip-download",
            "-o", f"/tmp/{video_id}.%(ext)s",
            url
        ]
        
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=30)
        
        if result.returncode == 0:
            logger.info(f"✅ yt-dlp command succeeded!")
            
            # Look for subtitle files
            import glob
            subs = glob.glob(f"/tmp/{video_id}*.vtt") + glob.glob(f"/tmp/{video_id}*.srt")
            
            if subs:
                sub_file = subs[0]
                logger.info(f"📄 Found caption file: {sub_file}")
                
                with open(sub_file, 'r') as f:
                    content = f.read()
                
                # ✅ FIX v10.0 - Properly parse the VTT/SRT file
                if sub_file.endswith('.vtt'):
                    text = parse_vtt(content)
                else:
                    # Parse SRT
                    text = re.sub(r'\d+\n\d{2}:\d{2}:\d{2},\d{3} --> \d{2}:\d{2}:\d{2},\d{3}\n', '', content)
                    text = clean_text(text)
                
                os.remove(sub_file)
                logger.info(f"✅ Got {len(text)} chars from captions")
                return text, "captions_manual_or_auto"
        
        logger.warning(f"⚠️ No captions found via yt-dlp")
        return None, None
    
    except Exception as e:
        logger.warning(f"⚠️ Caption extraction failed: {e}")
        return None, None

def extract_transcript_whisper(video_id: str):
    """
    ✅ FIX v10.0 - Use OpenAI Whisper for speech-to-text
    When captions don't exist, extract audio and transcribe with AI!
    Install: pip install yt-dlp openai-whisper
    """
    try:
        logger.info(f"🎤 Using Whisper AI to transcribe audio for {video_id}...")
        url = f"https://www.youtube.com/watch?v={video_id}"
        
        # Download ONLY audio (m4a format, lightweight)
        audio_file = f"/tmp/{video_id}.m4a"
        cmd = [
            "yt-dlp",
            "-f", "bestaudio[ext=m4a]",
            "-o", audio_file,
            url
        ]
        
        logger.info(f"🔊 Downloading audio...")
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=60)
        
        if result.returncode == 0 and os.path.exists(audio_file):
            logger.info(f"✅ Audio downloaded: {audio_file}")
            
            # Use Whisper to transcribe
            logger.info(f"🧠 Running Whisper transcription...")
            try:
                import whisper
                
                # Load model (tiny = fastest, base = better quality)
                model = whisper.load_model("small")
                logger.info(f"✅ Whisper model loaded")
                
                # Transcribe
                result = model.transcribe(audio_file, language="en")
                text = result["text"]
                text = clean_text(text)
                
                # Cleanup
                os.remove(audio_file)
                
                logger.info(f"✅ Got {len(text)} chars from Whisper AI")
                return text, "whisper_ai_transcription"
            
            except ImportError:
                logger.error("❌ whisper not installed! Run: pip install openai-whisper")
                raise Exception("openai-whisper not installed. Run: pip install openai-whisper")
        else:
            logger.error(f"❌ Audio download failed")
            raise Exception("Failed to download audio")
    
    except Exception as e:
        logger.error(f"❌ Whisper transcription failed: {e}")
        raise Exception(f"Failed to transcribe with Whisper: {str(e)}")

def fetch_transcript(youtube_url: str):
    """
    Extract video ID and fetch transcript
    Strategy: Try captions first → Fall back to Whisper AI
    """
    try:
        # Extract video ID from various URL formats
        if "watch?v=" in youtube_url:
            video_id = youtube_url.split("watch?v=")[1].split("&")[0]
        elif "youtu.be/" in youtube_url:
            video_id = youtube_url.split("youtu.be/")[1].split("?")[0]
        else:
            video_id = youtube_url.strip()[:11]
        
        logger.info(f"📺 Video ID extracted: {video_id}")
        
        # Strategy 1: Try captions first (fast)
        logger.info(f"📋 Strategy 1: Attempting caption extraction...")
        text, source = extract_transcript_captions(video_id)
        
        if text and len(text.strip()) >= 50:
            logger.info(f"✅ Transcript fetched: {len(text)} chars from {source}")
            return text, source
        
        # Strategy 2: Fall back to Whisper AI
        logger.info(f"🎤 Strategy 2: Falling back to Whisper AI...")
        text, source = extract_transcript_whisper(video_id)
        
        if not text or len(text.strip()) < 50:
            raise Exception(f"Transcript is too short: {len(text) if text else 0} chars")
        
        logger.info(f"✅ Transcript fetched: {len(text)} chars from {source}")
        return text, source
    
    except Exception as e:
        logger.error(f"❌ fetch_transcript failed: {str(e)}")
        raise Exception(f"Failed to fetch transcript: {str(e)}")

# ===== API ENDPOINTS =====

# ✅ NEW ENDPOINT - Health check for Java backend
@app.route('/api/transcript/health', methods=['GET', 'OPTIONS'])
def health_check_api():
    """✅ Health check endpoint for Java backend"""
    if request.method == 'OPTIONS':
        return '', 200
    
    logger.info("🏥 Health check requested from Java backend")
    response = jsonify({
        'status': 'healthy',
        'service': 'YouTube Transcript Extraction Service',
        'version': '10.0',
        'backend': 'yt-dlp + Whisper AI',
        'timestamp': datetime.datetime.now().isoformat(),
        'message': '✅ Service is running'
    })
    response.headers.add('Access-Control-Allow-Origin', '*')
    return response, 200

# Also keep the old /health endpoint for backward compatibility
@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'service': 'YouTube Transcript Extraction Service',
        'version': '10.0',
        'backend': 'yt-dlp + Whisper AI',
        'timestamp': datetime.datetime.now().isoformat()
    }), 200

@app.route('/api/transcript/extract', methods=['POST', 'OPTIONS'])
def extract_transcript_endpoint():
    """
    ✅ Extract transcript from YouTube URL
    
    Request: {"youtubeUrl": "https://www.youtube.com/watch?v=..."}
    Response: {"success": true, "transcript": "...", "source": "...", "length": 12345}
    """
    if request.method == 'OPTIONS':
        return '', 200
    
    try:
        data = request.get_json()
        youtube_url = data.get('youtubeUrl')
        
        if not youtube_url:
            return jsonify({
                'success': False,
                'error': 'No URL provided',
                'message': 'Please provide youtubeUrl in request body'
            }), 400
        
        logger.info(f"📥 Transcript request for: {youtube_url}")
        
        # Extract transcript (tries captions, falls back to Whisper)
        transcript, source = fetch_transcript(youtube_url)
        
        # Validate result
        if not transcript or len(transcript.strip()) < 50:
            logger.error(f"❌ Transcript too short: {len(transcript) if transcript else 0} chars")
            return jsonify({
                'success': False,
                'error': f'Transcript too short: {len(transcript) if transcript else 0} chars',
                'source': source,
                'length': len(transcript) if transcript else 0
            }), 400
        
        # Success response
        response = {
            'success': True,
            'transcript': transcript,
            'source': source,
            'length': len(transcript),
            'message': f'✅ Transcript extracted from {source}!',
            'timestamp': datetime.datetime.now().isoformat(),
            'videoId': youtube_url.split('v=')[-1][:11] if 'v=' in youtube_url else 'unknown'
        }
        
        logger.info(f"✅ Response: {response['length']} chars from {source}")
        response_obj = jsonify(response)
        response_obj.headers.add('Access-Control-Allow-Origin', '*')
        return response_obj, 200
    
    except Exception as e:
        logger.error(f"❌ Error in extract_transcript_endpoint: {str(e)}")
        response = jsonify({
            'success': False,
            'error': str(e),
            'message': 'Failed to extract transcript'
        })
        response.headers.add('Access-Control-Allow-Origin', '*')
        return response, 500

@app.route('/api/test', methods=['GET', 'OPTIONS'])
def test_endpoint():
    """Test endpoint to verify service is running"""
    if request.method == 'OPTIONS':
        return '', 200
    
    return jsonify({
        'message': '✅ Service is running and ready for use',
        'environment': 'PRODUCTION - Returns REAL YouTube transcripts!',
        'version': '10.0',
        'backend': 'yt-dlp + Whisper AI',
        'strategy': 'Try captions first → Fall back to Whisper AI',
        'endpoints': {
            'health_new': '/api/transcript/health',
            'health_legacy': '/health',
            'extract': '/api/transcript/extract',
            'test': '/api/test'
        }
    }), 200

# ===== ERROR HANDLERS =====

@app.errorhandler(400)
def bad_request(error):
    response = jsonify({'error': 'Bad request', 'message': str(error)})
    response.headers.add('Access-Control-Allow-Origin', '*')
    return response, 400

@app.errorhandler(404)
def not_found(error):
    response = jsonify({
        'error': 'Not found',
        'message': 'Endpoint does not exist',
        'available_endpoints': [
            '/api/transcript/health',
            '/api/transcript/extract',
            '/api/test',
            '/health'
        ]
    })
    response.headers.add('Access-Control-Allow-Origin', '*')
    return response, 404

@app.errorhandler(500)
def internal_error(error):
    logger.error(f"Internal server error: {str(error)}")
    response = jsonify({'error': 'Internal server error', 'message': str(error)})
    response.headers.add('Access-Control-Allow-Origin', '*')
    return response, 500

# ===== MAIN =====

if __name__ == '__main__':
    logger.info("=" * 80)
    logger.info("🚀 Starting Flask application...")
    logger.info("📍 Running on: http://localhost:5000")
    logger.info("✅ Using yt-dlp + Whisper AI for transcript extraction!")
    logger.info("🎤 Strategy: Try captions first → Fall back to Whisper AI")
    logger.info("🎯 Ready to extract transcripts from ANY YouTube video!")
    logger.info("=" * 80)
    logger.info("")
    logger.info("📚 Available Endpoints:")
    logger.info("  ✅ /api/transcript/health [GET] - Health check (Java backend)")
    logger.info("  ✅ /health [GET] - Health check (legacy)")
    logger.info("  ✅ /api/transcript/extract [POST] - Extract transcript")
    logger.info("  ✅ /api/test [GET] - Test endpoint")
    logger.info("")
    
    app.run(
        host='0.0.0.0',
        port=5000,
        debug=False,
        use_reloader=False
    )