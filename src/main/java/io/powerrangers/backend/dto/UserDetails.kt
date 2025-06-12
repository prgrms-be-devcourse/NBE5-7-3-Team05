package io.powerrangers.backend.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class UserDetails (
    var id: Long? = null,
    var role: Role? = null,
    val name: String,
    val email: String,
    val providerId: String,
    val profileImage: String,
    val attributes: MutableMap<String, Any?> = mutableMapOf()
) : OAuth2User {

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(role?.name))
    }

    override fun getName(): String = name

    override fun getAttributes(): Map<String, Any?> = attributes
}
