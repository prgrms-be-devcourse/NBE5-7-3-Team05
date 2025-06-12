package io.powerrangers.backend.dao

import io.powerrangers.backend.dto.TaskScope
import io.powerrangers.backend.entity.User
import io.powerrangers.backend.util.genUser
import io.powerrangers.backend.util.genTask
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDateTime

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TaskRepositoryTests @Autowired constructor(
    val taskRepository: TaskRepository,
    val userRepository: UserRepository
) {

    lateinit var user: User

    @BeforeEach
    fun setup() {
        taskRepository.deleteAll()
        userRepository.deleteAll()
        user = genUser("tester", "google", "google123", "tester@example.com")
        userRepository.save(user)
    }

    @Test
    fun `repository 주입 테스트`() {

        assertThat(taskRepository).isNotNull
        assertThat(userRepository).isNotNull

    }

    @Test
    fun `findAllByUserId는 해당 유저의 모든 할 일을 반환한다`() {

        val tasks = (1..3).map {
            genTask("work", "Task $it", LocalDateTime.now(), TaskScope.PRIVATE, user)
        }

        taskRepository.saveAll(tasks)

        val results = taskRepository.findAllByUserId(user.id!!)
        assertThat(results).hasSize(3)
    }

    @Test
    fun `findTasksForFollowers는 PRIVATE인 할 일을 제외하고 반환한다`() {

        val publicTask = taskRepository.save(
            genTask("work", "public task", LocalDateTime.now(), TaskScope.PUBLIC, user)
        )

        val followerTask = taskRepository.save(
            genTask("work", "followers task", LocalDateTime.now(), TaskScope.FOLLOWERS, user)
        )

        val privateTask = taskRepository.save(
            genTask("work", "private task", LocalDateTime.now(), TaskScope.PRIVATE, user)
        )

        val results = taskRepository.findTasksForFollowers(user.id!!)
        assertThat(results).contains(publicTask, followerTask)
        assertThat(results).doesNotContain(privateTask)

    }

    @Test
    fun `findTasksForPublic은 PUBLIC인 할 일만 반환한다`() {

        val publicTask = taskRepository.save(
            genTask("work", "public task", LocalDateTime.now(), TaskScope.PUBLIC, user)
        )

        taskRepository.save(
            genTask("work", "private task", LocalDateTime.now(), TaskScope.PRIVATE, user)
        )

        val results = taskRepository.findTasksForPublic(user.id!!)
        assertThat(results).containsOnly(publicTask)

    }

    @Test
    fun `countTasksByDateWithScope는 날짜별 할 일 개수를 반환한다`() {
        val today = LocalDateTime.now().withHour(10)
        val tomorrow = today.plusDays(1)

        // 오늘 2개 PUBLIC, 내일 1개 PUBLIC, 1개 PRIVATE
        taskRepository.save(
            genTask("work", "task 1", today, TaskScope.PUBLIC, user)
        )

        taskRepository.save(
            genTask("work", "task 2", today, TaskScope.PUBLIC, user)
        )

        taskRepository.save(
            genTask("work", "task 3", tomorrow, TaskScope.PUBLIC, user)
        )

        taskRepository.save(
            genTask("work", "task 4", today, TaskScope.PRIVATE, user)
        )

        val start = today.minusDays(1)
        val end = tomorrow.plusDays(1)

        val result = taskRepository.countTasksByDateWithScope(user.id!!, start, end, "PUBLIC", 2L)

        val dateCountMap = result.associate {
            val date = it[0] as java.sql.Date
            val count = it[1] as Long
            date.toLocalDate() to count
        }

        assertThat(dateCountMap[today.toLocalDate()]).isEqualTo(2L)
        assertThat(dateCountMap[tomorrow.toLocalDate()]).isEqualTo(1L)
    }

    @Test
    fun `countTasksByDateWithScope는 팔로워 관계가 아닐 경우 공개 범위가 PUBLIC인 할 일만 집계한다`() {
        val today = LocalDateTime.now().withHour(10)
        val start = today.minusDays(1)
        val end = today.plusDays(1)

        taskRepository.save(genTask("work", "public", today, TaskScope.PUBLIC, user))
        taskRepository.save(genTask("work", "follower", today, TaskScope.FOLLOWERS, user))
        taskRepository.save(genTask("work", "private", today, TaskScope.PRIVATE, user))

        val result = taskRepository.countTasksByDateWithScope(user.id!!, start, end, "PUBLIC", 2L)

        val count = (result[0][1] as Long)
        assertThat(count).isEqualTo(1L) // PUBLIC만 집계
    }

    @Test
    fun `countTasksByDateWithScope는 팔로워 관계에서 공개 범위가 PUBLIC, FOLLOWERS인 할 일의 수를 집계한다`() {
        val today = LocalDateTime.now().withHour(10)
        val start = today.minusDays(1)
        val end = today.plusDays(1)

        taskRepository.save(genTask("work", "public", today, TaskScope.PUBLIC, user))
        taskRepository.save(genTask("work", "follower", today, TaskScope.FOLLOWERS, user))
        taskRepository.save(genTask("work", "private", today, TaskScope.PRIVATE, user))

        val result = taskRepository.countTasksByDateWithScope(user.id!!, start, end, "FOLLOWERS", 2L)

        val count = (result[0][1] as Long)
        assertThat(count).isEqualTo(2L) // PUBLIC + FOLLOWERS
    }

    @Test
    fun `countTasksByDateWithScope는 본인이 조회할 경우 공개범위가 PRIVATE인 할 일을 포함하여 집계한다`() {
        val today = LocalDateTime.now().withHour(10)
        val start = today.minusDays(1)
        val end = today.plusDays(1)

        taskRepository.save(genTask("work", "public", today, TaskScope.PUBLIC, user))
        taskRepository.save(genTask("work", "follower", today, TaskScope.FOLLOWERS, user))
        taskRepository.save(genTask("work", "private", today, TaskScope.PRIVATE, user))

        val result = taskRepository.countTasksByDateWithScope(user.id!!, start, end, "PRIVATE", user.id!!)

        val count = (result[0][1] as Long)
        assertThat(count).isEqualTo(3L)
    }

    @Test
    fun `countTasksByDateWithScope는 본인이 아니면 공개 범위가 PRIVATE인 할 일이 집계되지 않는다`() {
        val today = LocalDateTime.now().withHour(10)
        val start = today.minusDays(1)
        val end = today.plusDays(1)

        taskRepository.save(genTask("work", "private", today, TaskScope.PRIVATE, user))

        val result = taskRepository.countTasksByDateWithScope(user.id!!, start, end, "PRIVATE", 2L)

        assertThat(result).isEmpty() // 접근 권한 없음
    }
}
