# 🕰️ PastPortals — Rediscover History Through AI-Generated Visual Stories 🌍

> ✨ An AI-powered mobile app that transforms historical events into **engaging, image-based videos** using Wikipedia and cutting-edge generative models.

---

![Node.js](https://img.shields.io/badge/Backend-Node.js-green?logo=node.js)
![Kotlin](https://img.shields.io/badge/Frontend-Kotlin-orange?logo=kotlin)
![Google Colab](https://img.shields.io/badge/AI-Powered%20by-Colab-yellow?logo=googlecolab)
![MIT License](https://img.shields.io/badge/License-MIT-blue.svg)

---

## 📖 What is PastPortals?

**PastPortals** is an innovative application that lets users explore any year, event, or historical figure through vivid, AI-generated visualizations.  

Simply ask:
- “What happened in 1947?”
- “Who was Nikola Tesla?”
- “Tell me about World War II.”

And the app will:
1. 📚 Fetch verified content from **Wikipedia**.
2. 🧠 Convert that into **detailed AI prompts**.
3. 🖼️ Generate **high-quality images** using state-of-the-art models.
4. 🎞️ Stitch the images into a **smooth video** via frame interpolation.

🔍 It’s history like never before — visually rich, interactive, and AI-driven.

---

## ✨ Core Features

| Feature                            | Description                                                                 |
|------------------------------------|-----------------------------------------------------------------------------|
| 🧭 Natural Query Interface          | Ask about any **year**, **event**, or **historical figure**                |
| 📚 Wikipedia-Powered Content        | Uses verified **Wikipedia data** for informative storytelling              |
| 🧠 Smart Prompt Generation          | Custom pipeline for transforming content into **image-friendly prompts**   |
| 🎨 Text-to-Image AI Generation      | Leverages **Wan2.1-T2V-1.3B-Diffusers** and similar models                 |
| 🎬 Frame Interpolation to Video     | Smoothly transitions between images for a cinematic experience             |
| 📱 Kotlin + Jetpack Compose UI      | Clean and responsive **Android frontend**                                  |
| ☁️ Google Colab GPU Integration     | Handles all heavy **AI tasks using Colab** + Python                         |

---

## 🏗️ System Architecture

```plaintext
User Query → Wikipedia API → Prompt Generator
       ↓
Text-to-Image Model (Colab GPU)
       ↓
Frame Interpolation → AI Video
       ↓
Android App Display (Jetpack Compose)
````

---

## 🔧 Tech Stack

| Layer           | Tools / Frameworks                           |
| --------------- | -------------------------------------------- |
| 📲 Frontend     | Kotlin, Jetpack Compose                      |
| 🌐 Backend      | Node.js, Express.js                          |
| 🧠 AI Models    | Wan2.1-T2V-1.3B-Diffusers, Diffusers Library |
| 📚 Data Source  | Wikipedia API                                |
| ☁️ AI Inference | Google Colab (GPU Runtime, Python)           |
| 🗃️ Storage     | Optional FAISS / Cloud (for caching vectors) |

---

## 📂 Project Structure

```bash
pastportals/
├── backend/             # Node.js API for prompt generation and video orchestration
│   └── routes/
│   └── controllers/
│   └── services/
│   └── utils/
│
├── frontend/            # Android app using Kotlin and Jetpack Compose
│   └── ui/
│   └── viewmodels/
│   └── screens/
│
├── colab_scripts/       # Google Colab notebooks for image + video generation
│   └── text_to_image.ipynb
│   └── image_to_video.ipynb
│
├── assets/              # Sample images, logos, and demo media
│   └── demo.gif
│
└── README.md            # Project documentation
```

---

## 🚀 Getting Started

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/your-username/pastportals.git
cd pastportals
```

### 2️⃣ Backend Setup (Node.js)

```bash
cd backend
npm install
npm run dev
```

> This will start the Express server on your specified port (default: 3000)

### 3️⃣ Frontend Setup (Android)

* Open the `frontend/` folder in **Android Studio**
* Connect a device/emulator
* Click **Run ▶️** to launch the app

### 4️⃣ Google Colab (AI Model Execution)

* Open the `.ipynb` notebooks under `/colab_scripts/`
* Enable **GPU Runtime** (`Runtime > Change Runtime Type > GPU`)
* Upload prompts (or trigger from backend)
* Run all cells to generate:

  * Images
  * Interpolated video

---

## 🖼️ Sample Output

> ✨ *Visual storytelling from AI-generated prompts*

<p align="center">
  <img src="assets/sample_frame_1.png" width="250"/>
  <img src="assets/sample_frame_2.png" width="250"/>
  <img src="assets/sample_frame_3.png" width="250"/>
</p>

> *Images above are automatically generated for the query “World War II”.*

---

## 🎓 Prompt Engineering Strategy

Each Wikipedia event is broken down into:

* 🌍 **Context** (time, location, scale)
* 🧑‍🤝‍🧑 **Key figures** (people, groups)
* 📜 **Descriptive action phrases** (e.g., "A war-torn battlefield at dawn")

The prompt is formatted like:

```
"A dramatic depiction of [event], showing [key figure] at [location], during [year]."
```

Which is passed to the **text-to-image model**, returning realistic or stylized outputs.

---

## 🧪 Future Enhancements

* 🎙️ Voice-based search (e.g., “Tell me what happened in 1857”)
* 🧬 Style transfer (e.g., Pixar, Realistic, Anime versions)
* 🌐 Web version of PastPortals
* 🧠 Model fine-tuning on historical datasets

---

## 🤝 Contributing

Contributions are welcome!

```bash
1. Fork the repo
2. Create your feature branch (git checkout -b feat/history-timeline)
3. Commit changes (git commit -am 'Add cool feature')
4. Push (git push origin feat/history-timeline)
5. Open a Pull Request
```

---

## 📄 License

This project is released under the [MIT License](LICENSE).

---

## 🙌 Credits

* 📚 Wikipedia API — historical data source
* 🧠 Hugging Face — Wan2.1-T2V-1.3B-Diffusers
* 💻 Google Colab — GPU infrastructure
* 💬 Created by [Jasvanth Varma Muppala]

---

> *"History is not just written — it's reimagined with AI."*

````

---
