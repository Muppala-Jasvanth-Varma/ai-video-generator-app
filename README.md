PastPortals — AI-Based Historical Visualization Platform
Overview

PastPortals is an AI-powered application that converts historical information into structured visual narratives. It enables users to explore historical events, timelines, and figures through automatically generated images and videos using verified data and generative AI models.

The system accepts natural language queries such as:

What happened in 1947
Who was Nikola Tesla
Explain World War II

It processes these inputs and generates a coherent visual representation of the requested topic.

Features
Natural language query interface for historical exploration
Integration with Wikipedia API for reliable data retrieval
Automated prompt generation pipeline
Text-to-image generation using diffusion models
Image-to-video conversion via frame interpolation
Android frontend built with Kotlin and Jetpack Compose
GPU-based AI processing using Google Colab
System Architecture
User Query
   ↓
Wikipedia API
   ↓
Prompt Generator
   ↓
Text-to-Image Model (GPU)
   ↓
Frame Interpolation
   ↓
Video Output
   ↓
Android Application
Tech Stack
Frontend
Kotlin
Jetpack Compose
Backend
Node.js
Express.js
AI and Processing
Diffusers Library
Wan2.1-T2V-1.3B
Data Source
Wikipedia API
Infrastructure
Google Colab (GPU Runtime)
Optional
FAISS or cloud-based storage for caching
Project Structure
pastportals/
├── backend/
│   ├── routes/
│   ├── controllers/
│   ├── services/
│   └── utils/
│
├── frontend/
│   ├── ui/
│   ├── viewmodels/
│   └── screens/
│
├── colab_scripts/
│   ├── text_to_image.ipynb
│   └── image_to_video.ipynb
│
├── assets/
│   └── demo files
│
└── README.md
Getting Started
1. Clone Repository
git clone https://github.com/your-username/pastportals.git
cd pastportals
2. Backend Setup
cd backend
npm install
npm run dev
3. Frontend Setup
Open the frontend/ folder in Android Studio
Connect an emulator or physical device
Run the application
4. AI Model Execution
Open the notebooks inside colab_scripts/
Enable GPU runtime in Google Colab
Run all cells to generate images and videos
Prompt Engineering

The system converts historical content into structured prompts using:

Context (time, location, scale)
Key entities (people or groups)
Descriptive actions and environment

These prompts are optimized for diffusion-based image generation models to ensure meaningful visual output.

Future Enhancements
Voice-based query input
Multiple visual styles (realistic, animated, stylized)
Web-based platform deployment
Fine-tuned models on domain-specific datasets
Contributing
Fork the repository
Create a new branch
Commit your changes
Push to your branch
Submit a pull request
License

This project is licensed under the MIT License.

Acknowledgements
Wikipedia API for historical data
Hugging Face for model ecosystem
Google Colab for GPU infrastructure
