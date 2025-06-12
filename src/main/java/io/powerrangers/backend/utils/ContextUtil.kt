package io.powerrangers.backend.utils

import io.powerrangers.backend.dto.UserDetails
import io.powerrangers.backend.exception.AuthTokenException
import io.powerrangers.backend.exception.ErrorCode
import org.springframework.security.core.context.SecurityContextHolder

fun getCurrentUserId(): Long {
    val authentication = SecurityContextHolder.getContext().authentication
    if (authentication == null || !authentication.isAuthenticated) {
        throw AuthTokenException(ErrorCode.UNAUTHORIZED)
    }

    val principal = authentication.principal as UserDetails
    return principal.id!!
}
