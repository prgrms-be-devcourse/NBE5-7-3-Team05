package io.powerrangers.backend.service

import io.powerrangers.backend.dao.CommentRepository
import io.powerrangers.backend.dao.TaskRepository
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.CommentCreateRequestDto
import io.powerrangers.backend.dto.CommentResponseDto
import io.powerrangers.backend.dto.CommentUpdateRequestDto
import io.powerrangers.backend.dto.CommentUpdateResponseDto
import io.powerrangers.backend.entity.Comment
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.utils.getCurrentUserId
import io.powerrangers.backend.utils.toResponseDto
import io.powerrangers.backend.utils.toUpdateResponseDto
import org.springframework.data.repository.findByIdOrNull
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
        val task = taskRepository.findByIdOrNull(request.taskId)
            ?: throw CustomException(ErrorCode.TASK_NOT_FOUND)

        val user = userRepository.findByIdOrNull(getCurrentUserId())
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val parent = request.parentId?.let {
            commentRepository.findByIdOrNull(it)
                ?: throw CustomException(ErrorCode.COMMENT_NOT_FOUND)
        }


        val comment = Comment(
            task=task,
            user=user,
            parent=parent,
            content=request.content
        )
        commentRepository.save(comment)
        return comment.toResponseDto()
    }

    @Transactional(readOnly = true)
    fun getComments(taskId: Long): List<CommentResponseDto> {
        taskRepository.findByIdOrNull(taskId)
            ?: throw CustomException(ErrorCode.TASK_NOT_FOUND)

        val allComments = commentRepository.findByTaskId(taskId).orEmpty().filterNotNull()

        // 자식 댓글들을 부모 ID 기준으로 그룹화하여 매핑
        val childrenMap: Map<Long, List<Comment>> = allComments
            .filter { it.parent?.id != null }
            .groupBy { it.parent!!.id!! }

        // 부모 댓글만 필터링
        val parentComments = allComments.filter { it.parent == null }

        return parentComments.map { toDto(it, childrenMap) }
    }

    @Transactional
    fun updateComment(commentId: Long, request: CommentUpdateRequestDto): CommentUpdateResponseDto {
        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw CustomException(ErrorCode.COMMENT_NOT_FOUND)

        validateOwner(comment)
        comment.content=request.content

        return comment.toUpdateResponseDto()
    }

    @Transactional
    fun deleteComment(commentId: Long) {
        val comment = commentRepository.findByIdOrNull(commentId)
            ?: throw CustomException(ErrorCode.COMMENT_NOT_FOUND)
        validateOwner(comment)
        commentRepository.deleteById(commentId)
    }

    //조회 메서드에서 트리형태로 반환하기 위한 private 메서드
    private fun toDto(parent: Comment, childrenMap: Map<Long, List<Comment>>): CommentResponseDto {
        // 현재 부모 댓글의 ID를 키로 자식 댓글 목록 조회
        val children = childrenMap[parent.id]?.map { toDto(it, childrenMap) } ?: emptyList()

        return parent.toResponseDto(children)
    }

    private fun validateOwner(comment: Comment) {
        val currentUserId = getCurrentUserId()
        if (comment.user.id!=currentUserId) {
            throw CustomException(ErrorCode.NOT_THE_OWNER)
        }
    }
}
