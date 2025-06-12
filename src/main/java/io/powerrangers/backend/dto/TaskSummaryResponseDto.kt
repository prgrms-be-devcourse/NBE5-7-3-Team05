package io.powerrangers.backend.dto

data class TaskSummaryResponseDto(
    val year: Int,
    val month: Int,
    val dailySummaries: List<DailySummary>
) {
    data class DailySummary(
        val date: String,
        val taskCount: Int
    )
}
