package io.powerrangers.backend.dto

import jakarta.validation.constraints.NotBlank


data class CommentUpdateRequestDto (
    @field:NotBlank(message = "내용을 입력해주세요.")
    val content: String
)
