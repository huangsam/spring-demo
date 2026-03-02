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

@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/", "/article/**", "/api/article/**", "/api/user/**", "/hello", "/style.css", "/register", "/api/user/register").permitAll()
                    .requestMatchers("/new-article").authenticated()
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form
                    .loginPage("/login")
                    .permitAll()
            }
            .logout { logout ->
                logout.permitAll()
            }
            .csrf { csrf ->
                csrf.disable() // Disabling CSRF for simplicity in this demo, though not recommended for production
            }

        return http.build()
    }

    @Bean
    fun userDetailsService(userRepository: UserRepository): UserDetailsService {
        return UserDetailsService { login ->
            val user = userRepository.findByLogin(login)
                ?: throw UsernameNotFoundException("User not found: $login")

            org.springframework.security.core.userdetails.User
                .withUsername(user.login)
                .password(user.password)
                .roles(user.role)
                .build()
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
