const ffmpeg = require("fluent-ffmpeg");
const path = require("path");

/**
 * Generate a video from a directory of sequentially named images (e.g., 1.png, 2.png...).
 * @param {string} imageDir - Directory containing generated images.
 * @returns {Promise<string>} - Path to the generated video file.
 */
const generateVideo = async (imageDir) => {
    const outputPath = path.join(__dirname, "generated_videos", `video_${Date.now()}.mp4`);

    return new Promise((resolve, reject) => {
        ffmpeg()
            .input(`${imageDir}/%d.png`) // expects 1.png, 2.png, etc.
            .inputFPS(1) // 1 image per second (adjustable)
            .videoCodec("libx264")
            .outputOptions(["-pix_fmt yuv420p"])
            .output(outputPath)
            .on("end", () => {
                console.log("🎥 Video successfully generated:", outputPath);
                resolve(outputPath);
            })
            .on("error", (err) => {
                console.error("❌ FFmpeg error:", err.message);
                reject(err);
            })
            .run();
    });
};

module.exports = generateVideo;
