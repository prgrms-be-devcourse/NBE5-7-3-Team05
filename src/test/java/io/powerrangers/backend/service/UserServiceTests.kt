package io.powerrangers.backend.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.powerrangers.backend.dao.TaskRepository
import io.powerrangers.backend.dao.TokenRepository
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.Role
import io.powerrangers.backend.dto.UserUpdateProfileRequestDto
import io.powerrangers.backend.entity.RefreshToken
import io.powerrangers.backend.entity.RefreshTokenBlackList
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.utils.genUser
import io.powerrangers.backend.utils.getCurrentUserId
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.multipart.MultipartFile
import kotlin.test.assertNotNull

private val log = KotlinLogging.logger {}

const val targetUserId = 1L
const val targetNickname = "test1"
const val targetProfileImage = "test1"
const val targetProvider = "test1"
const val targetProviderId = "test1"
const val targetEmail = "test1"
const val targetIntro = "test1"

class UserServiceUnitTests {

    val userRepository = mockk<UserRepository>()
    val refreshTokenRepositoryAdapter = mockk<TokenRepository>()
    val jwtProvider = mockk<JwtProvider>()
    val s3Service = mockk<S3Service>()
    val taskRepository = mockk<TaskRepository>()
    val followService = mockk<FollowService>()
    val taskService = TaskService(taskRepository, userRepository, s3Service, followService)
    val userService = UserService(userRepository, refreshTokenRepositoryAdapter, jwtProvider, s3Service, taskService)

    /*
    * checkNicknameDuplication (닉네임 중복 관련 테스트)
     */
    @Test
    fun `닉네임이 중복일 경우 False 반환`() {
    
        // given
        every { userRepository.existsByNickname(targetNickname) } returns false

        // when
        val result = userService.checkNicknameDuplication(targetNickname)

        // then
        assertFalse(result)

    
    }

    @Test
    fun `닉네임이 중복이 없을 경우 true 반환`() {

        // given
        every { userRepository.existsByNickname(targetNickname) } returns true

        // when
        val result = userService.checkNicknameDuplication(targetNickname)

        // then
        assertTrue(result)

    }

    /*
    * getUserProfile
     */

    @Test
    fun `getUserProfile 정상적인 userId가 들어왔을 때, 성공 테스트`() {

        // given
        val user = genUser(
            targetNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            Role.USER
        )

        ReflectionTestUtils.setField(user, "id", targetUserId)

        every { userRepository.findByIdOrNull(targetUserId) } returns user

        // when
        val actualUser = userService.getUserProfile(targetUserId)


        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(targetUserId) }

        assertNotNull(actualUser)
        actualUser.userId shouldBe targetUserId
        actualUser.nickname shouldBe targetNickname
        actualUser.profileImage shouldBe targetProfileImage
        actualUser.intro shouldBe targetIntro

    }

    @Test
    fun `getUserProfile DB에 없는 userId가 들어왔을 때, 실패 테스트`() {

        // given
        val nonExistUserId = 999L
        every { userRepository.findByIdOrNull(nonExistUserId) } returns null

        // when & then
        val exception = assertThrows<CustomException> { userService.getUserProfile(nonExistUserId) }

        exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND

    }

    /*
    * searchUserProfile
     */

    @Test
    fun `searchUserProfile 정상적인 nickname이 들어온 경우, 유저 리스트 반환`() {

        // given
        val user = genUser(
            targetNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            Role.USER
        )

        every { userRepository.findByNickname(targetNickname) } returns listOf(user)

        // when
        ReflectionTestUtils.setField(user, "id", 1L)
        val result = userService.searchUserProfile(targetNickname)

        // then
        result shouldHaveSize 1
        result.first().nickname shouldBe targetNickname


    }

    @Test
    fun `searchUserProfile nickname에 공백이 포함된 경우에도 정상 유저 리스트 반환`() {

        // given

        val trimNickname = "   test1   "
        val user = genUser(
            trimNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            Role.USER
        )

        every { userRepository.findByNickname(targetNickname) } returns listOf(user)

        // when
        ReflectionTestUtils.setField(user, "id", 1L)
        val result = userService.searchUserProfile(trimNickname)

        // then
        result shouldHaveSize 1
        result.first().nickname shouldBe trimNickname

    }

    @Test
    fun `searchUserProfile nickname이 존재하지 않는 경우 emptyList 반환`() {

        // given
        val nickname = "unknown"

        every { userRepository.findByNickname(nickname) } returns emptyList()

        // when
        val result = userService.searchUserProfile(nickname)

        // then
        result shouldHaveSize 0
        result shouldBe emptyList()

    }
    
    /*
    * updateUserProfile
     */
    @Test
    fun `updateUserProfile 정상 케이스 - 닉네임 동일, intro 수정`() {

        // given
        val duplicatedNickname = "duplicatedNickname"

        val request = UserUpdateProfileRequestDto(
            nickname = duplicatedNickname,
            intro = "new Intro",
            profileImage = null,
        )

        val user = genUser(
            duplicatedNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            Role.USER
        )

        ReflectionTestUtils.setField(user, "id", targetUserId)

        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } returns targetUserId
        every { userRepository.findByIdOrNull(targetUserId) } returns user
        every { userRepository.existsByNickname(targetNickname) } returns true
        every { s3Service.delete(any()) } just Runs

        // when
        userService.updateUserProfile(targetUserId, request, null)

        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(targetUserId) }
        verify(exactly = 0) { userRepository.existsByNickname(any()) }
        user.intro shouldBe "new Intro"
        user.nickname shouldBe duplicatedNickname
    
    }
    
    @Test
    fun `updateUserProfile 정상 케이스 - 닉네임, intro 수정, 기존 이미지 유지`() {

        // given
        val user = genUser(
            targetNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            Role.USER
        )
        ReflectionTestUtils.setField(user, "id", targetUserId)

        val newNickname = "newNickname"
        val newIntro = "newIntro"

        val request = UserUpdateProfileRequestDto(
            newNickname,
            newIntro,
            targetProfileImage,
        )

        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } returns targetUserId
        every { userRepository.findByIdOrNull(targetUserId) } returns user
        every { userRepository.existsByNickname(newNickname) } returns false

        // when & then
        userService.updateUserProfile(targetUserId, request, null)

        verify(exactly = 1) { userRepository.findByIdOrNull(targetUserId) }
        verify(exactly = 1) { userRepository.existsByNickname(newNickname) }

        user.nickname shouldBe newNickname
        user.intro shouldBe newIntro
    
    }

    @Test
    fun `updateUserProfile 정상 케이스 - 기존 이미지 삭제, 새 이미지 업로드`() {
        // given
        val user = genUser(
            targetNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            Role.USER
        )
        ReflectionTestUtils.setField(user, "id", targetUserId)

        val newImage: MultipartFile = mockk(relaxed = true)
        every { newImage.isEmpty } returns false
        every { newImage.contentType } returns "image/png"
        every { s3Service.delete(targetProfileImage) } just Runs
        every { s3Service.upload(newImage) } returns "newImageUrl"

        val request = UserUpdateProfileRequestDto(
            nickname = targetNickname,
            intro = "newIntro",
            profileImage = null // 무관
        )

        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } returns targetUserId
        every { userRepository.findByIdOrNull(targetUserId) } returns user
        every { userRepository.existsByNickname(targetNickname) } returns false

        // when
        userService.updateUserProfile(targetUserId, request, newImage)

        // then
        verify(exactly = 1) { s3Service.delete(targetProfileImage) }
        verify(exactly = 1) { s3Service.upload(newImage) }

        user.profileImage shouldBe "newImageUrl"
    }

    @Test
    fun `updateUserProfile 예외 케이스 - 유저가 존재하지 않을 때`() {

        // given
        val request = UserUpdateProfileRequestDto(
            targetNickname,
            targetIntro,
            null
        )
        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")

        every { getCurrentUserId() } returns targetUserId
        every { userRepository.findByIdOrNull(targetUserId) } returns null

        // when & then
        val exception = assertThrows<CustomException>{
            userService.updateUserProfile(targetUserId, request, null)
        }

        exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND


    }

    @Test
    fun `updateUserProfile 예외 케이스 - 본인이 아닐 때`() {

        // given
        val failUserId = 999L
        val request = UserUpdateProfileRequestDto(
            targetNickname,
            targetIntro,
            null
        )

        val user = genUser(
            targetNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            Role.USER
        )
        ReflectionTestUtils.setField(user, "id", failUserId)

        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } returns targetUserId
        every { userRepository.findByIdOrNull(failUserId) } returns user

        // when & then
        val exception = assertThrows<CustomException>{
            userService.updateUserProfile(failUserId, request, null)
        }

        exception.errorCode shouldBe ErrorCode.NOT_THE_OWNER

    }

    @Test
    fun `updateUserProfile 예외 케이스 - 닉네임이 중복 될 때`() {

        // given
        val duplicatedNickname = "duplicatedNickname"

        val request = UserUpdateProfileRequestDto(
            duplicatedNickname,
            targetIntro,
            null
        )

        val user = genUser(
            targetNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            Role.USER
        )

        ReflectionTestUtils.setField(user, "id", targetUserId)

        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } returns targetUserId
        every { userRepository.findByIdOrNull(targetUserId) } returns user
        every { userRepository.existsByNickname(duplicatedNickname) } returns true


        // when & then
        val exception = assertThrows<CustomException>{
            userService.updateUserProfile(targetUserId, request, null)
        }

        exception.errorCode shouldBe ErrorCode.DUPLICATED_NICKNAME


    }

    @Test
    fun `updateUserProfile 예외 케이스 - 이미지가 아닌 다른 종류의 파일이 들어올 때`() {

        // given
        val user = genUser(
            targetNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            Role.USER
        )
        ReflectionTestUtils.setField(user, "id", targetUserId)

        val request = UserUpdateProfileRequestDto(
            targetNickname,
            "new Intro",
            null
        )

        val invalidFile = mockk<MultipartFile> {
            every { isEmpty() } returns false
            every { contentType } returns "text/plain"
        }

        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } returns targetUserId
        every { userRepository.findByIdOrNull(targetUserId) } returns user


        // when & then
        val exception = assertThrows<CustomException>{
            userService.updateUserProfile(targetUserId, request, invalidFile)
        }

        exception.errorCode shouldBe ErrorCode.UNSUPPORTED_RESOURCE

    }

    /*
    * updateUserProfileImage
     */

    /*
    * cancelAccount
     */
    @Test
    fun `cancelAccount 정상적으로 회원탈퇴가 되는 경우 성공 테스트`() {

        // given
        val user = genUser(
            targetNickname,
            targetProfileImage,
            targetProvider,
            targetProviderId,
            targetEmail,
            targetIntro,
            Role.USER
        )

        val token = mockk<RefreshToken>(relaxed = true)
        val tokenList = listOf(token)
        val blackList = mockk<RefreshTokenBlackList>()

        ReflectionTestUtils.setField(user, "id", targetUserId)

        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")

        every { getCurrentUserId() } returns targetUserId
        every { userRepository.findByIdOrNull(targetUserId) } returns user
        every { refreshTokenRepositoryAdapter.findValidRefreshToken(targetUserId) } returns token
        every { refreshTokenRepositoryAdapter.tokenBlackList(any()) } returns false
        every { refreshTokenRepositoryAdapter.addBlackList(token) } returns blackList
        every { refreshTokenRepositoryAdapter.findAllRefreshTokensByUserId(targetUserId) } returns tokenList
        every { userRepository.deleteById(targetUserId) } just Runs

        // when
        userService.cancelAccount(targetUserId)

        // then
        verify(exactly = 1) { userRepository.findByIdOrNull(targetUserId) }
        verify(exactly = 1 ) { refreshTokenRepositoryAdapter.addBlackList(token) }
//        token.user shouldBe null

    }

    @Test
    fun `cancelAccount 현재 로그인한 유저가 본인이 아닌 경우 실패 테스트`() {

        // given
        val failUserId = 999L

        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } returns 1L
        every { userRepository.findByIdOrNull(failUserId) } returns mockk()

        // when & then
        val exception = assertThrows<CustomException> {
            userService.cancelAccount(failUserId)
        }

        exception.errorCode shouldBe ErrorCode.NOT_THE_OWNER

    }

    @Test
    fun `cancelAccount 유저가 존재하지 않는 경우 실패 테스트`() {

        // given
        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } returns targetUserId
        every { userRepository.findByIdOrNull(targetUserId) } returns null

        // when
        val exception = assertThrows<CustomException> {
            userService.cancelAccount(targetUserId)
        }

        // then
        exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND

    }
}