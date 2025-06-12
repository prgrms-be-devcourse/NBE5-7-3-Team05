package io.powerrangers.backend.dto

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/***
 * ToDo: 에러메시지는 반환, 성공메시지를 삭제하기위해선 두가지 방법이 있어보인다.
 * 1. message필드를 제외한 성공 리스폰스와, 실패 리스폰스를 따로 관리.
 * 2. 필드를 유지하고 통합하여 관리하지만 성공 시 message null값 처리.
 * 일단은 2번이 더 적합하다고 판단하여 진행하였습니다!
 */
data class BaseResponse<T> (
    val status : Int,
    val message : String?,
    val data : T?
) {

    companion object {
        //성공 메시지만 반환
        fun success(status: HttpStatus): ResponseEntity<BaseResponse<Void>> {
            return ResponseEntity.status(status)
                .body(BaseResponse(status.value(), null, null))
        }

        //성공 메시지와 데이터 반환
        fun <T> success(status: HttpStatus, data: T): ResponseEntity<BaseResponse<T>> {
            return ResponseEntity.status(status)
                .body(BaseResponse(status.value(), null, data))
        }

        //에러 메시지만 반환
        fun error(message: String?, status: HttpStatus): ResponseEntity<BaseResponse<Void>> {
            return ResponseEntity.status(status)
                .body(BaseResponse(status.value(), message, null))
        }

        //에러 메시지와 데이터를 반환
        fun <T> error(message: String?, data: T, status: HttpStatus): ResponseEntity<BaseResponse<T>> {
            return ResponseEntity.status(status)
                .body(BaseResponse(status.value(), message, data))
        }
    }
}