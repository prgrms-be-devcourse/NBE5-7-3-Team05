package io.powerrangers.backend.entity

import jakarta.persistence.*

@Entity
class RefreshToken (
    @Id
    @Column(name = "refresh_token_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    var user: User?,

    val refreshToken: String
)
