const { spawn } = require('child_process');

/**
 * Generates speech audio from the provided year summary object.
 * @returns {Promise<{ success: boolean, audioPath?: string, error?: string }>}
 */
const generateAudio = (yearSummary) => {
  return new Promise((resolve, reject) => {
    try {
      const python = spawn('python', ['scripts/generate_speech.py']);

      let output = '';
      let errorOutput = '';

      // Collect stdout
      python.stdout.on('data', (data) => {
        output += data.toString();
      });

      // Collect stderr
      python.stderr.on('data', (data) => {
        errorOutput += data.toString();
      });

      // Handle process close
      python.on('close', (code) => {
        if (code !== 0) {
          return resolve({
            success: false,
            error: `Python script failed with code ${code}: ${errorOutput.trim()}`
          });
        }

        try {
          const result = JSON.parse(output);
          if (result.success) {
            resolve({ success: true, audioPath: result.audio_path });
          } else {
            resolve({ success: false, error: result.error });
          }
        } catch (err) {
          resolve({ success: false, error: 'Invalid JSON from Python script' });
        }
      });

      // Send yearSummary as JSON input to Python
      python.stdin.write(JSON.stringify(yearSummary));
      python.stdin.end();
    } catch (err) {
      reject({ success: false, error: 'Exception occurred while generating audio', details: err });
    }
  });
};

module.exports = generateAudio;
