// ✅ aiVideoRoutes.js (Place inside /routes/aiVideoRoutes.js)

const express = require('express');
const router = express.Router();
const axios = require('axios');
const { v4: uuidv4 } = require('uuid');
const fs = require('fs');
const path = require('path');

// Temp job tracking store (replace with DB in production)
const jobs = {};

// POST /api/ai-video/generate
router.post('/generate', async (req, res) => {
  const { prompt } = req.body;

  if (!prompt) {
    return res.status(400).json({ success: false, error: 'Prompt is required' });
  }

  const jobId = uuidv4();
  jobs[jobId] = { status: 'in_progress', videoPath: null };

  try {
    // ✅ Call your Colab ngrok URL
    const colabApiUrl = 'https://xxxx.ngrok.io/generate';
    const colabRes = await axios.post(colabApiUrl, { prompt });

    if (!colabRes.data || !colabRes.data.video_path) {
      throw new Error('No video returned from Colab');
    }

    const videoUrl = colabRes.data.video_path;
    jobs[jobId] = { status: 'done', videoPath: videoUrl };

    res.json({ success: true, jobId, apiJobId: jobId });
  } catch (err) {
    jobs[jobId] = { status: 'failed', error: err.message };
    res.status(500).json({ success: false, error: err.message });
  }
});

// GET /api/ai-video/status/:jobId
router.get('/status/:jobId', (req, res) => {
  const { jobId } = req.params;
  if (!jobs[jobId]) {
    return res.status(404).json({ success: false, error: 'Job not found' });
  }

  res.json({ success: true, status: jobs[jobId].status });
});

// GET /api/ai-video/download/:jobId
router.get('/download/:jobId', (req, res) => {
  const { jobId } = req.params;
  const job = jobs[jobId];

  if (!job || job.status !== 'done') {
    return res.status(404).json({ success: false, error: 'Video not ready or job not found' });
  }

  // Send path returned from Colab (can be local Colab Drive, public link, etc.)
  res.json({ success: true, videoPath: job.videoPath });
});

module.exports = router;