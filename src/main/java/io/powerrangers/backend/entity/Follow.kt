package io.powerrangers.backend.entity

import jakarta.persistence.*

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["follower_id", "following_id"])])
class Follow (
    @Id
    @Column(name = "follow_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:JoinColumn(name = "follower_id", nullable = false)
    @field:ManyToOne(fetch = FetchType.LAZY)
    val follower: User,

    @field:JoinColumn(name = "following_id", nullable = false)
    @field:ManyToOne(fetch = FetchType.LAZY)
    val following: User
)
