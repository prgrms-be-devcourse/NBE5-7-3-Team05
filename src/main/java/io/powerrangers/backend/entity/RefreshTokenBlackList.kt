package io.powerrangers.backend.entity

import jakarta.persistence.*
import lombok.AccessLevel
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor

@Entity
class RefreshTokenBlackList (
    @Id
    @Column(name = "refresh_token__black_list_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne
    var refreshToken: RefreshToken
)
