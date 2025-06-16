package io.powerrangers.backend.dao

import io.powerrangers.backend.dto.TaskScope
import io.powerrangers.backend.entity.Comment
import io.powerrangers.backend.entity.Task
import io.powerrangers.backend.entity.User
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDateTime
import kotlin.test.Test

@DataJpaTest
class CommentRepositoryTests constructor(
    @Autowired
    val commentRepository: CommentRepository,
    @Autowired
    val userRepository: UserRepository,
    @Autowired
    val taskRepository: TaskRepository
){
    @PersistenceContext
    lateinit var em: EntityManager
    lateinit var savedUser: User
    lateinit var savedTask: Task

    @BeforeEach
    fun setUp(){
        savedUser = userRepository.save(createUser())
        savedTask = taskRepository.save(createTask(savedUser))
    }

    @Test
    fun `댓글 저장 및 조회 성공테스트`() {
        val comment = Comment(
            task = savedTask,
            user = savedUser,
            content = "잘 주무셨어요?"
        )

        val savedComment = commentRepository.save(comment)
        val found = commentRepository.findById(savedComment.id!!)

        Assertions.assertThat(found).isPresent
        Assertions.assertThat(found.get().content).isEqualTo("잘 주무셨어요?")
        Assertions.assertThat(found.get().user.nickname).isEqualTo("taeho4523")
    }

    @Test
    fun `최상위 부모 댓글 저장 및 조회 성공테스트`() {
        val comment = Comment(
            task = savedTask,
            user = savedUser,
            content = "부모댓글이에요",
            parent = null
        )
        val savedComment = commentRepository.save(comment)
        val found = commentRepository.findById(savedComment.id!!)

        Assertions.assertThat(found).isPresent
        Assertions.assertThat(found.get().content).isEqualTo("부모댓글이에요")
        Assertions.assertThat(found.get().parent).isNull()
    }

    @Test
    fun `자식 댓글 저장 및 조회 성공테스트`() {
        val parent = Comment(
            task = savedTask,
            user = savedUser,
            content = "부모댓글이에요",
            parent = null
        )
        val parentComment = commentRepository.save(parent)

        val child = Comment(
            task = savedTask,
            user = savedUser,
            content = "자식댓글이에요",
            parent = parentComment
        )
        val childComment = commentRepository.save(child)
        val found = commentRepository.findById(childComment.id!!)

        Assertions.assertThat(found).isPresent
        Assertions.assertThat(found.get().content).isEqualTo("자식댓글이에요")
        Assertions.assertThat(found.get().parent?.id).isEqualTo(parentComment.id)

    }



    @Test
    fun `해당 Task에 대한 댓글 목록 조회 성공테스트`() {
        val comment1 = commentRepository.save(
            Comment(task = savedTask, user = savedUser, content = "첫 댓글")
        )
        val comment2 = commentRepository.save(
            Comment(task = savedTask, user = savedUser, content = "두 번째 댓글")
        )
        val comments = commentRepository.findAll()
            .filter { it.task.id == savedTask.id && it.parent == null }

        Assertions.assertThat(comments).hasSize(2)
        Assertions.assertThat(comments.map { it.content }).contains("첫 댓글", "두 번째 댓글")
    }

    @Transactional
    @Test
    fun `부모 ID로 자식댓글 조회 성공테스트`() {
        val parent=commentRepository.save(
            Comment(
                task = savedTask,
                user = savedUser,
                content = "부모댓글이에요",
                parent = null
            )
        )
        val child1=commentRepository.save(
            Comment(
                task = savedTask,
                user = savedUser,
                content = "자식댓글1이에요",
                parent = parent
            )
        )
        val child2=commentRepository.save(
            Comment(
                task = savedTask,
                user = savedUser,
                content = "자식댓글2에요",
                parent = parent
            )
        )

        em.flush()
        em.clear()

        val foundParent=commentRepository.findById(parent.id!!).get()
        val children = foundParent.children.toList()



        Assertions.assertThat(children).hasSize(2)
        Assertions.assertThat(children.map { it.content }).contains("자식댓글1이에요","자식댓글2에요")

    }

    @Test
    fun `findByTaskId 테스트`(){
        val comment1 = commentRepository.save(
            Comment(task = savedTask, user = savedUser, content = "첫 번째 댓글")
        )
        val comment2 = commentRepository.save(
            Comment(task = savedTask, user = savedUser, content = "두 번째 댓글")
        )

        val result = commentRepository.findByTaskId(savedTask.id!!)

        Assertions.assertThat(result).hasSize(2)
        Assertions.assertThat(result.map { it?.content }).contains("첫 번째 댓글", "두 번째 댓글")
        Assertions.assertThat(result.all { it!!.user.id == savedUser.id }).isTrue()
    }


    @Transactional
    @Test
    fun `댓글 삭제 시 자식댓글도 함께 삭제 성공테스트`() {
        val parent = commentRepository.save(Comment(task = savedTask, user = savedUser, content = "부모댓글이에요"))
        val child = commentRepository.save(
            Comment(
                task = savedTask,
                user = savedUser,
                content = "자식댓글이에요",
                parent = parent
            )
        )

        em.flush()
        em.clear()

        commentRepository.deleteById(parent.id!!)
        em.flush()
        em.clear()

        val remain=commentRepository.findAll()
        Assertions.assertThat(remain).isEmpty()
    }


    @Test
    fun `존재하지 않는 부모 ID로 자식댓글 저장시 실패테스트`() {
        val fakeParent = Comment(task = savedTask, user = savedUser, content = "존재하지 않는 부모")
        val detachedParent= em.merge(fakeParent)
        em.remove(detachedParent)

        val child = Comment(task = savedTask, user = savedUser, content = "자식 댓글", parent = detachedParent)
        Assertions.assertThatThrownBy {
            commentRepository.save(child)
            em.flush()
        }.isInstanceOf(IllegalStateException::class.java)
    }

    @Test
    fun `존재하지 않는 댓글 ID로 삭제 시 예외 없이 통과`() {
        val nonExistentId = 9999L

        // deleteById는 존재하지 않아도 예외 안 발생
        commentRepository.deleteById(nonExistentId)

        // 검증: 전체 댓글 수 그대로 유지됨
        val all = commentRepository.findAll()
        Assertions.assertThat(all).hasSize(0)
    }


    private fun createTask(user: User): Task {
        return Task(
            category = "꿀잠자기",
            content = "기가막히게자기",
            dueDate = LocalDateTime.now().plusDays(2),
            scope = TaskScope.PUBLIC,
            user = user
        )
    }

    private fun createUser(): User {
        return User(
            nickname = "taeho4523",
            provider = "kakao",
            providerId = "taeho4523@kakao.com",
            email = "taeho4523@gmail.com"
        )
    }
}