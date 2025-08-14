package com.example.swp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll() // Cho phép tất cả request
                )
                .csrf(csrf -> csrf.disable()) // Tắt CSRF nếu cần test API
                .formLogin(form -> form.disable()) // Tắt form login
                .logout(logout -> logout.disable()); // Tắt logout

        return http.build();
    }
}
