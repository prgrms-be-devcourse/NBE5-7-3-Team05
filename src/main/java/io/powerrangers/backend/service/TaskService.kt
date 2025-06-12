package io.powerrangers.backend.service

import io.powerrangers.backend.dao.TaskRepository
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.*
import io.powerrangers.backend.entity.Task
import io.powerrangers.backend.entity.User
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.utils.getCurrentUserId
import io.powerrangers.backend.utils.toTaskImageResponseDto
import io.powerrangers.backend.utils.toTaskResponseDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val s3Service: S3Service,
    private val followService: FollowService
) {

    @Transactional
    fun createTask(dto: TaskCreateRequestDto) {
        val userId = getCurrentUserId()

        val user: User = userRepository.findByIdOrNull(userId) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val task = Task(
            category = dto.category,
            content = dto.content,
            dueDate = dto.dueDate,
            status = dto.status,
            taskImage = dto.taskImage,
            scope = dto.scope,
            user = user
        )

        taskRepository.save(task)
    }

    @Transactional
    fun updateTask(id: Long, dto: TaskUpdateRequestDto) {
        val task = getTaskIfOwner(id)
        task.updateFrom(dto)
    }

    @Transactional
    fun removeTask(id: Long) {
        val task = getTaskIfOwner(id)
        taskRepository.delete(task)
    }

    private fun getTaskIfOwner(id: Long): Task {
        val task = taskRepository.findByIdOrNull(id) ?: throw CustomException(ErrorCode.TASK_NOT_FOUND)
        val userId = getCurrentUserId()
        if (task.user.id != userId) {
            throw CustomException(ErrorCode.NOT_THE_OWNER)
        }
        return task
    }

    @Transactional
    fun changeStatus(taskId: Long) {
        val task = getTaskIfOwner(taskId)
        val status = task.status
        task.status = if (status == TaskStatus.INCOMPLETE) TaskStatus.COMPLETE else TaskStatus.INCOMPLETE
    }

    @Transactional
    @Throws(IOException::class)
    fun uploadTaskImage(file: MultipartFile?, taskId: Long): String {
        validFile(file)
        val imageUrl = s3Service.upload(file!!)

        val task = taskRepository.findByIdOrNull(taskId) ?: throw CustomException(ErrorCode.TASK_NOT_FOUND)
        task.taskImage = imageUrl

        return imageUrl
    }

    private fun validFile(file: MultipartFile?) {
        if (file == null || file.isEmpty) {
            throw CustomException(ErrorCode.INVALID_REQUEST)
        }
        if (!file.contentType!!.startsWith("image/")) {
            throw CustomException(ErrorCode.UNSUPPORTED_RESOURCE)
        }
    }

    fun getTaskImages(userId: Long): List<TaskImageResponseDto> {
        val tasks = getTasksByScope(userId)
        return tasks.map { it.toTaskImageResponseDto() }
    }

    internal fun getTasksByScope(userId: Long): List<Task> {
        userRepository.findByIdOrNull(userId) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val scope = followService.checkScopeWithUser(userId)
        return when (scope) {
            TaskScope.PRIVATE -> taskRepository.findAllByUserId(userId)
            TaskScope.FOLLOWERS -> taskRepository.findTasksForFollowers(userId)
            else -> taskRepository.findTasksForPublic(userId)
        }
    }

    fun getTask(taskId: Long): TaskResponseDto {
        val task = taskRepository.findByIdOrNull(taskId) ?: throw CustomException(ErrorCode.TASK_NOT_FOUND)
        val scope = followService.checkScopeWithUser(task.user.id!!)

        return when {
            scope == TaskScope.PRIVATE -> task.toTaskResponseDto()
            scope == TaskScope.FOLLOWERS && task.scope != TaskScope.PRIVATE -> task.toTaskResponseDto()
            scope == TaskScope.PUBLIC && task.scope == TaskScope.PUBLIC -> task.toTaskResponseDto()
            else -> throw CustomException(ErrorCode.NOT_ALLOWED)
        }
    }

    @Transactional
    fun postpone(taskId: Long) {
        val task = taskRepository.findByIdOrNull(taskId) ?: throw CustomException(ErrorCode.TASK_NOT_FOUND)
        task.dueDate = task.dueDate.plusHours(24)
        taskRepository.save(task)
    }

    fun getMonthlyTaskSummary(targetUserId: Long, year: Int, month: Int): TaskSummaryResponseDto {
        val currentUserId = getCurrentUserId()
        val scope = followService.checkScopeWithUser(targetUserId)

        val start = LocalDate.of(year, month, 1)
        val end = start.withDayOfMonth(start.lengthOfMonth())

        val result: List<Array<Any>> = taskRepository.countTasksByDateWithScope(
            targetUserId,
            start.atStartOfDay(),
            end.atTime(23, 59, 59),
            scope.name,
            currentUserId
        )

        val countMap = HashMap<LocalDate, Long>()
        for (row in result) {
            val date = (row[0] as java.sql.Date).toLocalDate()
            val count = row[1] as Long
            countMap[date] = count
        }

        val dailySummaries = ArrayList<TaskSummaryResponseDto.DailySummary>()
        for (day in 1..start.lengthOfMonth()) {
            val currentDate = start.withDayOfMonth(day)
            val count = countMap[currentDate]?.toInt() ?: 0
            dailySummaries.add(TaskSummaryResponseDto.DailySummary(currentDate.toString(), count))
        }

        return TaskSummaryResponseDto(year, month, dailySummaries)
    }
}
