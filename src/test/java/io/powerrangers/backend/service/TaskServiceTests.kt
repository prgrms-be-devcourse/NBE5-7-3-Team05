package io.powerrangers.backend.service

import io.kotest.matchers.shouldBe
import io.mockk.*
import io.powerrangers.backend.dao.TaskRepository
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.*
import io.powerrangers.backend.entity.Task
import io.powerrangers.backend.entity.User
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime
import io.powerrangers.backend.utils.getCurrentUserId

class TaskServiceTests {

    private lateinit var taskRepository: TaskRepository
    private lateinit var userRepository: UserRepository
    private lateinit var s3Service: S3Service
    private lateinit var followService: FollowService
    private lateinit var taskService: TaskService

    private val testUser = User(
            id = 1L,
            nickname = "testNickname",
            profileImage = null,
            provider = "google",
            providerId = "google-12345",
            email = "test@example.com",
            intro = "테스트 유저입니다.",
            role = Role.USER
    )

    private val testTask = Task(
            id = 1L,
            category = "work",
            content = "테스트 할 일",
            dueDate = LocalDateTime.now().plusDays(1),
            status = TaskStatus.INCOMPLETE,
            taskImage = null,
            scope = TaskScope.PRIVATE,
            user = testUser
    )

    @BeforeEach
    fun setUp() {
        taskRepository = mockk()
        userRepository = mockk()
        s3Service = mockk()
        followService = mockk()
        taskService = spyk(TaskService(taskRepository, userRepository, s3Service, followService))

        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } returns 1L
    }

    @Test
    fun `createTask - 정상 케이스`() {
        val dto = TaskCreateRequestDto(
                category = "test",
                content = "content",
                dueDate = LocalDateTime.now(),
                status = TaskStatus.INCOMPLETE,
                taskImage = null,
                scope = TaskScope.PRIVATE
        )

        every { userRepository.findByIdOrNull(1L) } returns testUser
        every { taskRepository.save(any()) } returns testTask

        assertDoesNotThrow {
            taskService.createTask(dto)
        }

        verify(exactly = 1) { taskRepository.save(any()) }
    }

    @Test
    fun `createTask - 유저가 없을 때 예외`() {
        val dto = TaskCreateRequestDto(
                category = "test",
                content = "content",
                dueDate = LocalDateTime.now(),
                status = TaskStatus.INCOMPLETE,
                taskImage = null,
                scope = TaskScope.PRIVATE
        )

        every { userRepository.findByIdOrNull(1L) } returns null

        val ex = assertThrows<CustomException> {
                taskService.createTask(dto)
        }

        ex.errorCode shouldBe ErrorCode.USER_NOT_FOUND
    }

    @Test
    fun `updateTask - 정상 케이스`() {
        val dto = TaskUpdateRequestDto(
                category = "newCategory",
                content = "newContent",
                scope = TaskScope.PUBLIC
        )

        every { taskRepository.findByIdOrNull(1L) } returns testTask
        every { getCurrentUserId() } returns 1L

        taskService.updateTask(1L, dto)

        testTask.category shouldBe "newCategory"
        testTask.content shouldBe "newContent"
        testTask.scope shouldBe TaskScope.PUBLIC
    }

    @Test
    fun `updateTask - 작성자가 아닐 경우 예외`() {

        val otherUser = User(
                id = 2L,
                nickname = "testNickname",
                profileImage = null,
                provider = "google",
                providerId = "google-12345",
                email = "test@example.com",
                intro = "테스트 유저입니다.",
                role = Role.USER
        )

        val otherTask = Task(
                category = "work",
                content = "테스트 할 일",
                dueDate = LocalDateTime.now().plusDays(1),
                status = TaskStatus.INCOMPLETE,
                taskImage = null,
                scope = TaskScope.PRIVATE,
                user = otherUser
        )

        every { taskRepository.findByIdOrNull(1L) } returns otherTask
        every { getCurrentUserId() } returns 1L

        val ex = assertThrows<CustomException> {
                taskService.updateTask(1L, mockk())
        }

        ex.errorCode shouldBe ErrorCode.NOT_THE_OWNER
    }

    @Test
    fun `removeTask - 정상 케이스`() {
        every { taskRepository.findByIdOrNull(1L) } returns testTask
        every { getCurrentUserId() } returns 1L
        every { taskRepository.delete(testTask) } just Runs

        taskService.removeTask(1L)

        verify { taskRepository.delete(testTask) }
    }

    @Test
    fun `removeTask - 작성자가 아닐 경우 예외`() {

        val otherUser = User(
                id = 2L,
                nickname = "testNickname",
                profileImage = null,
                provider = "google",
                providerId = "google-12345",
                email = "test@example.com",
                intro = "테스트 유저입니다.",
                role = Role.USER
        )

        val otherTask = Task(
                category = "work",
                content = "테스트 할 일",
                dueDate = LocalDateTime.now().plusDays(1),
                status = TaskStatus.INCOMPLETE,
                taskImage = null,
                scope = TaskScope.PRIVATE,
                user = otherUser
        )

        every { taskRepository.findByIdOrNull(1L) } returns otherTask
        every { getCurrentUserId() } returns 1L

        val ex = assertThrows<CustomException> {
                taskService.removeTask(1L)
        }

        ex.errorCode shouldBe ErrorCode.NOT_THE_OWNER
    }

    @Test
    fun `changeStatus - INCOMPLETE to COMPLETE`() {
        every { taskRepository.findByIdOrNull(1L) } returns testTask
        every { getCurrentUserId() } returns 1L

        testTask.status = TaskStatus.INCOMPLETE

        taskService.changeStatus(1L)

        testTask.status shouldBe TaskStatus.COMPLETE
    }

    @Test
    fun `changeStatus - COMPLETE to INCOMPLETE`() {
        every { taskRepository.findByIdOrNull(1L) } returns testTask
        every { getCurrentUserId() } returns 1L
        testTask.status = TaskStatus.COMPLETE

        taskService.changeStatus(1L)

        testTask.status shouldBe TaskStatus.INCOMPLETE
    }

    @Test
    fun `uploadTaskImage - 정상 케이스`() {
        val file = mockk<MultipartFile>()
        every { file.isEmpty } returns false
        every { file.contentType } returns "image/png"
        every { s3Service.upload(file) } returns "https://fakeurl.com/image.png"
        every { taskRepository.findByIdOrNull(1L) } returns testTask
        every { getCurrentUserId() } returns 1L

        val result = taskService.uploadTaskImage(file, 1L)

        result shouldBe "https://fakeurl.com/image.png"
        testTask.taskImage shouldBe "https://fakeurl.com/image.png"
    }

    @Test
    fun `uploadTaskImage - 파일이 없을 때`() {
        val result = assertThrows<CustomException> {
                taskService.uploadTaskImage(null, 1L)
        }
        result.errorCode shouldBe ErrorCode.INVALID_REQUEST
    }

    @Test
    fun `uploadTaskImage - 이미지 파일이 아닐 때`() {
        val file = mockk<MultipartFile>()
        every { file.isEmpty } returns false
        every { file.contentType } returns "application/pdf"

        val result = assertThrows<CustomException> {
                taskService.uploadTaskImage(file, 1L)
        }
        result.errorCode shouldBe ErrorCode.UNSUPPORTED_RESOURCE
    }

    @Test
    fun `getTaskImages - 정상 케이스`() {
        every { taskService.getTasksByScope(1L) } returns listOf(testTask)

        val result = taskService.getTaskImages(1L)

        result.size shouldBe 1
        result[0].imageUrl shouldBe testTask.taskImage
        result[0].status shouldBe testTask.status
        result[0].dueDate shouldBe testTask.dueDate
    }

    @Test
    fun `getTasksByScope - PRIVATE scope`() {
        every { userRepository.findByIdOrNull(1L) } returns testUser
        every { followService.checkScopeWithUser(1L) } returns TaskScope.PRIVATE
        every { taskRepository.findAllByUserId(1L) } returns listOf(testTask)

        val result = taskService.getTasksByScope(1L)

        testTask shouldBe result[0]

        verify { taskRepository.findAllByUserId(1L) }
    }

    @Test
    fun `getTasksByScope - FOLLOWERS scope`() {
        every { userRepository.findByIdOrNull(1L) } returns testUser
        every { followService.checkScopeWithUser(1L) } returns TaskScope.FOLLOWERS
        every { taskRepository.findTasksForFollowers(1L) } returns listOf(testTask)

        val result = taskService.getTasksByScope(1L)

        testTask shouldBe result[0]
        verify { taskRepository.findTasksForFollowers(1L) }
    }

    @Test
    fun `getTasksByScope - PUBLIC scope`() {
        every { userRepository.findByIdOrNull(1L) } returns testUser
        every { followService.checkScopeWithUser(1L) } returns TaskScope.PUBLIC
        every { taskRepository.findTasksForPublic(1L) } returns listOf(testTask)

        val result = taskService.getTasksByScope(1L)

        testTask shouldBe result[0]
        verify { taskRepository.findTasksForPublic(1L) }
    }

    @Test
    fun `getTask - 본인일 경우`() {
        testTask.scope = TaskScope.PRIVATE

        every { taskRepository.findByIdOrNull(1L) } returns testTask
        every { followService.checkScopeWithUser(testUser.id!!) } returns TaskScope.PRIVATE

        val result = taskService.getTask(1L)

        val expected = TaskResponseDto(
                id = testTask.id!!,
                category = testTask.category,
                content = testTask.content,
                status = testTask.status,
                taskImage = testTask.taskImage,
                dueDate = testTask.dueDate,
                scope = testTask.scope,
                nickname = testTask.user.nickname
        )

        expected shouldBe result
    }

    @Test
    fun `getTask - 팔로워 관계일 경우 공개범위가 FOLLOWERS인 할 일에 접근할 수 있다`() {
        testTask.scope = TaskScope.FOLLOWERS

        every { taskRepository.findByIdOrNull(1L) } returns testTask
        every { followService.checkScopeWithUser(testUser.id!!) } returns TaskScope.FOLLOWERS

        val result = taskService.getTask(1L)

        val expected = TaskResponseDto(
                id = testTask.id!!,
                category = testTask.category,
                content = testTask.content,
                status = testTask.status,
                taskImage = testTask.taskImage,
                dueDate = testTask.dueDate,
                scope = testTask.scope,
                nickname = testTask.user.nickname
        )

        expected shouldBe result
    }

    @Test
    fun `getTask - 팔로워 관계일 경우 공개범위가 PUBLIC인 할 일도 접근할 수 있다`() {
        testTask.scope = TaskScope.PUBLIC

        every { taskRepository.findByIdOrNull(1L) } returns testTask
        every { followService.checkScopeWithUser(testUser.id!!) } returns TaskScope.FOLLOWERS

        val result = taskService.getTask(1L)

        val expected = TaskResponseDto(
                id = testTask.id!!,
                category = testTask.category,
                content = testTask.content,
                status = testTask.status,
                taskImage = testTask.taskImage,
                dueDate = testTask.dueDate,
                scope = testTask.scope,
                nickname = testTask.user.nickname
        )

        result shouldBe expected
    }

    @Test
    fun `getTask - 아무 관계도 아닐 경우 PUBLIC인 할 일에 접근 가능하다`() {
        testTask.scope = TaskScope.PUBLIC

        every { taskRepository.findByIdOrNull(1L) } returns testTask
        every { followService.checkScopeWithUser(testUser.id!!) } returns TaskScope.PUBLIC

        val result = taskService.getTask(1L)

        val expected = TaskResponseDto(
                id = testTask.id!!,
                category = testTask.category,
                content = testTask.content,
                status = testTask.status,
                taskImage = testTask.taskImage,
                dueDate = testTask.dueDate,
                scope = testTask.scope,
                nickname = testTask.user.nickname
        )

        expected shouldBe result
    }

    @Test
    fun `getTask - 접근 권한이 없는 경우`() {
        testTask.scope = TaskScope.PRIVATE

        every { taskRepository.findByIdOrNull(1L) } returns testTask
        every { followService.checkScopeWithUser(testUser.id!!) } returns TaskScope.PUBLIC

        val ex = assertThrows<CustomException> {
                taskService.getTask(1L)
        }

        ex.errorCode shouldBe ErrorCode.NOT_ALLOWED
    }

    @Test
    fun `postpone - 정상 케이스`() {
        val now = LocalDateTime.now()
        val task = spyk(testTask)
        task.dueDate = now

        every { taskRepository.findByIdOrNull(1L) } returns task
        every { taskRepository.save(task) } returns task

        taskService.postpone(1L)

        task.dueDate = now.plusHours(24)

        verify { taskRepository.save(task) }
    }

    @Test
    fun `getMonthlyTaskSummary - 정상 케이스`() {
        val year = 2025
        val month = 6
        val currentUserId = 10L
        val targetUserId = 1L
        val start = LocalDate.of(year, month, 1)
        val end = start.withDayOfMonth(start.lengthOfMonth())

        val rawResult: List<Array<Any>> = listOf(
                arrayOf(java.sql.Date.valueOf(start), 3L),
                arrayOf(java.sql.Date.valueOf(start.plusDays(1)), 1L)
        )

        every { getCurrentUserId() } returns currentUserId
        every { followService.checkScopeWithUser(targetUserId) } returns TaskScope.PUBLIC
        every {
            taskRepository.countTasksByDateWithScope(
                    targetUserId,
                    start.atStartOfDay(),
                    end.atTime(23, 59, 59),
                    TaskScope.PUBLIC.name,
                    currentUserId
            )
        } returns rawResult

        val summary = taskService.getMonthlyTaskSummary(targetUserId, year, month)

        year shouldBe summary.year
        month shouldBe summary.month

        assertTrue(summary.dailySummaries.any { it.date == start.toString() && it.taskCount == 3 })
        assertTrue(summary.dailySummaries.any { it.date == start.plusDays(1).toString() && it.taskCount == 1 })
    }
}

