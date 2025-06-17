package io.powerrangers.backend.service

import io.powerrangers.backend.dao.FollowRepository
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.*
import io.powerrangers.backend.entity.Follow
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.utils.toUserFollowResponseDto
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import io.powerrangers.backend.utils.getCurrentUserId
import org.springframework.transaction.annotation.Transactional

@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository,
    private val notificationService: NotificationService
) {

    @Transactional
    fun follow(request: FollowRequestDto): FollowResponseDto {

        val follower = userRepository.findByIdOrNull(getCurrentUserId())?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val following = userRepository.findByIdOrNull(request.followingId)?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw CustomException(ErrorCode.ALREADY_FOLLOWED)
        }

        val follow = Follow(
            follower = follower,
            following = following,
        )

        val saved = try {
            followRepository.save(follow)
        } catch (e: DataIntegrityViolationException) {
            throw CustomException(ErrorCode.ALREADY_FOLLOWED)
        }
        //자기자신 팔로우 아닐경우
        if(follower.id != following.id) {
            val notification = Notification(
                receiverId = following.id!!,
                type = NotificationType.FOLLOW,
                content = "${follower.nickname}님이 당신을 팔로우했습니다."
            )
            notificationService.send(notification)
        }
        return FollowResponseDto(
            followId = saved.id!!,
            followerId = saved.follower.id!!,
            followingId = saved.following.id!!,
        )
    }

    @Transactional
    fun unfollow(followingId: Long) {
        val follower = userRepository.findByIdOrNull(getCurrentUserId())?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val following = userRepository.findByIdOrNull(followingId)?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val follow: Follow = followRepository.findByFollowerAndFollowing(follower, following)?: throw CustomException(ErrorCode.FOLLOW_NOT_FOUND)

        followRepository.delete(follow)
    }

    @Transactional(readOnly = true)
    fun findFollowers(userId: Long): List<UserFollowResponseDto> {
        userRepository.findByIdOrNull(userId)?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        // 팔로잉에 userId가 있어야 한다.
        val users = followRepository.findFollowersByUser(userId)
        return users.map { it.toUserFollowResponseDto() }
    }

    @Transactional(readOnly = true)
    fun findFollowings(userId: Long): List<UserFollowResponseDto> {
        userRepository.findByIdOrNull(userId)?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        // 팔로워 id에 userId가 있어야 한다.
        val users = followRepository.findFollowingsByUser(userId)
        return users.map { it.toUserFollowResponseDto() }
    }

    @Transactional(readOnly = true)
    fun checkFollowingRelationship(userId: Long): FollowCheckResponseDto {
        val me = userRepository.findByIdOrNull(getCurrentUserId())?:throw CustomException(ErrorCode.USER_NOT_FOUND)
        val target = userRepository.findByIdOrNull(userId)?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        return FollowCheckResponseDto(
            userId = userId,
            following = followRepository.existsByFollowerAndFollowing(me, target),
        )
    }

    @Transactional(readOnly = true)
    fun checkScopeWithUser(userId: Long): TaskScope {
        val myId = getCurrentUserId()
        if (getCurrentUserId() == userId) {
            // 내 아이디 -> PUBLIC, PRIVATE, FOLLOWER 다 줘도 됨.
            return TaskScope.PRIVATE
        }

        val me = userRepository.findByIdOrNull(myId) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val target = userRepository.findByIdOrNull(userId) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        val following = followRepository.existsByFollowerAndFollowing(me, target)
        val followed = followRepository.existsByFollowerAndFollowing(target, me)

        if (following && followed) {
            // 맞팔 관계 -> PUBLIC, FOLLOWER 까지 줘도 됨.
            return TaskScope.FOLLOWERS
        }
        // PUBLIC 만 줘야 함.
        return TaskScope.PUBLIC
    }

    @Transactional
    fun getFollowCount(userId: Long): FollowCountResponseDto {
        userRepository.findByIdOrNull(userId) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        return FollowCountResponseDto(
            userId = userId,
            followerCount = followRepository.countFollowersByUser(userId),
            followingCount = followRepository.countFollowingsByUser(userId)
        )
    }
}
