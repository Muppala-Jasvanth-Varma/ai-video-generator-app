from gtts import gTTS
import os
import tempfile
from flask import Flask, jsonify, send_file, request
import requests
import json

app = Flask(__name__)

def create_audio_from_wikipedia_data(year_data, language='en', slow=False):
    """
    Convert Wikipedia year data to audio files
    """
    audio_files = {}
    
    try:
        # Create audio for main summary
        if year_data.get('paragraph'):
            summary_text = f"Year {year_data['year']} summary: {year_data['paragraph']}"
            summary_tts = gTTS(text=summary_text, lang=language, slow=slow)
            
            # Save to temporary file
            summary_file = tempfile.NamedTemporaryFile(delete=False, suffix='.mp3', prefix='summary_')
            summary_tts.save(summary_file.name)
            audio_files['summary'] = summary_file.name
        
        # Create audio for timeline events
        timeline_audio_files = []
        if year_data.get('timeline'):
            for i, event in enumerate(year_data['timeline']):
                event_text = f"Event {i+1}: {event['title']}. Impact: {event['impact']}"
                event_tts = gTTS(text=event_text, lang=language, slow=slow)
                
                event_file = tempfile.NamedTemporaryFile(delete=False, suffix='.mp3', prefix=f'event_{i+1}_')
                event_tts.save(event_file.name)
                timeline_audio_files.append({
                    'event_number': i+1,
                    'title': event['title'],
                    'file_path': event_file.name
                })
        
        audio_files['timeline_events'] = timeline_audio_files
        
        # Create combined audio (all content together)
        full_text = f"Historical overview for the year {year_data['year']}. "
        
        if year_data.get('paragraph'):
            full_text += f"Summary: {year_data['paragraph']} "
        
        if year_data.get('timeline'):
            full_text += "Major events: "
            for i, event in enumerate(year_data['timeline']):
                full_text += f"Event {i+1}: {event['title']}. {event['impact']}. "
        
        combined_tts = gTTS(text=full_text, lang=language, slow=slow)
        combined_file = tempfile.NamedTemporaryFile(delete=False, suffix='.mp3', prefix='combined_')
        combined_tts.save(combined_file.name)
        audio_files['combined'] = combined_file.name
        
        return audio_files
        
    except Exception as e:
        print(f"Error creating audio: {str(e)}")
        return None

@app.route('/api/wikipedia/year/<int:year>/audio', methods=['GET'])
def get_year_audio(year):
    """
    Endpoint to get audio files for a specific year
    """
    try:
        # Get the Wikipedia data (assuming you have this endpoint)
        wiki_response = requests.get(f'http://localhost:5000/api/wikipedia/year/{year}')
        
        if not wiki_response.ok:
            return jsonify({'error': 'Failed to fetch Wikipedia data'}), 500
        
        wiki_data = wiki_response.json()
        
        if not wiki_data.get('success'):
            return jsonify({'error': 'Wikipedia data not available'}), 404
        
        # Get audio options from query parameters
        language = request.args.get('lang', 'en')
        slow = request.args.get('slow', 'false').lower() == 'true'
        audio_type = request.args.get('type', 'combined')  # summary, combined, or timeline
        
        # Create audio files
        audio_files = create_audio_from_wikipedia_data(
            wiki_data['yearSummary'], 
            language=language, 
            slow=slow
        )
        
        if not audio_files:
            return jsonify({'error': 'Failed to create audio files'}), 500
        
        # Return the requested audio type
        if audio_type == 'summary' and 'summary' in audio_files:
            return send_file(audio_files['summary'], as_attachment=True, 
                           download_name=f'{year}_summary.mp3')
        
        elif audio_type == 'combined' and 'combined' in audio_files:
            return send_file(audio_files['combined'], as_attachment=True, 
                           download_name=f'{year}_combined.mp3')
        
        elif audio_type == 'timeline' and 'timeline_events' in audio_files:
            # Return list of timeline audio files info
            timeline_info = []
            for event_audio in audio_files['timeline_events']:
                timeline_info.append({
                    'event_number': event_audio['event_number'],
                    'title': event_audio['title'],
                    'audio_url': f'/api/wikipedia/year/{year}/audio/event/{event_audio["event_number"]}'
                })
            
            return jsonify({
                'success': True,
                'year': year,
                'timeline_audio': timeline_info
            })
        
        else:
            return jsonify({'error': 'Invalid audio type'}), 400
    
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/wikipedia/year/<int:year>/audio/event/<int:event_number>', methods=['GET'])
def get_event_audio(year, event_number):
    """
    Endpoint to get audio for a specific timeline event
    """
    try:
        # This is a simplified version - in practice, you might want to cache audio files
        # or store the file paths in a database/cache
        
        # For now, regenerate the audio (you should optimize this)
        wiki_response = requests.get(f'http://localhost:5000/api/wikipedia/year/{year}')
        wiki_data = wiki_response.json()
        
        audio_files = create_audio_from_wikipedia_data(wiki_data['yearSummary'])
        
        if audio_files and 'timeline_events' in audio_files:
            for event_audio in audio_files['timeline_events']:
                if event_audio['event_number'] == event_number:
                    return send_file(event_audio['file_path'], as_attachment=True,
                                   download_name=f'{year}_event_{event_number}.mp3')
        
        return jsonify({'error': 'Event audio not found'}), 404
    
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/text-to-speech', methods=['POST'])
def text_to_speech():
    """
    General endpoint to convert any text to speech
    """
    try:
        data = request.json
        text = data.get('text')
        language = data.get('language', 'en')
        slow = data.get('slow', False)
        
        if not text:
            return jsonify({'error': 'Text is required'}), 400
        
        # Create TTS
        tts = gTTS(text=text, lang=language, slow=slow)
        
        # Save to temporary file
        temp_file = tempfile.NamedTemporaryFile(delete=False, suffix='.mp3', prefix='tts_')
        tts.save(temp_file.name)
        
        return send_file(temp_file.name, as_attachment=True, download_name='speech.mp3')
    
    except Exception as e:
        return jsonify({'error': str(e)}), 500

# Cleanup function to remove temporary files (call this periodically)
def cleanup_temp_files():
    """
    Clean up temporary audio files
    """
    import glob
    temp_dir = tempfile.gettempdir()
    
    # Remove old TTS files
    for pattern in ['summary_*.mp3', 'event_*.mp3', 'combined_*.mp3', 'tts_*.mp3']:
        for file_path in glob.glob(os.path.join(temp_dir, pattern)):
            try:
                # Remove files older than 1 hour
                if os.path.getctime(file_path) < (time.time() - 3600):
                    os.unlink(file_path)
            except:
                pass

if __name__ == '__main__':
    app.run(debug=True, port=5001)  # Different port from your Wikipedia API