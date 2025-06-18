package io.powerrangers.backend.entity

import io.powerrangers.backend.dto.Role
import jakarta.persistence.*

@Entity
@Table(name = "`user`")
class User (

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @field:Column(
        length = 20,
        unique = true
    ) var nickname: String,

    @field:Column(
        name = "profile_image"
    ) var profileImage: String? = null,

    @field:Column(
        length = 10,
    ) val provider: String,

    @field:Column(
        name = "provider_id",
        length = 50,
    ) val providerId: String,

    @field:Column(
        length = 40,
        unique = true
    ) val email: String,

    var intro: String? = null,

    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.REMOVE])
    val tasks: MutableList<Task> = mutableListOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.REMOVE])
    val comments: MutableList<Comment> = mutableListOf(),

    @OneToMany(mappedBy = "follower", cascade = [CascadeType.REMOVE])
    val followers: MutableList<Follow> = mutableListOf(),

    @OneToMany(mappedBy = "following", cascade = [CascadeType.REMOVE])
    val followings: MutableList<Follow> = mutableListOf(),

) : BaseEntity()
