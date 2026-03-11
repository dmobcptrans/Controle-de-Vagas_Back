package com.cptrans.petrocarga.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(withDefaults -> withDefaults.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                 .requestMatchers(
                    "/auth/login/",
                    "/auth/login",
                    "/auth/resend-code",
                    "/auth/resend-code/",
                    "/auth/activate",
                    "/auth/activate/",
                    "/auth/forgot-password",
                    "/auth/forgot-password/",
                    "/auth/reset-password",
                    "/auth/reset-password/",
                    "/auth/admin/",
                    "/auth/admin",
                    "/auth/me/",
                    "/auth/me",
                    "auth/loginWithGoogle",
                    "auth/loginWithGoogle/",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/motoristas/cadastro/",
                    "/motoristas/cadastro",
                    "/notificacoes/stream/",
                    "/notificacoes/stream",
                    "/api/test/**"  // Endpoints de teste de email (remover em produção)
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(allowedOrigins);
        corsConfig.setAllowedMethods(java.util.List.of("GET", "PATCH", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(java.util.List.of( 
            "Authorization",
            "Content-Type",
            "Accept",
            "Cache-Control",
            "Last-Event-ID"));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
}

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
  
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}

