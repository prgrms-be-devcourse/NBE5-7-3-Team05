package io.powerrangers.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.powerrangers.backend.config.JwtAuthenticationFilter
import io.powerrangers.backend.dto.*
import io.powerrangers.backend.service.TaskService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@WebMvcTest(
    controllers = [TaskController::class],
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [JwtAuthenticationFilter::class])
    ]
)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerSliceTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var taskService: TaskService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `POST tasks - 할 일 생성 요청 시 201 CREATED 응답`() {
        val createDto = TaskCreateRequestDto(
            category = "Work",
            content = "created content",
            dueDate = LocalDateTime.now().plusDays(1),
            status = TaskStatus.INCOMPLETE,
            taskImage = null,
            scope = TaskScope.PRIVATE
        )

        mockMvc.post("/tasks") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createDto)
        }.andExpect {
            status { isCreated() }
        }

    }

    @Test
    fun `할 일 수정 요청 시 200 OK 응답`() {
        val taskId = 1L
        val updateDto = TaskUpdateRequestDto(
            category = "Work",
            content = "updated content",
            scope = TaskScope.PUBLIC
        )

        mockMvc.patch("/tasks/$taskId") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateDto)
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `할 일 삭제 요청 시 204 NO_CONTENT 응답`() {
        val taskId = 1L

        mockMvc.delete("/tasks/$taskId")
            .andExpect {
                status { isNoContent() }
            }
    }

    @Test
    fun `상태 변경 요청 시 200 OK 응답`() {
        val taskId = 1L

        mockMvc.patch("/tasks/$taskId/status")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `이미지 업로드 요청 시 200 OK 응답과 이미지 URL 반환`() {
        val taskId = 1L
        val mockFile = MockMultipartFile("image", "test.png", "image/png", "test image bytes".toByteArray())
        val expectedUrl = "http://image.url/test.png"

        `when`(taskService.uploadTaskImage(mockFile, taskId)).thenReturn(expectedUrl)



        val requestBuilder: MockHttpServletRequestBuilder = multipart("/tasks/$taskId/image")
            .file(mockFile)
            .with { req ->
                req.method = "PATCH"
                req
            }

        mockMvc.perform(requestBuilder)
            .andExpect {
                status().isOk()
                jsonPath("$.data").value(expectedUrl)
            }
    }

    @Test
    fun `유저 이미지 목록 조회 시 200 OK와 리스트 반환`() {
        val userId = 23L
        val fixedDate = LocalDateTime.of(2025, 6, 13, 12, 0, 0)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        val imageList = listOf(
            TaskImageResponseDto(taskId = 1L, imageUrl = "http://image1.url",status=TaskStatus.COMPLETE, dueDate = fixedDate),
            TaskImageResponseDto(taskId = 2L, imageUrl = "http://image2.url",status=TaskStatus.COMPLETE, dueDate = fixedDate)
        )

        `when`(taskService.getTaskImages(userId)).thenReturn(imageList)

        mockMvc.get("/tasks/$userId/images")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.size()") { value(imageList.size) }
                jsonPath("$.data[0].taskId") { value(imageList[0].taskId.toInt()) }
                jsonPath("$.data[0].imageUrl") { value(imageList[0].imageUrl) }
                jsonPath("$.data[0].status") {value(imageList[0].status.name) }
                jsonPath("$.data[0].dueDate") { value(imageList[0].dueDate.format(formatter)) }
            }
    }

    @Test
    fun `단일 할 일 조회 시 200 OK와 할 일 데이터 반환`() {
        val taskId = 1L
        val fixedDate = LocalDateTime.of(2025, 6, 13, 12, 0, 0)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        val taskResponse = TaskResponseDto(
            id = taskId,
            category = "Work",
            content = "Test Content",
            dueDate = fixedDate,
            status = TaskStatus.INCOMPLETE,
            taskImage = null,
            scope = TaskScope.PUBLIC,
            nickname = "test"
        )

        `when`(taskService.getTask(taskId)).thenReturn(taskResponse)

        mockMvc.get("/tasks/$taskId")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.id") { value(taskId.toInt()) }
                jsonPath("$.data.category") { value(taskResponse.category) }
                jsonPath("$.data.content") { value(taskResponse.content) }
                jsonPath("$.data.dueDate") { value(taskResponse.dueDate.format(formatter)) }
                jsonPath("$.data.status") { value(taskResponse.status.name) }
                jsonPath("$.data.taskImage") { value(taskResponse.taskImage) }
                jsonPath("$.data.scope") { value(taskResponse.scope.name) }
                jsonPath("$.data.nickname") { value(taskResponse.nickname) }
            }
    }

    @Test
    fun `내일로 미루기 요청 시 200 OK 응답`() {
        val taskId = 1L

        mockMvc.patch("/tasks/$taskId/postpone")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `요약 정보 요청 시 날짜별 할 일 갯수와 200 OK를 반환한다`() {

        val year = 2025
        val month = 6
        val userId = 23L

        val summary = TaskSummaryResponseDto(
            year = year,
            month = month,
            dailySummaries = listOf(
                TaskSummaryResponseDto.DailySummary("2025-06-01", 3),
                TaskSummaryResponseDto.DailySummary("2025-06-02", 5),
            )
        )

        `when`(taskService.getMonthlyTaskSummary(userId, year, month)).thenReturn(summary)

        mockMvc.get("/tasks/summary") {
            param("year", year.toString())
            param("month", month.toString())
            param("userId", userId.toString())
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.data.year") { value(year) }
            jsonPath("$.data.month") { value(month) }
            jsonPath("$.data.dailySummaries.size()") { value(2) }
            jsonPath("$.data.dailySummaries[0].date") { value("2025-06-01") }
            jsonPath("$.data.dailySummaries[0].taskCount") { value(3) }
            jsonPath("$.data.dailySummaries[1].date") { value("2025-06-02") }
            jsonPath("$.data.dailySummaries[1].taskCount") { value(5) }
        }
    }
}
