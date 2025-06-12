package io.powerrangers.backend.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import io.powerrangers.backend.dto.BaseResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestCookieException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.IOException

private val log = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CustomException::class)
    protected fun handleCustomException(e: CustomException): ResponseEntity<BaseResponse<Void>> {
        return BaseResponse.error(e.errorCode.message, e.errorCode.status)
    }

    @ExceptionHandler(AuthTokenException::class)
    protected fun handleAuthTokenException(e: AuthTokenException): ResponseEntity<BaseResponse<Void>> {
        return BaseResponse.error(e.errorCode.message, e.errorCode.status)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<BaseResponse<Void>> {
        val fieldErrors = e.bindingResult.fieldErrors

        val errorMessage = fieldErrors.map {
            "${it.field} : ${it.defaultMessage}"
        }.joinToString(", ")

        val errorCode = ErrorCode.INVALID_REQUEST

        return BaseResponse.error(errorMessage, errorCode.status)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    protected fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException?): ResponseEntity<BaseResponse<Void>> {
        return BaseResponse.error(ErrorCode.INVALID_REQUEST.message, ErrorCode.INVALID_REQUEST.status)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    protected fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException?): ResponseEntity<BaseResponse<Void>> {
        return BaseResponse.error(ErrorCode.INVALID_REQUEST.message, ErrorCode.INVALID_REQUEST.status)
    }

    @ExceptionHandler(MissingRequestCookieException::class)
    protected fun handleMissingRequestCookieException(e: MissingRequestCookieException): ResponseEntity<BaseResponse<Void>> {
        log.warn { "[인증 실패] 토큰 쿠키가 존재하지 않음. 원인: ${e.message}" }
        return BaseResponse.error(ErrorCode.UNAUTHORIZED.message, ErrorCode.UNAUTHORIZED.status)
    }

    @ExceptionHandler(IOException::class)
    protected fun handleIOException(e: IOException): ResponseEntity<BaseResponse<Void>> {
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        log.error(e) { "Unhandled I/O Exception occurred: ${e.message}" }
        return BaseResponse.error(errorCode.message, errorCode.status)
    }

    @ExceptionHandler(Exception::class)
    protected fun handleException(e: Exception): ResponseEntity<BaseResponse<Void>> {
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        log.error(e) { "Unhandled Exception occurred: ${e.message}" }
        return BaseResponse.error(errorCode.message, errorCode.status)
    }
}
