package io.powerrangers.backend.dto

import jakarta.validation.constraints.NotBlank

data class UserUpdateProfileRequestDto(

    @field:NotBlank(message = "닉네임을 지정해주세요.")
    val nickname:  String,

    val intro: String? = null,

    val profileImage: String? = null

)
