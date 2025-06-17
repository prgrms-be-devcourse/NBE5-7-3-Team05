package io.powerrangers.backend.entity

import jakarta.persistence.*
import lombok.AccessLevel
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Table(
    name = "refresh_token_black_list",
    indexes = [Index(name = "idx_refresh_token_black_list_created_at", columnList = "createdAt")]
)
@EntityListeners(AuditingEntityListener::class)
class RefreshTokenBlackList (
    @Id
    @Column(name = "refresh_token_black_list_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne
    var refreshToken: RefreshToken,
) {
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    var createdAt: LocalDateTime? = null
}
