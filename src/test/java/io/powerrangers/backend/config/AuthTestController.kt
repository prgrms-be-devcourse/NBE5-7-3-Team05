package io.powerrangers.backend.config

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthTestController {
    @GetMapping("/security-endpoint")
    fun securityEndpoint(): String {
        return "This is a security endpoint!"
    }

    @GetMapping("/test/url")
    fun testUrl(): String {
        return "This is a public endpoint"
    }
}
