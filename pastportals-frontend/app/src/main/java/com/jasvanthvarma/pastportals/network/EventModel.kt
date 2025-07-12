package com.jasvanthvarma.pastportals.network

// 🌟 Response for /day/{month}/{day}
data class DayEventsResponse(
    val success: Boolean,
    val events: List<DayEvent>
)

data class DayEvent(
    val title: String,
    val year: Int,
    val pages: List<String>
)

// 🌟 Response for /query/{query}
data class EventDetailsResponse(
    val success: Boolean,
    val eventData: EventData
)

data class EventData(
    val title: String,
    val description: String,
    val pageUrl: String?,
    val thumbnail: String?,
    val image_prompt: String
)
