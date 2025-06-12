package io.powerrangers.backend.exception

class AuthTokenException(val errorCode: ErrorCode) : RuntimeException()
