const { GoogleGenerativeAI } = require("@google/generative-ai");
const dotenv = require("dotenv");

dotenv.config();

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);
const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

// 🕒 Delay utility (ms)
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

async function generatePromptFromWikipediaText(wikiText, retries = 3) {
  for (let attempt = 1; attempt <= retries; attempt++) {
    try {
      const result = await model.generateContent({
        contents: [
          {
            role: "user",
            parts: [
              {
                text: `Turn this historical Wikipedia summary into a visual scene prompt for video generation:\n\n${wikiText}`
              }
            ]
          }
        ]
      });

      const response = await result.response;
      return response.text(); // ✅ Final Prompt

    } catch (err) {
      const status = err?.status || err?.response?.status;
      const isServiceUnavailable = status === 503;

      console.warn(`⚠️ Gemini API attempt ${attempt} failed. Status: ${status || "unknown"}`);

      if (isServiceUnavailable && attempt < retries) {
        const waitTime = 2000 * attempt; // exponential: 2s, 4s, 6s
        console.log(`🔁 Retrying in ${waitTime / 1000}s...`);
        await delay(waitTime);
      } else {
        console.error("❌ Gemini API error:", err);
        throw new Error("Gemini API is temporarily unavailable. Please try again later.");
      }
    }
  }
}

module.exports = {
  generatePromptFromWikipediaText
};
