package io.powerrangers.backend.dto

import jakarta.validation.constraints.*
import java.time.LocalDateTime

data class TaskCreateRequestDto(

    @field:NotBlank(message = "카테고리를 지정하지 않았습니다.")
    val category: String,

    @field:NotBlank(message = "내용을 지정하지 않았습니다.")
    val content: String,

    @field:NotNull(message = "기한을 지정하지 않았습니다.")
    @field:Future(message = "기한은 미래로 지정해야 합니다.")
    val dueDate: LocalDateTime,

    @field:NotNull(message = "상태를 지정하지 않았습니다.")
    val status: TaskStatus,

    val taskImage: String? = null,

    @field:NotNull(message = "공개 범위를 지정하지 않았습니다.")
    val scope: TaskScope
)
