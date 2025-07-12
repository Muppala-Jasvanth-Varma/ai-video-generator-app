// controllers/textToSpeechController.js
const gtts = require('gtts');
const fs = require('fs');
const path = require('path');
const axios = require('axios');

class TextToSpeechController {
  constructor() {
    // Create audio directory if it doesn't exist
    this.audioDir = path.join(__dirname, '..', 'audio');
    if (!fs.existsSync(this.audioDir)) {
      fs.mkdirSync(this.audioDir, { recursive: true });
    }
  }

  // Helper function to create audio from text
  createAudioFromText(text, filename, language = 'en', slow = false) {
    return new Promise((resolve, reject) => {
      const speech = new gtts(text, language, slow);
      const filePath = path.join(this.audioDir, filename);
      
      speech.save(filePath, (err, result) => {
        if (err) {
          console.error('❌ TTS Error:', err);
          reject(err);
        } else {
          console.log(`✅ Audio saved: ${filename}`);
          resolve(filePath);
        }
      });
    });
  }

  // Generate audio from Wikipedia data
  async generateWikipediaAudio(req, res) {
    try {
      const { year, language = 'en', slow = false, audioType = 'combined' } = req.body;
      
      if (!year) {
        return res.status(400).json({
          success: false,
          error: "Year is required"
        });
      }

      // Get Wikipedia data from existing endpoint
      const wikiResponse = await axios.get(`http://localhost:5000/api/wikipedia/year/${year}`);
      
      if (!wikiResponse.data.success) {
        return res.status(404).json({
          success: false,
          error: "Wikipedia data not found"
        });
      }

      const yearData = wikiResponse.data.yearSummary;
      const timestamp = Date.now();
      let audioFiles = {};

      // Generate summary audio
      if (yearData.paragraph && (audioType === 'summary' || audioType === 'combined')) {
        const summaryText = `Year ${yearData.year} summary: ${yearData.paragraph}`;
        const summaryFilename = `${year}_summary_${timestamp}.mp3`;
        const summaryPath = await this.createAudioFromText(summaryText, summaryFilename, language, slow);
        audioFiles.summary = {
          filename: summaryFilename,
          url: `/audio/${summaryFilename}`,
          path: summaryPath
        };
      }

      // Generate timeline events audio
      if (yearData.timeline && yearData.timeline.length > 0 && (audioType === 'timeline' || audioType === 'combined')) {
        audioFiles.timeline = [];
        
        for (let i = 0; i < yearData.timeline.length; i++) {
          const event = yearData.timeline[i];
          const eventText = `Event ${i + 1}: ${event.title}. Impact: ${event.impact}`;
          const eventFilename = `${year}_event_${i + 1}_${timestamp}.mp3`;
          const eventPath = await this.createAudioFromText(eventText, eventFilename, language, slow);
          
          audioFiles.timeline.push({
            eventNumber: i + 1,
            title: event.title,
            filename: eventFilename,
            url: `/audio/${eventFilename}`,
            path: eventPath
          });
        }
      }

      // Generate combined audio
      if (audioType === 'combined') {
        let combinedText = `Historical overview for the year ${yearData.year}. `;
        
        if (yearData.paragraph) {
          combinedText += `Summary: ${yearData.paragraph} `;
        }
        
        if (yearData.timeline && yearData.timeline.length > 0) {
          combinedText += "Major events: ";
          yearData.timeline.forEach((event, index) => {
            combinedText += `Event ${index + 1}: ${event.title}. ${event.impact}. `;
          });
        }
        
        const combinedFilename = `${year}_combined_${timestamp}.mp3`;
        const combinedPath = await this.createAudioFromText(combinedText, combinedFilename, language, slow);
        audioFiles.combined = {
          filename: combinedFilename,
          url: `/audio/${combinedFilename}`,
          path: combinedPath
        };
      }

      res.json({
        success: true,
        year: parseInt(year),
        audioType,
        language,
        slow,
        audioFiles,
        message: "Audio generated successfully"
      });

    } catch (error) {
      console.error("❌ Audio generation error:", error.message);
      res.status(500).json({
        success: false,
        error: error.message
      });
    }
  }

  // General text-to-speech
  async convertTextToSpeech(req, res) {
    try {
      const { text, language = 'en', slow = false, filename } = req.body;
      
      if (!text) {
        return res.status(400).json({
          success: false,
          error: "Text is required"
        });
      }

      const timestamp = Date.now();
      const audioFilename = filename || `tts_${timestamp}.mp3`;
      const audioPath = await this.createAudioFromText(text, audioFilename, language, slow);

      res.json({
        success: true,
        filename: audioFilename,
        url: `/audio/${audioFilename}`,
        path: audioPath,
        message: "Text converted to speech successfully"
      });

    } catch (error) {
      console.error("❌ TTS error:", error.message);
      res.status(500).json({
        success: false,
        error: error.message
      });
    }
  }

  // Get audio file info
  getAudioFileInfo(req, res) {
    try {
      const filename = req.params.filename;
      const filePath = path.join(this.audioDir, filename);
      
      if (!fs.existsSync(filePath)) {
        return res.status(404).json({
          success: false,
          error: "Audio file not found"
        });
      }

      const stats = fs.statSync(filePath);
      
      res.json({
        success: true,
        filename,
        size: stats.size,
        created: stats.birthtime,
        url: `/audio/${filename}`
      });

    } catch (error) {
      res.status(500).json({
        success: false,
        error: error.message
      });
    }
  }

  // Clean up old audio files
  async cleanupOldFiles(req, res) {
    try {
      const maxAge = req.query.maxAge || 24; // hours
      const maxAgeMs = maxAge * 60 * 60 * 1000;
      const now = Date.now();
      
      const files = fs.readdirSync(this.audioDir);
      let deletedFiles = 0;
      
      for (const file of files) {
        const filePath = path.join(this.audioDir, file);
        const stats = fs.statSync(filePath);
        
        if (now - stats.birthtime.getTime() > maxAgeMs) {
          fs.unlinkSync(filePath);
          deletedFiles++;
        }
      }
      
      res.json({
        success: true,
        deletedFiles,
        message: `Cleaned up ${deletedFiles} old audio files`
      });
      
    } catch (error) {
      res.status(500).json({
        success: false,
        error: error.message
      });
    }
  }

  // Generate audio for video (helper method)
  async generateAudioForVideo(year, language = 'en') {
    try {
      console.log("🎵 Generating audio for year:", year);
      
      // Get Wikipedia data
      const wikiResponse = await axios.get(`http://localhost:5000/api/wikipedia/year/${year}`);
      if (!wikiResponse.data.success) {
        throw new Error("Wikipedia data not found");
      }

      const yearData = wikiResponse.data.yearSummary;
      
      // Create combined text for audio
      let combinedText = `Historical overview for the year ${yearData.year}. `;
      if (yearData.paragraph) {
        combinedText += `Summary: ${yearData.paragraph} `;
      }
      if (yearData.timeline && yearData.timeline.length > 0) {
        combinedText += "Major events: ";
        yearData.timeline.forEach((event, index) => {
          combinedText += `Event ${index + 1}: ${event.title}. ${event.impact}. `;
        });
      }
      
      // Generate audio
      const timestamp = Date.now();
      const audioFilename = `video_${year}_${timestamp}.mp3`;
      const audioPath = await this.createAudioFromText(combinedText, audioFilename, language);
      
      console.log("✅ Audio generated:", audioFilename);
      return audioPath;

    } catch (error) {
      console.error("❌ Video audio generation error:", error.message);
      throw error;
    }
  }

  // List all audio files
  listAudioFiles(req, res) {
    try {
      const files = fs.readdirSync(this.audioDir);
      const audioFiles = files
        .filter(file => file.endsWith('.mp3'))
        .map(file => {
          const filePath = path.join(this.audioDir, file);
          const stats = fs.statSync(filePath);
          return {
            filename: file,
            size: stats.size,
            created: stats.birthtime,
            url: `/audio/${file}`
          };
        })
        .sort((a, b) => new Date(b.created) - new Date(a.created));

      res.json({
        success: true,
        count: audioFiles.length,
        files: audioFiles
      });

    } catch (error) {
      res.status(500).json({
        success: false,
        error: error.message
      });
    }
  }
}

module.exports = new TextToSpeechController();