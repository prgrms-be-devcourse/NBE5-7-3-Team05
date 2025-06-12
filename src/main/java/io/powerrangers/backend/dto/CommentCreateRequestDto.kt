package io.powerrangers.backend.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull


data class CommentCreateRequestDto (

    @field:NotNull(message = "할 일이 존재하지 않습니다.")
    val taskId:  Long,

    val parentId: Long?,

    @field:NotBlank(message = "내용을 입력해주세요")
    val content: String
    )