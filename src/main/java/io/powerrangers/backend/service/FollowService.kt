package io.powerrangers.backend.service

import io.powerrangers.backend.dao.FollowRepository
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.*
import io.powerrangers.backend.entity.Follow
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.utils.toUserFollowResponseDto
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FollowService(
    private val followRepository: FollowRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun follow(request: FollowRequestDto): FollowResponseDto {
        // ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        val follower = userRepository.findById(ContextUtil.getCurrentUserId())
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        val following = userRepository.findById(request.followingId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw CustomException(ErrorCode.ALREADY_FOLLOWED)
        }

        val follow: Follow = Follow(
            follower = follower,
            following = following,
        )

        val saved = try {
            followRepository.save(follow)
        } catch (e: DataIntegrityViolationException) {
            throw CustomException(ErrorCode.ALREADY_FOLLOWED)
        }

        return FollowResponseDto(
            followId = saved.id!!,
            followerId = saved.follower.id,
            followingId = saved.following.id,
        )
    }

    @Transactional
    fun unfollow(followingId: Long) {
        val follower = userRepository.findById(ContextUtil.getCurrentUserId())
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        val following = userRepository.findById(followingId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        val follow: Follow = followRepository.findByFollowerAndFollowing(follower, following)?: throw CustomException(ErrorCode.FOLLOW_NOT_FOUND)

        followRepository.delete(follow)
    }

    @Transactional(readOnly = true)
    fun findFollowers(userId: Long): List<UserFollowResponseDto> {
        userRepository.findById(userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        // 팔로잉에 userId가 있어야 한다.
        val users = followRepository.findFollowersByUser(userId)

        return users.map { it.toUserFollowResponseDto() }
    }

    @Transactional(readOnly = true)
    fun findFollowings(userId: Long): List<UserFollowResponseDto> {
        userRepository.findById(userId)
            .orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        // 팔로워 id에 userId가 있어야 한다.
        val users = followRepository.findFollowingsByUser(userId)
        return users.map { it.toUserFollowResponseDto() }
//        return users.stream()
//            .map { user: User? -> UserFollowResponseDto.from(user) }
//            .toList()
    }

    @Transactional(readOnly = true)
    fun checkFollowingRelationship(userId: Long): FollowCheckResponseDto {
        val myId = ContextUtil.getCurrentUserId()

        val me = userRepository.findById(myId).orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        val target = userRepository.findById(userId).orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

        return FollowCheckResponseDto(
            userId = userId,
            following = followRepository.existsByFollowerAndFollowing(me, target),
        )
    }

    @Transactional(readOnly = true)
    fun checkScopeWithUser(userId: Long): TaskScope {
        val myId = ContextUtil.getCurrentUserId()
        if (myId == userId) {
            // 내 아이디 -> PUBLIC, PRIVATE, FOLLOWER 다 줘도 됨.
            return TaskScope.PRIVATE
        }

        val me = userRepository.findById(myId).orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        val target = userRepository.findById(userId).orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }

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
        userRepository.findById(userId).orElseThrow { CustomException(ErrorCode.USER_NOT_FOUND) }
        val followersOfUser = followRepository.countFollowersByUser(userId)
        val followingsOfUser = followRepository.countFollowingsByUser(userId)

        return FollowCountResponseDto(
            userId = userId,
            followerCount = followersOfUser,
            followingCount = followingsOfUser
        )
    }
}
