package io.powerrangers.backend.controller

import io.powerrangers.backend.dto.*
import io.powerrangers.backend.service.FollowService
import io.powerrangers.backend.service.UserService
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/follow")
class FollowController(
    private val followService: FollowService
) {

    @PostMapping
    fun follow(@RequestBody followRequestDto: @Valid FollowRequestDto): ResponseEntity<BaseResponse<FollowResponseDto>> {
        return BaseResponse.success(HttpStatus.CREATED, followService.follow(followRequestDto))
    }

    @DeleteMapping("/{followingId}")
    fun unfollow(@PathVariable followingId: Long): ResponseEntity<BaseResponse<Void>> {
        followService.unfollow(followingId)
        return BaseResponse.success(HttpStatus.NO_CONTENT)
    }

    @GetMapping("/{userId}/followers")
    fun getFollowers(@PathVariable userId: Long): ResponseEntity<BaseResponse<List<UserFollowResponseDto>>> {
        return BaseResponse.success(HttpStatus.OK, followService.findFollowers(userId))
    }

    @GetMapping("/{userId}/followings")
    fun getFollowings(@PathVariable userId: Long): ResponseEntity<BaseResponse<List<UserFollowResponseDto>>> {
        return BaseResponse.success(HttpStatus.OK, followService.findFollowings(userId))
    }

    @GetMapping("/{userId}")
    fun getFollowCount(@PathVariable userId: Long): ResponseEntity<BaseResponse<FollowCountResponseDto>> {
        return BaseResponse.success(HttpStatus.OK, followService.getFollowCount(userId))
    }

    @GetMapping("/check")
    fun checkFollowingRelationship(@RequestParam userId: Long): ResponseEntity<BaseResponse<FollowCheckResponseDto>> {
        return BaseResponse.success(HttpStatus.OK, followService.checkFollowingRelationship(userId))
    }
}
