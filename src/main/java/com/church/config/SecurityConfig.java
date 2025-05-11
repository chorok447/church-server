package com.church.config;

import com.church.security.JwtUtil;
import com.church.security.JwtAuthorizationFilter;
import com.church.service.AdminDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;


import java.util.Arrays;

@Configuration
public class SecurityConfig {
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // src/main/java/com/church/config/SecurityConfig.java

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtil jwtUtil, AdminDetailsService adminDetailsService) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/notices/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/notices").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/notices/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/notices/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/sermons/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/sermons").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/sermons/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/sermons/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/admin/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/admin/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/admin/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/admin/**").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthorizationFilter(jwtUtil, adminDetailsService), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}