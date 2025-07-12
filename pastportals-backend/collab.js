const axios = require("axios");

async function sendPrompt(prompt) {
  try {
    const response = await axios.post("https://https://colab.research.google.com/drive/1GOMOS-QgVfeGD559JdhjUQmWArOlhqlt?usp=sharing/generate", {
      prompt: prompt
    });
    console.log("Video generated at:", response.data.video_path);
  } catch (err) {
    console.error("Error:", err.message);
  }
}

sendPrompt("A futuristic city with flying cars and neon lights at night");


