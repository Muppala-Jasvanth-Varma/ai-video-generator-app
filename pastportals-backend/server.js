const express = require("express");
const mongoose = require("mongoose");
const dotenv = require("dotenv");
const cors = require("cors");
const jwt = require("jsonwebtoken");
const axios = require("axios");
const bcrypt = require("bcryptjs");
const ffmpeg = require("fluent-ffmpeg");
const fs = require('fs');
const path = require('path');
const rateLimit = require('express-rate-limit');

require("dotenv").config();

// Models and Routes
const User = require("./models/userModel");
const Login = require("./models/loginModel");
const userRoutes = require("./routes/userRoutes");
const wikipediaRoutes = require("./routes/wikipediaRoutes");

// ✅ Import TTS Controller
const ttsController = require('./controllers/textToSpeechController');

// Replicate setup
dotenv.config();



const app = express();

// Middlewares
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));
app.use(cors());
app.use(express.static('public'));
app.use('/audio', express.static('audio')); // ✅ Serve audio files

// MongoDB Connection
mongoose.connect(process.env.MONGO_URI, {
  useNewUrlParser: true,
  useUnifiedTopology: true
}).then(() => console.log("✅ MongoDB connected"))
  .catch(err => console.error("❌ MongoDB connection error:", err));

// Request logging middleware
app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.path}`);
    next();
});

// Rate limiting for video generation
const videoLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 10, // limit each IP to 10 requests per windowMs
    message: {
        success: false,
        error: 'Too many video generation requests, please try again later'
    }
});

// Existing Routes
app.use("/api/users", userRoutes);
app.use("/api/wikipedia", wikipediaRoutes);

// ✅ TTS Routes
app.post("/api/wikipedia/generate-audio", ttsController.generateWikipediaAudio.bind(ttsController));
app.post("/api/text-to-speech", ttsController.convertTextToSpeech.bind(ttsController));
app.get("/api/audio/:filename", ttsController.getAudioFileInfo.bind(ttsController));
app.get("/api/audio", ttsController.listAudioFiles.bind(ttsController));
app.delete("/api/audio/cleanup", ttsController.cleanupOldFiles.bind(ttsController));



const { generatePromptFromWikipediaText } = require("./services/geminiService");
app.post("/api/gemini/generate-prompt", async (req, res) => {
  const { wikipediaText } = req.body;

  if (!wikipediaText || wikipediaText.trim().length === 0) {
    return res.status(400).json({ success: false, error: "Wikipedia text is required" });
  }

  try {
    const prompt = await generatePromptFromWikipediaText(wikipediaText);
    res.json({ success: true, prompt });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});



// ✅ EXISTING: Enhanced video generation with audio (Replicate + FFmpeg)
app.post("/api/generate-video", async (req, res) => {
  try {
    const { email, topic, year, includeAudio = true } = req.body;
        
    // Validate input
    if (!email || !topic) {
      return res.status(400).json({ 
        success: false,
        error: "Email and topic are required"
      });
    }

    const user = await User.findOne({ email });
    if (!user) {
      return res.status(404).json({ 
        success: false,
        error: "User not found"
      });
    }

    let audioPath = null;

    // ✅ Generate audio if year is provided and includeAudio is true
    if (year && includeAudio) {
      try {
        audioPath = await ttsController.generateAudioForVideo(year);
      } catch (audioError) {
        console.warn("⚠️ Audio generation failed, continuing without audio:", audioError.message);
      }
    }
            
    // 2. Create video with or without audio
    console.log("🎬 Creating video...");
    const timestamp = Date.now();
    const outputFilename = `video_${timestamp}.mp4`;
    const outputPath = path.join(__dirname, 'public', outputFilename);

    await new Promise((resolve, reject) => {
      const ffmpegCommand = ffmpeg()
        .inputFPS(1/5)
        .addInput(images);

      // Add audio if available
      if (audioPath && fs.existsSync(audioPath)) {
        ffmpegCommand.input(audioPath);
      }

      ffmpegCommand
        .save(outputPath)
        .on("end", () => {
          console.log("✅ Video created successfully");
          resolve();
        })
        .on("error", (err) => {
          console.error("❌ FFmpeg error:", err);
          reject(err);
        });
    });

    const videoRoutes = require('./routes/video');
    app.use('/api/video', videoRoutes);

    
    // 3. Save video info to database
    const videoRecord = new Video({
      user: user._id,
      topic,
      year: year || null,
      filename: outputFilename,
      audioIncluded: !!audioPath,
      createdAt: new Date()
    });
    await videoRecord.save();

    res.json({ 
      success: true,
      videoUrl: `/${outputFilename}`,
      videoId: videoRecord._id,
      audioIncluded: !!audioPath,
      message: "Video generated successfully"
    });

  } catch (error) {
    console.error("❌ Video generation error:", error.message);
    res.status(500).json({ 
      success: false,
      error: error.message
    });
  }
});


// Add this to your existing server.js file

// Import the new AI video routes (add this near your other route imports)
const aiVideoRoutes = require("./routes/aiVideoRoutes");

// Add this route in your existing routes section (after your other app.use statements)
app.use("/api/ai-video", aiVideoRoutes);

// Update your existing video generation endpoint to use the new AI video service
app.post("/api/generate-video-with-ai", async (req, res) => {
  try {
    const { email, prompt, duration = 5, aspectRatio = '16:9', includeAudio = true } = req.body;
        
    // Validate input
    if (!email || !prompt) {
      return res.status(400).json({ 
        success: false,
        error: "Email and prompt are required"
      });
    }

    // Check if user exists
    const user = await User.findOne({ email });
    if (!user) {
      return res.status(404).json({ 
        success: false,
        error: "User not found"
      });
    }

    // Forward request to AI video service
    const videoResponse = await axios.post(`http://localhost:${PORT}/api/ai-video/generate`, {
      prompt,
      email,
      duration,
      aspectRatio,
      style: 'realistic'
    });

    if (videoResponse.data.success) {
      res.json({
        success: true,
        jobId: videoResponse.data.jobId,
        apiJobId: videoResponse.data.apiJobId,
        status: 'generating',
        message: 'Video generation started successfully',
        statusCheckUrl: `/api/ai-video/status/${videoResponse.data.jobId}`,
        estimatedTime: '2-5 minutes'
      });
    } else {
      throw new Error('Failed to start video generation');
    }

  } catch (error) {
    console.error("❌ Video generation error:", error.message);
    res.status(500).json({ 
      success: false,
      error: error.response?.data?.error || error.message
    });
  }
});

// Add rate limiting to the new AI video routes (add this after your existing rate limiters)
const aiVideoLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 5, // limit each IP to 5 video generations per 15 minutes
    message: {
        success: false,
        error: 'Too many video generation requests, please try again later'
    }
});

// Apply rate limiting to AI video routes
app.use("/api/ai-video/generate", aiVideoLimiter);

// Update your health check to include AI video service
app.get('/health', (req, res) => {
    res.json({ 
        success: true, 
        message: 'Server is running',
        timestamp: new Date().toISOString(),
        services: {
            aiVideo: !!process.env.VIDEO_API_KEY,

            mongodb: mongoose.connection.readyState === 1
        }
    });
});

// Update your root endpoint to include AI video endpoints
app.get('/', (req, res) => {
    res.json({ 
        success: true,
        message: 'Pastportals API is running!',
        timestamp: new Date().toISOString(),
        features: ["Wikipedia API", "Text-to-Speech", "AI Video Generation", "RapidAPI Video"],
        endpoints: {
            aiVideoGenerate: 'POST /api/ai-video/generate',
            aiVideoStatus: 'GET /api/ai-video/status/:jobId',
            aiVideoDownload: 'GET /api/ai-video/download/:jobId',
            combinedGenerate: 'POST /api/generate-video-with-ai',
            health: 'GET /health'
        }
    });
});



// Health check endpoint
app.get('/health', (req, res) => {
    res.json({ 
        success: true, 
        message: 'Server is running',
        timestamp: new Date().toISOString(),
        services: {
            mongodb: mongoose.connection.readyState === 1
        }
    });
});

// Root endpoint
app.get('/', (req, res) => {
    res.json({ 
        success: true,
        message: 'Pastportals API is running!',
        timestamp: new Date().toISOString(),
        features: ["Wikipedia API", "Text-to-Speech", "Video Generation", "RapidAPI Video"],
        endpoints: {

            health: 'GET /health'
        }
    });
});

// 404 handler
app.use('*', (req, res) => {
    res.status(404).json({
        success: false,
        error: 'Endpoint not found'
    });
});

// Global error handler
app.use((error, req, res, next) => {
    console.error('❌ Global error:', error);
    res.status(500).json({
        success: false,
        error: 'Internal server error'
    });
});

// Server Listen
const PORT = process.env.PORT || 5000;
app.listen(PORT, "0.0.0.0", () => {
  console.log(`🚀 Server running on http://0.0.0.0:${PORT}`);
  console.log(`🌐 Local access: http://localhost:${PORT}`);
  console.log(`📱 Network access: http://192.168.68.184:${PORT}`);
  console.log(`🎵 Audio files served from: /audio/`);
});