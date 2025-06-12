package io.powerrangers.backend.service

import io.powerrangers.backend.dao.TokenRepository
import io.powerrangers.backend.dao.UserRepository
import io.powerrangers.backend.dto.Role
import io.powerrangers.backend.dto.TaskResponseDto
import io.powerrangers.backend.dto.UserGetProfileResponseDto
import io.powerrangers.backend.dto.UserUpdateProfileRequestDto
import io.powerrangers.backend.entity.User
import io.powerrangers.backend.exception.AuthTokenException
import io.powerrangers.backend.exception.CustomException
import io.powerrangers.backend.exception.ErrorCode
import io.powerrangers.backend.utils.getCurrentUserId
import io.powerrangers.backend.utils.toProfileResponseDto
import org.springframework.data.repository.findByIdOrNull
import io.powerrangers.backend.utils.toTaskResponseDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@Service
class UserService(
    private val userRepository: UserRepository,
    private val refreshTokenRepositoryAdapter: TokenRepository,
    private val jwtProvider: JwtProvider,
    private val s3Service: S3Service,
    private val taskService: TaskService
) {

    @Transactional(readOnly = true)
    fun checkNicknameDuplication(nickname: String): Boolean =
        userRepository.existsByNickname(nickname)

    fun identified(userId: Long?): Boolean =
        getCurrentUserId() == userId

    @Transactional(readOnly = true)
    fun getUserProfile(userId: Long): UserGetProfileResponseDto {
        val user = userRepository.findByIdOrNull(userId) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)
        return user.toProfileResponseDto()
    }

    @Transactional(readOnly = true)
    fun getTasksByUser(userId: Long, date: LocalDate): List<TaskResponseDto> {
        return taskService.getTasksByScope(userId)
            .filter { it.dueDate.toLocalDate() == date }
            .map { it.toTaskResponseDto() }
    }

    @Transactional(readOnly = true)
    fun searchUserProfile(nickname: String): List<UserGetProfileResponseDto> {
        val users = userRepository.findByNickname(nickname.trim())
        return users.map { it.toProfileResponseDto() }
    }

    @Transactional
    fun updateUserProfile(userId: Long, request: UserUpdateProfileRequestDto, image: MultipartFile?) {
        val user = userRepository.findByIdOrNull(userId) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        if (!identified(userId)) {
            throw CustomException(ErrorCode.NOT_THE_OWNER)
        }

        if (user.nickname != request.nickname && checkNicknameDuplication(request.nickname)) {
            throw CustomException(ErrorCode.DUPLICATED_NICKNAME)
        }

        user.nickname = request.nickname
        user.intro = request.intro
        updateUserProfileImage(user, image, request.profileImage)
    }

    private fun updateUserProfileImage(user: User, image: MultipartFile?, profileImage: String?) {
        val existingImageUrl = user.profileImage

        if ((image == null || image.isEmpty) && !profileImage.isNullOrBlank()) {
            return // 이미지 유지
        }

        if ((image == null || image.isEmpty) && profileImage.isNullOrBlank()) {
            existingImageUrl?.takeIf { it.isNotBlank() }?.let { s3Service.delete(it) }
            user.profileImage = null
            return
        }

        if (image != null && !image.isEmpty) {
            if (!image.contentType.startsWith("image/")) {
                throw CustomException(ErrorCode.UNSUPPORTED_RESOURCE)
            }

            existingImageUrl?.takeIf { it.isNotBlank() }?.let { s3Service.delete(it) }

            val uploadedImageUrl = s3Service.upload(image)
            user.profileImage = uploadedImageUrl
        }
    }

    @Transactional
    fun cancelAccount(userId: Long) {
        if (!identified(userId)) {
            throw CustomException(ErrorCode.NOT_THE_OWNER)
        }

        userRepository.findByIdOrNull(userId) ?: throw CustomException(ErrorCode.USER_NOT_FOUND)

        findRefreshTokenAndAddToBlackList(userId)

        val refreshTokens = refreshTokenRepositoryAdapter.findAllRefreshTokensByUserId(userId)
        refreshTokens.forEach { it.user = null }

        userRepository.deleteById(userId)
    }

    @Transactional
    fun logout() {
        val userId = getCurrentUserId()
        findRefreshTokenAndAddToBlackList(userId)
    }

    private fun findRefreshTokenAndAddToBlackList(userId: Long) {
        val refreshToken = refreshTokenRepositoryAdapter.findValidRefreshToken(userId)
            ?: throw AuthTokenException(ErrorCode.UNAUTHORIZED)

        val refreshTokenValue = refreshToken.refreshToken

        if (!refreshTokenRepositoryAdapter.tokenBlackList(refreshTokenValue)) {
            refreshTokenRepositoryAdapter.addBlackList(refreshToken)
        }
    }

    @Transactional(readOnly = true)
    fun reissueAccessToken(refreshTokenValue: String): String {
        if (!jwtProvider.validateToken(refreshTokenValue)) {
            throw AuthTokenException(ErrorCode.UNAUTHORIZED)
        }

        val tokenBody = jwtProvider.parseToken(refreshTokenValue)
        val userId = tokenBody.userId
        val role = Role.valueOf(tokenBody.role)

        val refreshToken = refreshTokenRepositoryAdapter.findValidRefreshToken(userId)
            ?: throw AuthTokenException(ErrorCode.UNAUTHORIZED)

        if (refreshTokenValue != refreshToken.refreshToken) {
            throw AuthTokenException(ErrorCode.UNAUTHORIZED)
        }

        return jwtProvider.issueAccessToken(userId, role)
    }
}
