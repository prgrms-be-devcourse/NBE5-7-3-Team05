package io.powerrangers.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class TaskUpdateRequestDto(
    @field:NotBlank
    val category: String,

    @field:NotBlank
    val content: String,

    @field:NotNull
    val scope: TaskScope
)