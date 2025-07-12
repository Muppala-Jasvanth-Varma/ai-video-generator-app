const edgeTTS = require('edge-tts');
const fs = require('fs').promises;

exports.generateVoiceover = async (text, outputFile = 'voice.mp3') => {
  try {
    await edgeTTS.speak(text, 'en-US-ChristopherNeural', outputFile);
    return outputFile;
  } catch (error) {
    throw new Error(`TTS Generation Error: ${error.message}`);
  }
};