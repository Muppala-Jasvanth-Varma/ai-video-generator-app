package com.jasvanthvarma.pastportals

// Data classes for TTS functionality
data class AudioRequest(
    val year: Int,
    val language: String = "en",
    val slow: Boolean = false,
    val audioType: String = "combined" // "summary", "timeline", or "combined"
)

data class TtsRequest(
    val text: String,
    val language: String = "en",
    val slow: Boolean = false,
    val filename: String? = null
)

data class AudioResponse(
    val success: Boolean,
    val year: Int? = null,
    val audioType: String? = null,
    val language: String? = null,
    val slow: Boolean? = null,
    val audioFiles: AudioFiles? = null,
    val filename: String? = null,
    val url: String? = null,
    val message: String? = null,
    val error: String? = null
)

data class AudioFiles(
    val summary: AudioFile? = null,
    val combined: AudioFile? = null,
    val timeline: List<TimelineAudio>? = null
)

data class AudioFile(
    val filename: String,
    val url: String,
    val path: String
)

data class TimelineAudio(
    val eventNumber: Int,
    val title: String,
    val filename: String,
    val url: String,
    val path: String
)

data class AudioListResponse(
    val success: Boolean,
    val count: Int,
    val files: List<AudioFileInfo>
)

data class AudioFileInfo(
    val filename: String,
    val size: Long,
    val created: String,
    val url: String
)