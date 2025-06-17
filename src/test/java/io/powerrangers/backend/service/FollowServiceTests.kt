package io.powerrangers.backend.service

import io.kotest.matchers.shouldBe
import io.mockk.*
import io.powerrangers.backend.dao.FollowRepository
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.FollowRequestDto
import io.powerrangers.backend.dto.TaskScope
import io.powerrangers.backend.entity.Follow
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.utils.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.repository.findByIdOrNull

class FollowServiceTests {

    val followRepository = mockk<FollowRepository>()
    val userRepository = mockk<UserRepository>()
    val notificationService = mockk<NotificationService>()
    val followService = FollowService(followRepository, userRepository,notificationService)

    private val size = 5
    private val me = genUserWithId(1L, "me", "me@gmail.com")
    private val user = genUserWithId(2L, "testUser", "test@gmail.com")
    private val userList = genUserListWithId(size)

    @BeforeEach
    fun setUp() {
        mockkStatic("io.powerrangers.backend.utils.ContextUtilKt")
        every { getCurrentUserId() } returns 1L

        every { userRepository.findByIdOrNull(1L) } returns me
        every { userRepository.findByIdOrNull(user.id) } returns user
    }

    @Test
    fun `follow 성공 케이스 - FollowResponseDto 반환`() {

        val followReq = FollowRequestDto (
            followingId = user.id!!
        )

        every { followRepository.existsByFollowerAndFollowing(me, user) } returns false

        val newFollow = Follow(id = 1L, follower = me, following = user)

        every { followRepository.save(any()) } returns newFollow

        val expectedFollow = followService.follow(followReq)

        expectedFollow.followerId shouldBe me.id
        expectedFollow.followingId shouldBe user.id

        verify(exactly = 1) { followRepository.save(any()) }
    }

    @Test
    fun `follow 실패 케이스 - 이미 팔로우했을 경우, CustomException을 throw`() {

        every { followRepository.existsByFollowerAndFollowing(me, user) } returns true

        val followReq = FollowRequestDto (
            followingId = user.id!!
        )

        val expected = assertThrows<CustomException> {
            followService.follow(followReq)
        }

        verify(exactly = 0) { followRepository.save(any()) }
        expected.errorCode shouldBe ErrorCode.ALREADY_FOLLOWED
    }

    @Test
    fun `unfollow 성공 케이스`() {

        val findFollow = Follow(
            id = 1L,
            follower = me,
            following = user
        )

        every { followRepository.findByFollowerAndFollowing(me, user) } returns findFollow
        every { followRepository.delete(findFollow)} just Runs

        followService.unfollow(user.id!!)

        verify(exactly = 1) { followRepository.delete(findFollow)}
    }

    @Test
    fun `팔로워 리스트 조회 성공 케이스 - userId로 팔로워 조회 시 UserFollowResponseDto가 리스트에 담겨 조회된다`() {

        every { followRepository.findFollowersByUser(me.id!!) } returns userList

        val findFollowers = followService.findFollowers(me.id!!)

        findFollowers.size shouldBe size
        verify(exactly = 1) { followRepository.findFollowersByUser(me.id!!) }

        findFollowers.forEachIndexed { idx, actual ->
            val expected = userList[idx]

            actual.id shouldBe expected.id
            actual.nickname shouldBe expected.nickname
        }

    }

    @Test
    fun `팔로잉 리스트 조회 성공 케이스 - userId로 조회 시 UserFollowResponseDto가 리스트로 반환된다`() {

        every { followRepository.findFollowingsByUser(me.id!!) } returns userList

        val findFollowings = followService.findFollowings(me.id!!)

        findFollowings.size shouldBe size
        verify(exactly = 1) { followRepository.findFollowingsByUser(me.id!!) }

        findFollowings.forEachIndexed { idx, actual ->
            val expected = userList[idx]

            actual.id shouldBe expected.id
            actual.nickname shouldBe expected.nickname
        }
    }

    @Test
    fun `팔로우 관계 있을 때 케이스 - 로그인한 사용자-타겟 사용자 간의 팔로워 관계를 확인하는 테스트`() {

        every { followRepository.existsByFollowerAndFollowing(me, user) } returns true

        val check = followService.checkFollowingRelationship(user.id!!)

        check.userId shouldBe user.id
        check.following shouldBe true

    }

    @Test
    fun `팔로우 관계 없을 때 케이스 - 로그인한 사용자-타겟 사용자 간의 팔로워 관계를 확인하는 테스트`() {

        every { followRepository.existsByFollowerAndFollowing(me, user) } returns false

        val check = followService.checkFollowingRelationship(user.id!!)

        check.userId shouldBe user.id
        check.following shouldBe false
    }

    @Test
    fun `타겟 사용자가 나일 경우 - Private`() {

        val scope = followService.checkScopeWithUser(me.id!!)

        scope shouldBe TaskScope.PRIVATE

        verify(exactly = 0) { userRepository.findByIdOrNull(me.id!!) }

    }

    @Test
    fun `타겟 사용자가 나와 맞팔 관계인 경우 - FOLLOWERS`() {

        every { followRepository.existsByFollowerAndFollowing(me, user) } returns true
        every { followRepository.existsByFollowerAndFollowing(user, me) } returns true

        val scope = followService.checkScopeWithUser(user.id!!)

        scope shouldBe TaskScope.FOLLOWERS

    }

    @Test
    fun `타겟 사용자와 내가 아무 관계도 없거나, 단방향 팔로우 관계일 경우 - PUBLIC`() {

        every { followRepository.existsByFollowerAndFollowing(me, user) } returns false
        every { followRepository.existsByFollowerAndFollowing(user, me) } returns false

        val scope1 = followService.checkScopeWithUser(user.id!!)

        scope1 shouldBe TaskScope.PUBLIC

        every { followRepository.existsByFollowerAndFollowing(me, user) } returns true

        val scope2 = followService.checkScopeWithUser(user.id!!)

        scope2 shouldBe TaskScope.PUBLIC

    }

    @Test
    fun `사용자의 팔로워, 팔로잉 수 조회 성공 케이스`() {

        every { followRepository.countFollowersByUser(me.id!!) } returns 0
        every { followRepository.countFollowingsByUser(me.id!!) } returns 10

        val followCount = followService.getFollowCount(me.id!!)

        followCount.userId shouldBe me.id
        followCount.followerCount shouldBe 0
        followCount.followingCount shouldBe 10

    }
}