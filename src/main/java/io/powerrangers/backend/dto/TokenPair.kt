package io.powerrangers.backend.dto

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter

class TokenPair(
    val accessToken: String,
    val refreshToken: String
)
