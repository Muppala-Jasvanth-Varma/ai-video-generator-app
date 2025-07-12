const express = require('express');
const router = express.Router();
const axios = require('axios');

router.get('/generate-image', async (req, res) => {
  try {
    const { prompt } = req.query;

    if (!prompt) {
      return res.status(400).json({ error: 'Prompt is required' });
    }

    // Pollinations API URL
    const imageUrl = `https://image.pollinations.ai/prompt/${encodeURIComponent(prompt)}`;

    // Optional: You can directly return this URL
    res.json({ imageUrl });

    // Optional: Or if you want to download the image buffer and serve it:
    // const response = await axios.get(imageUrl, { responseType: 'arraybuffer' });
    // res.setHeader('Content-Type', 'image/jpeg');
    // res.send(response.data);

  } catch (error) {
    console.error('Image generation failed:', error.message);
    res.status(500).json({ error: 'Failed to generate image' });
  }
});

module.exports = router;
