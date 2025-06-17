package io.powerrangers.backend.dto

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

sealed class BaseResponse<T>(
    open val status: Int,
) {
    data class Success<T>(
        override val status: Int,
        val data: T?,
    ) : BaseResponse<T>(status)

    data class Error<T>(
        override val status: Int,
        val message: String
    ) : BaseResponse<T>(status)

    companion object {
        fun <T> success(status: HttpStatus, data: T? = null): ResponseEntity<Success<T>> {
            return ResponseEntity.status(status)
                .body(Success(status.value(), data))
        }

        fun <T> error(message: String, status: HttpStatus): ResponseEntity<Error<T>> {
            return ResponseEntity.status(status)
                .body(Error(status.value(), message))
        }
    }
}