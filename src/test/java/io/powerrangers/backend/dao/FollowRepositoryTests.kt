package io.powerrangers.backend.dao

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.powerrangers.backend.entity.Follow
import io.powerrangers.backend.utils.genUser
import io.powerrangers.backend.utils.genUserList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

private val log = KotlinLogging.logger {}

@DataJpaTest
class FollowRepositoryTests @Autowired constructor(
    var userRepository: UserRepository,
    var followRepository: FollowRepository
) {

    @Test
    fun `repository 주입 테스트`() {

        followRepository shouldNotBe null
        userRepository shouldNotBe null

    }
    
    @Test
    fun `A 팔로워가 특정 사용자를 팔로우 할 경우, 팔로우 관계가 존재하기 때문에 true를 반환한다 하지만, 특정 사용자는 A를 팔로우하지 않기 때문에 false를 반환한다`() {

        val userList = genUserList(2)
        userRepository.saveAll(userList)

        val follower = userList[0]
        val following = userList[1]

        followRepository.save(
            Follow(
                follower = follower,
                following = following)
        )

        val exists = followRepository.existsByFollowerAndFollowing(follower, following)
        val notExist = followRepository.existsByFollowerAndFollowing(following, follower)

        exists shouldBe true
        notExist shouldBe false
    }

    @Test
    fun `팔로워가 특정 사용자를 팔로우 하지 않는 경우, 팔로우 관계가 존재하지 않기 때문에 false를 반환한다`() {

        val userList = genUserList(2)
        userRepository.saveAll(userList)

        val guest1 = userList[0]
        val guest2 = userList[1]

        val notExist1 = followRepository.existsByFollowerAndFollowing(guest1, guest2)
        notExist1 shouldBe false

        val notExist2 = followRepository.existsByFollowerAndFollowing(guest2, guest1)
        notExist2 shouldBe false
    }

    @Test
    fun `팔로우 관계가 있을 때는, Follow 객체를 반환한다 (단방향일 때는 한 쪽만 Follow 객체, 나머지는 Null 반환)`() {

        val userList = genUserList(2)
        userRepository.saveAll(userList)

        val follower = userList[0]
        val following = userList[1]

        followRepository.save(
            Follow(
                follower = follower,
                following = following
            )
        )

        val findFollow = followRepository.findByFollowerAndFollowing(follower, following)

        findFollow shouldNotBe null
        findFollow!!.follower.nickname shouldBe follower.nickname
        findFollow!!.follower.email shouldBe follower.email

        findFollow!!.following.nickname shouldBe following.nickname
        findFollow!!.following.email shouldBe following.email

        val notFollow = followRepository.findByFollowerAndFollowing(following, follower)

        notFollow shouldBe null
    }

    @Test
    fun `팔로우 관계가 없을 때는, null을 반환한다`() {

        val userList = genUserList(2)
        userRepository.saveAll(userList)

        val guest1 = userList[0]
        val guest2 = userList[1]

        val findFollow = followRepository.findByFollowerAndFollowing(guest1, guest2)
        findFollow shouldBe null

    }

    @Test
    fun `userId로 사용자의 팔로워 목록을 조회하면 리스트에 User가 담겨 반환된다`() {

        val myNickname = "me"
        val myEmail = "me@gmail.com"
        val me = genUser(nickname = myNickname, email = myEmail)
        userRepository.save(me)

        val size = 10
        val userList = genUserList(size)
        userRepository.saveAll(userList)

        val followList : MutableList<Follow> = mutableListOf()

        followList.addAll(
            userList.map { target ->
                Follow(follower = target, following = me)
            }
        )

        followRepository.saveAll(followList)

        val followerList = followRepository.findFollowersByUser(me.id!!)
        followerList.size shouldBe size

        followerList.forEachIndexed { idx, follower ->
            val expected = userList[idx]
            follower.id shouldBe expected.id
            follower.nickname shouldBe expected.nickname
            follower.email shouldBe expected.email
        }

        val allFollows = followRepository.findAll()
        allFollows.forEach {
            it.following.id shouldBe me.id
            userList.map { u -> u.id } shouldContain it.follower.id
        }
    }

    @Test
    fun `userId로 사용자의 팔로잉 목록을 조회하면 리스트에 User가 담겨 반환된다`() {

        val myNickname = "me"
        val myEmail = "me@gmail.com"
        val me = genUser(nickname = myNickname, email = myEmail)
        userRepository.save(me)

        val size = 10
        val userList = genUserList(size)
        userRepository.saveAll(userList)

        val followList : MutableList<Follow> = mutableListOf()

        followList.addAll(
            userList.map { target ->
                Follow(follower = me, following = target)
            }
        )

        followRepository.saveAll(followList)

        val followingList = followRepository.findFollowingsByUser(me.id!!)
        followingList.size shouldBe size

        followingList.forEachIndexed { idx, following ->
            val expected = userList[idx]

            following.id shouldBe expected.id
            following.nickname shouldBe expected.nickname
            following.email shouldBe expected.email
        }

        val allFollows = followRepository.findAll()
        allFollows.forEach {
            it.follower.id shouldBe me.id
            userList.map { u -> u.id } shouldContain it.following.id
        }

    }

    @Test
    fun `유저의 userId로 해당 유저의 팔로워 수를 조회하면 유저의 팔로워 수가 Long으로 조회된다`() {

        val myNickname = "me"
        val myEmail = "me@gmail.com"
        val me = genUser(nickname = myNickname, email = myEmail)
        userRepository.save(me)

        val size = 10
        val userList = genUserList(size)
        userRepository.saveAll(userList)

        val followList : MutableList<Follow> = mutableListOf()

        followList.addAll(
            userList.map { target ->
                Follow(follower = target, following = me)
            }
        )

        followRepository.saveAll(followList)

        val followersCount = followRepository.countFollowersByUser(me.id!!)

        followersCount shouldBe size.toLong()

    }

    @Test
    fun `유저의 userId로 해당 유저의 팔로잉 수를 조회하면 유저의 팔로워 수가 Long으로 조회된다`() {

        val myNickname = "me"
        val myEmail = "me@gmail.com"
        val me = genUser(nickname = myNickname, email = myEmail)
        userRepository.save(me)

        val size = 10
        val userList = genUserList(size)
        userRepository.saveAll(userList)

        val followList : MutableList<Follow> = mutableListOf()

        followList.addAll(
            userList.map { target ->
                Follow(follower = me, following = target)
            }
        )

        followRepository.saveAll(followList)

        val followingsCount = followRepository.countFollowingsByUser(me.id!!)

        followingsCount shouldBe size.toLong()

    }
}