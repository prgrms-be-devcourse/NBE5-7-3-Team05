package io.powerrangers.backend.service

import io.mockk.*
import io.powerrangers.backend.dao.CommentRepository
import io.powerrangers.backend.dao.TaskRepository
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.*
import io.powerrangers.backend.entity.Comment
import io.powerrangers.backend.entity.Task
import io.powerrangers.backend.entity.User
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.utils.getCurrentUserId
import io.powerrangers.backend.utils.toResponseDto
import io.powerrangers.backend.utils.toUpdateResponseDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime
import kotlin.test.Test

class CommentServiceTests {

    val commentRepository = mockk<CommentRepository>()
    val userRepository = mockk<UserRepository>()
    val taskRepository = mockk<TaskRepository>()

    val commentService = CommentService(commentRepository, taskRepository, userRepository)

    val testUser = User(
        id = 1L,
        nickname = "유저1",
        profileImage = null,
        provider = "kakao",
        providerId = "kakao.com",
        email = "kakao@gmail.com",
        role = Role.USER,
    )
    val testTask = Task(
        id = 1L,
        category = "Study",
        content = "Test Comment",
        dueDate = LocalDateTime.now().plusDays(3),
        status = TaskStatus.INCOMPLETE,
        scope = TaskScope.PRIVATE,
        user = testUser
    )

    @BeforeEach
    fun setUp() {
        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        mockkStatic("io.powerrangers.backend.utils.ExtensionsKt")
        every { getCurrentUserId() } returns 1
    }

    @Test
    fun `createComment 성공 시 저장된 댓글 DTO반환`() {
        val request = CommentCreateRequestDto(1L, null, "테스트 댓글")

        val comment = Comment(
            id=1L,
            task = testTask,
            user = testUser,
            parent = null,
            content = "테스트 댓글",
        )
        val expected = CommentResponseDto(
            id = 1L,
            content = "테스트 댓글",
            nickname = "유저1",
            profileImage = null,
            createdAt = LocalDateTime.now(),
            userId = 1L,
            children = emptyList()
        )

        every { commentRepository.save(any()) } returns comment
        every { taskRepository.findByIdOrNull(1L) } returns testTask
        every { userRepository.findByIdOrNull(1L) } returns testUser
        every { commentRepository.findByIdOrNull(1L) } returns null


        every { any<Comment>().toResponseDto(any()) } returns expected

        val result = commentService.createComment(request)

        assertEquals(expected.copy(createdAt = result.createdAt), result)
    }

    @Test
    fun `해당 Task가 존재하지 않을 때 실페테스트`() {
        val request = CommentCreateRequestDto(999L, null, "테스트 댓글")

        every{taskRepository.findByIdOrNull(999L)} returns null
        every{userRepository.findByIdOrNull(1L)} returns testUser

        val exception = assertThrows<CustomException>{
            commentService.createComment(request)
        }

        assertEquals(ErrorCode.TASK_NOT_FOUND.message, exception.errorCode.message)
    }

    @Test
    fun `User가 존재하지 않을 때 실패테스트`() {
        val request = CommentCreateRequestDto(1L, null, "테스트 댓글")

        every{taskRepository.findByIdOrNull(1L)} returns testTask
        every { getCurrentUserId() } returns 999L
        every{userRepository.findByIdOrNull(999L)} returns null

        val exception = assertThrows<CustomException> {
            commentService.createComment(request)
        }

        assertEquals(ErrorCode.USER_NOT_FOUND.message, exception.errorCode.message)
    }

    @Test
    fun `부모댓글이 존재하지 않을 때 실패테스트`() {
        val request = CommentCreateRequestDto(1L,999L,"대댓글")

        every{taskRepository.findByIdOrNull(1L)} returns testTask
        every{userRepository.findByIdOrNull(1L)} returns testUser
        every { commentRepository.findByIdOrNull(999L) } returns null

        val exception = assertThrows<CustomException> {
            commentService.createComment(request)
        }

        assertEquals(ErrorCode.COMMENT_NOT_FOUND.message, exception.errorCode.message)
    }

    @Test
    fun `getComments 성공 시 댓글 반환`() {
        val parentComment = Comment(1L,testTask,testUser,null,"부모 댓글" )
        val childComment = Comment(2L,testTask,testUser,parentComment,"자식댓글")
        parentComment.children.add(childComment)

        every { taskRepository.findByIdOrNull(1L) } returns testTask
        every { commentRepository.findByTaskId(1L) } returns listOf(parentComment, childComment)

        val childDto = CommentResponseDto(
            id = 2L,
            content = "자식댓글",
            nickname = "유저1",
            profileImage = null,
            createdAt = LocalDateTime.now(),
            userId = 1L,
            children = emptyList()
        )

        val parentDto = CommentResponseDto(
            id = 1L,
            content = "부모 댓글",
            nickname = "유저1",
            profileImage = null,
            createdAt = LocalDateTime.now(),
            userId = 1L,
            children = listOf(childDto)
        )

        every { childComment.toResponseDto(any()) } returns childDto
        every { parentComment.toResponseDto(listOf(childDto)) } returns parentDto

        val result = commentService.getComments(1L)

        assertEquals(1, result.size)
        assertEquals("부모 댓글", result[0].content)
        assertEquals(1, result[0].children.size)
        assertEquals("자식댓글", result[0].children[0].content)
    }

    @Test
    fun `getComments 존재하지 않는 Task 실패 테스트`() {
        every { taskRepository.findByIdOrNull(999L) } returns null

        val exception = assertThrows<CustomException> {
            commentService.getComments(999L)
        }

        assertEquals(ErrorCode.TASK_NOT_FOUND.message, exception.errorCode.message)
    }

    @Test
    fun `updateComment 성공 시 수정된 댓글 DTO 반환`() {
        val request = CommentUpdateRequestDto("수정된 댓글")
        val comment = Comment(
            id=1L,
            task = testTask,
            user = testUser,
            parent = null,
            content = "기존 댓글"
        )

        every{commentRepository.findByIdOrNull(1L)} returns comment
        every{comment.toUpdateResponseDto()} returns CommentUpdateResponseDto(id=1L, content = "수정된 댓글", nickname = "유저1", profileImage = null)

        val result = commentService.updateComment(1L,request)

        assertEquals(1L,result.id)
        assertEquals("수정된 댓글",result.content)
    }

    @Test
    fun `updateComment 댓글이 존재하지 않을 시 실패테스트`() {
        val request = CommentUpdateRequestDto("수정된 댓글")

        every { commentRepository.findByIdOrNull(999L) } returns null

        val exception=assertThrows<CustomException> {
            commentService.updateComment(999L,request)
        }

        assertEquals(ErrorCode.COMMENT_NOT_FOUND.message, exception.errorCode.message)
    }

    @Test
    fun `updateComment 댓글의 작성자가 아닐 시 실패테스트`() {
        val request = CommentUpdateRequestDto("수정된 댓글")
        val otherUser = User(
            id=2L,
            nickname="유저2",
            profileImage = null,
            provider = "kakao",
            providerId = "kakao",
            email="kakao2@gmail.com",
            role = Role.USER
            )
        val comment = Comment(
            id = 1L,
            task = testTask,
            user = otherUser,
            parent = null,
            content = "기존 내용"
        )
        every { commentRepository.findByIdOrNull(1L) } returns comment

        val exception = assertThrows<CustomException> {
            commentService.updateComment(1L, request)
        }

        assertEquals(ErrorCode.NOT_THE_OWNER.message, exception.errorCode.message)
    }

    @Test
    fun `deleteComment 성공 시 댓글 삭제`() {
        val comment = Comment(
            id = 1L,
            task = testTask,
            user = testUser,
            parent = null,
            content = "삭제할 댓글"
        )

        every{commentRepository.findByIdOrNull(1L)} returns comment
        every{commentRepository.deleteById(1L)} just Runs

        commentService.deleteComment(1L)

        verify(exactly = 1) {commentRepository.deleteById(1L)}
    }

    @Test
    fun `deleteComment 댓글이 존재하지 않을 시 실페테스트`() {
        every { commentRepository.findByIdOrNull(999L) } returns null

        val exception = assertThrows<CustomException> {
            commentService.deleteComment(999L)
        }

        assertEquals(ErrorCode.COMMENT_NOT_FOUND.message, exception.errorCode.message)
    }

    @Test
    fun `deleteComment 작성자가 아닐 경우 실패테스트`(){
        val otherUser = User(
            id=2L,
            nickname="유저2",
            profileImage = null,
            provider = "kakao",
            providerId = "kakao",
            email="kakao2@gmail.com",
            role = Role.USER
        )
        val comment = Comment(
            id = 1L,
            task = testTask,
            user = otherUser,
            parent = null,
            content = "삭제 시도 댓글"
        )

        every{commentRepository.findByIdOrNull(1L)} returns comment

        val exception = assertThrows<CustomException> {
            commentService.deleteComment(1L)
        }

        assertEquals(ErrorCode.NOT_THE_OWNER.message, exception.errorCode.message)
    }
}
