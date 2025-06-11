package io.powerrangers.backend.exception

import org.springframework.http.HttpStatus

enum class ErrorCode (
    val status: HttpStatus,
    val message: String
) {
    // 400 Bad Request
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 데이터가 올바르지 않습니다."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),

    // 403 Forbidden
    NOT_THE_OWNER(HttpStatus.FORBIDDEN, "해당 작업에 대한 권한이 없습니다."),
    NOT_ALLOWED(HttpStatus.FORBIDDEN, "해당 리소스에 대한 접근 권한이 없습니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 할 일입니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "팔로우 관계를 찾을 수 없습니다."),

    // 409 Conflict
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    ALREADY_FOLLOWED(HttpStatus.CONFLICT, "이미 팔로우한 사용자입니다."),

    // 415 Unsupported Media Type
    UNSUPPORTED_RESOURCE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 파일 형식입니다. 이미지 파일만 업로드해주세요."),

    // 500 INTERNAL_SERVER ERROR
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러입니다. 서버 팀에게 문의해주세요.");

}
