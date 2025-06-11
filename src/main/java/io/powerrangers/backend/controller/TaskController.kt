package io.powerrangers.backend.controller

import io.powerrangers.backend.dto.*
import io.powerrangers.backend.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @PostMapping
    fun createTask(@Valid @RequestBody dto: TaskCreateRequestDto): ResponseEntity<BaseResponse<Void>> {
        taskService.createTask(dto)
        return BaseResponse.success(HttpStatus.CREATED)
    }

    @PatchMapping("/{taskId}")
    fun updateTask(@PathVariable taskId: Long, @Valid @RequestBody dto: TaskUpdateRequestDto): ResponseEntity<BaseResponse<Void>> {
        taskService.updateTask(taskId, dto)
        return BaseResponse.success(HttpStatus.OK)
    }

    @DeleteMapping("/{taskId}")
    fun removeTask(@PathVariable taskId: Long): ResponseEntity<BaseResponse<Void>> {
        taskService.removeTask(taskId)
        return BaseResponse.success(HttpStatus.NO_CONTENT)
    }

    @PatchMapping("/{taskId}/status")
    fun changeStatus(@PathVariable taskId: Long): ResponseEntity<BaseResponse<Void>> {
        taskService.changeStatus(taskId)
        return BaseResponse.success(HttpStatus.OK)
    }

    @PatchMapping("/{taskId}/image")
    @Throws(IOException::class)
    fun uploadImage(@RequestParam("image") file: MultipartFile, @PathVariable taskId: Long): ResponseEntity<BaseResponse<String>> {
        val imageUrl = taskService.uploadTaskImage(file, taskId)
        return BaseResponse.success(HttpStatus.OK, imageUrl)
    }

    @GetMapping("/{userId}/images")
    fun getTaskImages(@PathVariable userId: Long): ResponseEntity<BaseResponse<List<TaskImageResponseDto>>> {
        return BaseResponse.success(HttpStatus.OK, taskService.getTaskImages(userId))
    }

    @GetMapping("/{taskId}")
    fun getTask(@PathVariable taskId: Long): ResponseEntity<BaseResponse<TaskResponseDto>> {
        return BaseResponse.success(HttpStatus.OK, taskService.getTask(taskId))
    }

    @PatchMapping("/{taskId}/postpone")
    fun postpone(@PathVariable taskId: Long): ResponseEntity<BaseResponse<Void>> {
        taskService.postpone(taskId)
        return BaseResponse.success(HttpStatus.OK)
    }

    @GetMapping("/summary")
    fun getTaskSummary(
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam userId: Long
    ): ResponseEntity<BaseResponse<TaskSummaryResponseDto>> {
        val result = taskService.getMonthlyTaskSummary(userId, year, month)
        return BaseResponse.success(HttpStatus.OK, result)
    }
}
