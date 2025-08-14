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
                .requestMatchers("/", "/SWP", "/SWP/customers", "/home-page", "/css/**", "/js/**", "/img/**", "/fonts/**", "/lib/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // Tạm thời disable CSRF để test
//                  .authorizeHttpRequests(auth -> auth
//                .anyRequest().permitAll()    // cho phép tất cả request
//        )
//                .csrf(csrf -> csrf.disable())    // tắt CSRF
//                .httpBasic(httpBasic -> {})      // thêm nếu muốn dùng basic auth (có thể bỏ)
//                .formLogin(form -> form.disable()) // tắt login form
//                .logout(logout -> logout.disable()); // tắt logout
        return http.build();
    }
}

