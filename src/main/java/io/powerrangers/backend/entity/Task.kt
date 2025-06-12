package io.powerrangers.backend.entity

import io.powerrangers.backend.dto.TaskScope
import io.powerrangers.backend.dto.TaskStatus
import io.powerrangers.backend.dto.TaskUpdateRequestDto
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Task(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    val id: Long? = null,

    @Column(length = 10, nullable = false)
    var category: String,

    @Column(length = 50, nullable = false)
    var content: String,

    @Column(name = "due_date")
    var dueDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TaskStatus = TaskStatus.INCOMPLETE,

    @Column(name = "task_image")
    var taskImage: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var scope: TaskScope,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @OneToMany(mappedBy = "task", cascade = [CascadeType.REMOVE])
    val comments: List<Comment> = mutableListOf()

) : BaseEntity() {

    fun updateFrom(dto: TaskUpdateRequestDto) {
        this.category = dto.category
        this.content = dto.content
        this.scope = dto.scope
    }
}

