package io.powerrangers.backend.service

import io.powerrangers.backend.dao.CommentRepository
import io.powerrangers.backend.dao.TaskRepository
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.comment.CommentCreateRequestDto
import io.powerrangers.backend.dto.comment.CommentResponseDto
import io.powerrangers.backend.dto.comment.CommentUpdateRequestDto
import io.powerrangers.backend.dto.comment.CommentUpdateResponseDto
import io.powerrangers.backend.entity.Comment
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class CommentService (
    private val commentRepository: CommentRepository,
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createComment(request: CommentCreateRequestDto): CommentResponseDto {
        val task = taskRepository.findById(request.taskId)
            .orElseThrow<CustomException> { CustomException(ErrorCode.TASK_NOT_FOUND) }

        val user = userRepository.findById(ContextUtil.getCurrentUserId())
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        val parent = request.parentId?.let {
            commentRepository.findById(it)
                .orElseThrow { CustomException(ErrorCode.COMMENT_NOT_FOUND) }
        }


        val comment = Comment(
            task=task,
            user=user,
            parent=parent,
            content=request.content
        )
        commentRepository.save(comment)
        return CommentResponseDto.from(comment)
    }

    @Transactional(readOnly = true)
    fun getComments(taskId: Long): List<CommentResponseDto> {
        taskRepository.findById(taskId).orElseThrow { CustomException(ErrorCode.TASK_NOT_FOUND) }

        val allComments = commentRepository.findByTaskId(taskId).orEmpty()

        // 부모 댓글만 필터링
        val parentComments = allComments
            .filter { it?.parent == null }
            .filterNotNull()

        return parentComments.map { toDto(it, allComments.filterNotNull()) }
    }

    @Transactional
    fun updateComment(commentId: Long, request: CommentUpdateRequestDto): CommentUpdateResponseDto {
        val comment = commentRepository.findById(commentId)
            .orElseThrow { CustomException(ErrorCode.COMMENT_NOT_FOUND) }

        validateOwner(comment)
        comment.content=request.content

        return CommentUpdateResponseDto.from(comment)
    }

    @Transactional
    fun deleteComment(commentId: Long) {
        val comment = commentRepository.findById(commentId)
            .orElseThrow { CustomException(ErrorCode.COMMENT_NOT_FOUND) }
        validateOwner(comment)
        commentRepository.deleteById(commentId)
    }

    //조회 메서드에서 트리형태로 반환하기 위한 private 메서드
    private fun toDto(parent: Comment, allComments: List<Comment>): CommentResponseDto {
        val children = allComments
            .filter { it.parent?.id == parent.id }
            .map { toDto(it, allComments) }

        return CommentResponseDto(
            id = parent.id ?: throw IllegalStateException("댓글 ID는 null일 수 없습니다."),
            userId = parent.user.id,
            content = parent.content,
            nickname = parent.user.nickname,
            profileImage = parent.user.profileImage,
            children = children,
            createdAt = parent.createdAt
        )
    }

    private fun validateOwner(comment: Comment) {
        val currentUserId = ContextUtil.getCurrentUserId()
        if (comment.user.id!=currentUserId) {
            throw CustomException(ErrorCode.NOT_THE_OWNER)
        }
    }
}
