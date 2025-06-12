package io.powerrangers.backend.controller

import io.powerrangers.backend.dto.BaseResponse
import io.powerrangers.backend.dto.CommentCreateRequestDto
import io.powerrangers.backend.dto.CommentResponseDto
import io.powerrangers.backend.dto.CommentUpdateRequestDto
import io.powerrangers.backend.dto.CommentUpdateResponseDto
import io.powerrangers.backend.service.CommentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/comments")
class CommentController (
    private val commentService: CommentService
){
    @PostMapping
    fun createComment(@RequestBody @Valid request: CommentCreateRequestDto): ResponseEntity<BaseResponse<CommentResponseDto>> {
        val response = commentService.createComment(request)
        return BaseResponse.success(HttpStatus.OK, response)
    }

    @GetMapping("/{taskId}")
    fun getComments(@PathVariable taskId: Long): ResponseEntity<BaseResponse<List<CommentResponseDto>>> {
        val comments = commentService.getComments(taskId)
        return BaseResponse.success(HttpStatus.OK, comments)
    }

    @PutMapping("/{commentId}")
    fun updateComment(
        @PathVariable commentId: Long,
        @RequestBody @Valid request: CommentUpdateRequestDto
    ): ResponseEntity<BaseResponse<CommentUpdateResponseDto>> {
        val response = commentService.updateComment(commentId, request)
        return BaseResponse.success(HttpStatus.OK, response)
    }

    @DeleteMapping("/{commentId}")
    fun deleteComment(@PathVariable commentId: Long): ResponseEntity<BaseResponse<Void>> {
        commentService.deleteComment(commentId)
        return BaseResponse.success(HttpStatus.NO_CONTENT)
    }
}