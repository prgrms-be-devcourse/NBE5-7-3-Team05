package io.powerrangers.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.common.contenttype.ContentType
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.powerrangers.backend.config.JwtAuthenticationFilter
import io.powerrangers.backend.dto.CommentCreateRequestDto
import io.powerrangers.backend.dto.CommentResponseDto
import io.powerrangers.backend.dto.CommentUpdateRequestDto
import io.powerrangers.backend.dto.CommentUpdateResponseDto
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.service.CommentService
import io.powerrangers.backend.utils.getCurrentUserId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doNothing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.*
import java.time.LocalDateTime

@WebMvcTest(
    controllers = [CommentController::class],
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [JwtAuthenticationFilter::class])
    ]
)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var commentService: CommentService

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } returns 1L
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
    }

    @Test
    fun `댓글 생성 요청 성공하면 200 OK와 응답 데이터 반환`() {
        val request = CommentCreateRequestDto(
            taskId = 1L,
            parentId = null,
            content = "Hello World")

        val response = CommentResponseDto(
            id = 1L,
            content = "Hello World",
            nickname = "Tester",
            createdAt = LocalDateTime.now(),
            userId = 1L,
            profileImage = null
        )

        given(commentService.createComment(request)).willReturn(response)

        mockMvc.post("/comments"){
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.data.id") {value(1)}
                jsonPath("$.data.content") {value("Hello World")}
                jsonPath("$.data.nickname") {value("Tester")}
            }
    }

    @Test
    fun `댓글 생성 요청 시 content 비어있으면 400 반환`() {
        val request = CommentCreateRequestDto(
            taskId = 1L,
            parentId = null,
            content = ""
        )

        mockMvc.post("/comments"){
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `댓글 생성 요청 시 taskId 누락되면 400 반환`(){
        val request = """
            {
            "content": "Hello World",
            "parentId": null
        }
        """.trimIndent()

        mockMvc.post("/comments"){
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `댓글 목록 조회 요청 성공하면 200 OK와 응답 데이터 반환`() {
        val taskId = 1L
        val response = listOf(
            CommentResponseDto(
                id = 1L,
                content = "첫 번째 댓글",
                nickname = "User1",
                createdAt = LocalDateTime.now(),
                userId = 1L,
                profileImage = null
            ),
            CommentResponseDto(
                id = 2L,
                content = "두 번째 댓글",
                nickname = "User2",
                createdAt = LocalDateTime.now(),
                userId = 2L,
                profileImage = "https://example.com/profile.jpg"
            )
        )

        given(commentService.getComments(taskId)).willReturn(response)

        mockMvc.get("/comments/${taskId}")
            .andExpect {
                status { isOk() }
                jsonPath("$.data[0].id") { value(1) }
                jsonPath("$.data[0].content") { value("첫 번째 댓글") }
                jsonPath("$.data[1].id") { value(2) }
                jsonPath("$.data[1].nickname") { value("User2") }
            }
    }

    @Test
    fun `존재하지 않는 taskId로 댓글 조회  400 예외 반환`() {
        val taskId = 999L


        given(commentService.getComments(taskId)).willThrow(CustomException(ErrorCode.TASK_NOT_FOUND))

        mockMvc.get("/comments/${taskId}")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") {
                    value(ErrorCode.TASK_NOT_FOUND.message)
                }
            }
    }

    @Test
    fun `댓글 수정 요청 성공 시 200 OK와 응답 데이터 반환`() {
        val commentId = 1L
        val request = CommentUpdateRequestDto(content = "수정된 댓글입니다.")

        val response = CommentUpdateResponseDto(
            id = commentId,
            content = "수정된 댓글입니다.",
            nickname = "Tester",
            profileImage = null
        )

        given(commentService.updateComment(commentId,request)).willReturn(response)

        mockMvc.put("/comments/${commentId}"){
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.data.id") { value(1) }
                jsonPath("$.data.content") {value("수정된 댓글입니다.")}
            }
    }

    @Test
    fun `존재하지 않는 댓글을 수정하려하면 404 NotFound`() {
        val commentId=999L
        val request = CommentUpdateRequestDto(content = "수정 시도댓글")

        given(commentService.updateComment(commentId, request)).willThrow(CustomException(ErrorCode.TASK_NOT_FOUND))

        mockMvc.put("/comments/${commentId}"){
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect { status { isNotFound() }
            jsonPath("$.message") {
                value(ErrorCode.TASK_NOT_FOUND.message) }
            }
    }

    @Test
    fun `댓글 수정 시 content가 비어있으면 400 BadRequest 반환`() {
        val request = CommentUpdateRequestDto(content = "")

        mockMvc.put("/comments/1"){
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.message") { value("content : 내용을 입력해주세요.")}
            }
    }

    @Test
    fun `댓글 삭제 요청 성공 시 200OK반환`() {
        val commentId = 1L

        doNothing().`when`(commentService).deleteComment(commentId)

        mockMvc.delete("/comments/${commentId}")
            .andExpect { status { isNoContent() } }

    }

    @Test
    fun `댓글 삭제 요청 시, 댓글이 존재하지 않으면 404 NotFound`() {
        val commentId = 999L

        given(commentService.deleteComment(commentId)).willThrow(CustomException(ErrorCode.TASK_NOT_FOUND))
        mockMvc.delete("/comments/${commentId}")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") {value(ErrorCode.TASK_NOT_FOUND.message)}
            }
    }
}
