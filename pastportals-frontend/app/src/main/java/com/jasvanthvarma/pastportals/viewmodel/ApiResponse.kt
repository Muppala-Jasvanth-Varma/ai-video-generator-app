// ApiResponse.kt
package com.jasvanthvarma.pastportals.viewmodel
// ApiResponse.kt
data class TimelineEvent(
    val title: String,
    val date: String,
)

data class YearSummary(
    val year: String,
    val paragraph: String,
    val timeline: List<TimelineEvent>,
    val image_prompts: List<String>
)

data class ApiResponse(
    val success: Boolean,
    val message: String? = null,  // <-- Add this
    val yearSummary: YearSummary
)

