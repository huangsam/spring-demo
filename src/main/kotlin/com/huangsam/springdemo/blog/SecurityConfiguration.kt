package com.huangsam.springdemo.blog

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

// Configures Spring Security for authentication (verifying identity) and authorization (checking
// permissions).
// Spring Security intercepts all requests and applies these rules before they reach controllers.
@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    // The SecurityFilterChain bean defines the overall security configuration.
    // It builds a filter chain that processes every HTTP request through Spring Security's security
    // filters.
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        // authorizeHttpRequests() sets up rules for which endpoints require authentication.
        // Rules are checked in order; the first matching rule wins.
        http
            .authorizeHttpRequests { auth ->
                auth
                    // These endpoints are publicly accessible (permitAll)
                    .requestMatchers(
                        "/",
                        "/error",
                        "/article/**",
                        "/category/**",
                        "/tag/**",
                        "/user/**",
                        "/api/article/**",
                        "/api/user/**",
                        "/hello",
                        "/style.css",
                        "/register",
                        "/api/user/register",
                        "/rss",
                        "/atom",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/actuator/**",
                    )
                    .permitAll()
                    // Role-based authorization: admin endpoints require ADMIN role
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    // Other endpoints require user to be authenticated (any valid role)
                    .requestMatchers("/new-article")
                    .authenticated()
                    // Everything else also requires authentication
                    .anyRequest()
                    .authenticated()
            }
            .formLogin { form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll() }
            .logout { logout -> logout.permitAll() }
            .csrf { csrf ->
                // CSRF support is important for form submissions, but for a
                // simple demo app with only REST endpoints and no real session
                // state it's easier to disable it; keep in mind this is not a
                // best practice in real applications.
                csrf.disable()
            }

        return http.build()
    }

    // UserDetailsService is Spring Security's interface for loading user information from storage.
    // During login, Spring Security calls this to fetch the user's password and roles.
    @Bean
    fun userDetailsService(userRepository: UserRepository): UserDetailsService {
        return UserDetailsService { login ->
            val user =
                userRepository.findByLogin(login)
                    ?: throw UsernameNotFoundException("User not found: $login")

            // Build a Spring Security User object with credentials and roles for authentication
            org.springframework.security.core.userdetails.User.withUsername(user.login)
                .password(user.password)
                .roles(user.role)
                .build()
        }
    }

    // PasswordEncoder implements a hashing algorithm (BCrypt) to store passwords securely.
    // BCrypt is slow by design to make brute-force attacks computationally expensive.
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
