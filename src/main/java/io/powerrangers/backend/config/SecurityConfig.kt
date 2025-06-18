package io.powerrangers.backend.config

import io.powerrangers.backend.exception.CustomOAuth2AuthenticationFailureHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oauth2SuccessHandler: Oauth2SuccessHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customOAuth2AuthenticationFailureHandler: CustomOAuth2AuthenticationFailureHandler,
) {
    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain? {
        return http
            .httpBasic{ it.disable() }
            .csrf{ it.disable() }
            .cors{ it.disable() }
            .formLogin{ it.disable() }
            .sessionManagement(
                Customizer {
                    it.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                    )
                }
            )
            .oauth2Login(Customizer {
                it
                    .loginPage("/loginPage")
                    .successHandler(oauth2SuccessHandler)
                    .failureHandler(customOAuth2AuthenticationFailureHandler)
            })
            .authorizeHttpRequests(Customizer {
                it
                        .requestMatchers("/admin/**")
                            .hasAuthority("ADMIN")
                        .requestMatchers(
                            "/test/**",
                            "/index.html",
                            "/favicon.ico",
                            "/css/**",
                            "/js/**",
                            "/users/reissue",
                            "/loginPage",
                            "/images/**",
                            "/fonts/**",
                            "/search.html",
                            "/follow-list.html",
                            "/actuator/prometheus",// prometheus metric 데이터 수집할 수 있도록 허용
                            "/api/notifications/subscribe"
                        )
                            .permitAll()
                        .anyRequest()
                            .authenticated()
                }
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun defaultOAuth2UserService(): DefaultOAuth2UserService {
        return DefaultOAuth2UserService()
    }
}
