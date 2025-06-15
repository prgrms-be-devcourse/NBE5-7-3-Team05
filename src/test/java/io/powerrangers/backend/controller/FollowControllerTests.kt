package io.powerrangers.backend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkStatic
import io.powerrangers.backend.config.JwtAuthenticationFilter
import io.powerrangers.backend.config.Oauth2SuccessHandler
import io.powerrangers.backend.dto.*
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.service.CustomOauth2UserService
import io.powerrangers.backend.service.FollowService
import io.powerrangers.backend.service.JwtProvider
import io.powerrangers.backend.utils.genUserFollowResList
import io.powerrangers.backend.utils.getCurrentUserId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.mockito.Mockito.`when`
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(
    controllers = [FollowController::class],
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [JwtAuthenticationFilter::class])
    ]
)
@AutoConfigureMockMvc(addFilters = false)
class FollowControllerTests {

    @Autowired
    lateinit var mockMvc : MockMvc

    @MockitoBean
    lateinit var followService: FollowService

    @Autowired
    lateinit var om : ObjectMapper

    @BeforeEach
    fun setUp() {
        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } returns 100L
    }

    @Test
    fun `팔로우 요청 성공 케이스 - 201 Created`() {

        val targetId = 2L

        val followReq = FollowRequestDto(
            followingId = targetId,
        )

        val followResponse = FollowResponseDto(
            followId = 1L,
            followerId = getCurrentUserId(),
            followingId = targetId
        )

        `when`(followService.follow(followReq)).thenReturn(followResponse)

        mockMvc.post("/follow") {
            contentType = MediaType.APPLICATION_JSON
            content = om.writeValueAsString(followReq)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.data.followId") { value(followResponse.followId)}
            jsonPath("$.data.followerId") { value(followResponse.followerId)}
            jsonPath("$.data.followingId") { value(followResponse.followingId)}
        }

    }

    @Test
    fun `팔로우 요청 실패 케이스 - 존재하지 않는 유저 - 404 Not Found`() {

        val targetId = 2L

        val followReq = FollowRequestDto(
            followingId = targetId,
        )

        `when`(followService.follow(followReq)).thenThrow(CustomException(ErrorCode.USER_NOT_FOUND))

        mockMvc.post("/follow") {
            contentType = MediaType.APPLICATION_JSON
            content = om.writeValueAsString(followReq)
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.status") { value(HttpStatus.NOT_FOUND.value()) }
            jsonPath("$.message") { value("존재하지 않는 사용자입니다.")}
        }

    }

    @Test
    fun `팔로우 요청 실패 케이스 - 이미 팔로우한 유저 - 409 Conflict`() {

        val targetId = 2L

        val followReq = FollowRequestDto(
            followingId = targetId,
        )

        `when`(followService.follow(followReq)).thenThrow(CustomException(ErrorCode.ALREADY_FOLLOWED))

        mockMvc.post("/follow") {
            contentType = MediaType.APPLICATION_JSON
            content = om.writeValueAsString(followReq)
        }.andExpect {
            status { isConflict() }
            jsonPath("$.status") { value(HttpStatus.CONFLICT.value()) }
            jsonPath("$.message") {value ("이미 팔로우한 사용자입니다.")}
        }

    }

    @Test
    fun `언팔로우 요청 성공 케이스 - 204 No Content`() {

        val targetId = 2L

        doNothing().`when`(followService).unfollow(targetId)

        mockMvc.delete("/follow/${targetId}")
            .andExpect {
                status { isNoContent() }
                jsonPath("$.status") { value(HttpStatus.NO_CONTENT.value()) }
            }

    }

    @Test
    fun `언팔로우 요청 실패 케이스 - 존재하지 않는 유저 - 404 Not found`() {

        val targetId = 2L

        `when`(followService.unfollow(targetId)).thenThrow(CustomException(ErrorCode.USER_NOT_FOUND))

        mockMvc.delete("/follow/${targetId}")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.status") { value(HttpStatus.NOT_FOUND.value()) }
                jsonPath("$.message") { value ("존재하지 않는 사용자입니다.") }
            }

    }

    @Test
    fun `언팔로우 요청 실패 케이스 - 팔로우 관계가 존재하지 않을 때 - 404 Not found`() {

        val targetId = 2L

        `when`(followService.unfollow(targetId)).thenThrow(CustomException(ErrorCode.FOLLOW_NOT_FOUND))

        mockMvc.delete("/follow/${targetId}")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.status") { value(HttpStatus.NOT_FOUND.value()) }
                jsonPath("$.message") { value ("팔로우 관계를 찾을 수 없습니다.") }
            }
    }

    @Test
    fun `팔로워 목록 요청 성공 케이스 - 200 Ok`() {

        val size = 10
        var userList = genUserFollowResList(size)

        `when`(followService.findFollowers(getCurrentUserId())).thenReturn(userList)

        val result = mockMvc.get("/follow/${getCurrentUserId()}/followers")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value(HttpStatus.OK.value()) }
                jsonPath("$.data.size()") { value(size) }
            }
            .andReturn()

        val responseStr = result.response.contentAsString
        val responseJson = om.readTree(responseStr)

        val data = responseJson["data"]
        for(i in 0 until size) {
            val expected = userList[i]
            val actual = data[i]

            expected.id shouldBe actual["id"].asText().toLong()
            expected.nickname shouldBe actual["nickname"].asText()
            expected.intro shouldBe actual["intro"].asText()
            expected.profileImage shouldBe null
        }

    }

    @Test
    fun `팔로워 목록 요청 실패 케이스 - 존재하지 않는 유저 - 404 Not found`() {

        val targetId = 2L

        `when`(followService.findFollowers(targetId)).thenThrow(CustomException(ErrorCode.USER_NOT_FOUND))

        mockMvc.get("/follow/${targetId}/followers")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.status") { value(HttpStatus.NOT_FOUND.value()) }
                jsonPath("$.message") { value("존재하지 않는 사용자입니다.")}
            }

    }

    @Test
    fun `팔로워 목록 요청 성공 케이스 - 팔로워가 없을 때 - 빈 리스트`() {

        val emptyList = mutableListOf<UserFollowResponseDto>()

        `when`(followService.findFollowers(getCurrentUserId())).thenReturn(emptyList)

        mockMvc.get("/follow/${getCurrentUserId()}/followers")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value(HttpStatus.OK.value()) }
                jsonPath("$.data.size()") { value(0)}
            }

    }

    @Test
    fun `팔로잉 목록 요청 성공 케이스 - 팔로잉이 있을 때 - 200 Ok`() {

        val size = 10
        var userList = genUserFollowResList(size)

        `when`(followService.findFollowings(getCurrentUserId())).thenReturn(userList)

        val result = mockMvc.get("/follow/${getCurrentUserId()}/followings")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value(HttpStatus.OK.value()) }
                jsonPath("$.data.size()") { value(size) }
            }
            .andReturn()

        val responseStr = result.response.contentAsString
        val responseJson = om.readTree(responseStr)

        val data = responseJson["data"]
        for(i in 0 until size) {
            val expected = userList[i]
            val actual = data[i]

            expected.id shouldBe actual["id"].asText().toLong()
            expected.nickname shouldBe actual["nickname"].asText()
            expected.intro shouldBe actual["intro"].asText()
            expected.profileImage shouldBe null
        }
    }

    @Test
    fun `팔로잉 목록 요청 성공 케이스 - 팔로잉이 없을 때 - 빈 리스트`() {

        val emptyList = mutableListOf<UserFollowResponseDto>()

        `when`(followService.findFollowings(getCurrentUserId())).thenReturn(emptyList)

        mockMvc.get("/follow/${getCurrentUserId()}/followings")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value(HttpStatus.OK.value()) }
                jsonPath("$.data.size()") { value(0) }
            }

    }

    @Test
    fun `팔로워, 팔로잉 수 조회 요청 성공 케이스 - 200 OK`() {

        val count = 10L

        val followCount = FollowCountResponseDto(
            userId = getCurrentUserId(),
            followerCount = count,
            followingCount = count
        )

        `when`(followService.getFollowCount(getCurrentUserId())).thenReturn(followCount)

        mockMvc.get("/follow/${getCurrentUserId()}")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value(HttpStatus.OK.value()) }
                jsonPath("$.data.followerCount") { value(count) }
                jsonPath("$.data.followingCount") { value(count) }
            }
    }

    @Test
    fun `로그인한 사용자와 타겟 사용자 간의 관계 조회 성공 케이스 - 팔로우 관계일 때`() {

        val targetId = 2L

        val followCheck = FollowCheckResponseDto(
            userId = targetId,
            following = true
        )

        `when`(followService.checkFollowingRelationship(targetId)).thenReturn(followCheck)

        mockMvc.get("/follow/check?userId=$targetId")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value(HttpStatus.OK.value()) }
                jsonPath("$.data.userId") { value(targetId) }
                jsonPath("$.data.following") {value(true)}
            }
    }

    @Test
    fun `로그인한 사용자와 타겟 사용자 간의 관계 조회 성공 케이스 - 팔로우 관계가 아닐 때`() {

        val targetId = 2L

        val followCheck = FollowCheckResponseDto(
            userId = targetId,
            following = false
        )

        `when`(followService.checkFollowingRelationship(targetId)).thenReturn(followCheck)

        mockMvc.get("/follow/check?userId=$targetId")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value(HttpStatus.OK.value()) }
                jsonPath("$.data.userId") { value(targetId) }
                jsonPath("$.data.following") {value(false)}
            }
    }

    @Test
    fun `로그인한 사용자와 타겟 사용자 간의 관계 조회 실패 케이스 - 존재하지 않는 사용자일 때`() {

        val targetId = 2L

        `when`(followService.checkFollowingRelationship(targetId)).thenThrow(CustomException(ErrorCode.USER_NOT_FOUND))

        mockMvc.get("/follow/check?userId=$targetId")
            .andExpect {
                status { isNotFound() }
                jsonPath("$.status") { value(HttpStatus.NOT_FOUND.value()) }
                jsonPath("$.message") { value("존재하지 않는 사용자입니다.")}
            }
    }
}