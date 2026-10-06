package com.ice.music.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin) // For H2 console
                .contentTypeOptions(HeadersConfigurer.ContentTypeOptionsConfig::disable)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public read-only endpoints
                .requestMatchers(HttpMethod.GET, "/api/v1/featured/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/catalogue/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/artists/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/tracks/**").permitAll()
                
                // Public Auth & Documentation
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()

                // Editor role required for mutating catalogue
                .requestMatchers(HttpMethod.POST, "/api/v1/artists/**").hasAnyAuthority("ROLE_EDITOR", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/artists/**").hasAnyAuthority("ROLE_EDITOR", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/artists/**").hasAnyAuthority("ROLE_EDITOR", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/artists/**").hasAnyAuthority("ROLE_EDITOR", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/tracks/**").hasAnyAuthority("ROLE_EDITOR", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/tracks/**").hasAnyAuthority("ROLE_EDITOR", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/tracks/**").hasAnyAuthority("ROLE_EDITOR", "ROLE_ADMIN")

                // Any other request authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "X-Correlation-ID"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
