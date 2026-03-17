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

    /**
     * Cria a cadeia de filtros de segurança para autenticar as requisições HTTP.
     * A cadeia de filtros adiciona o filtro de autenticação JWT antes do filtro de autenticação de usuario e senha.
     * Todos os endpoints listados abaixo não precisam de autenticação.
     * Os endpoints que não estão listados abaixo precisam de autenticação.
     * @return a cadeia de filtros de segurança configurada para autenticar as requisições HTTP.
     */
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
                    "/auth/loginWithGoogle",
                    "/auth/loginWithGoogle/",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/motoristas/cadastro/",
                    "/motoristas/cadastro",
                    "/notificacoes/stream/",
                    "/notificacoes/stream"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Providencia uma fonte de configuração CORS para a aplicação (CorsConfigurationSource).
     * Permite origens especificadas na propriedade "app.cors.allowed-origins" do arquivo de configuração da aplicação.
     * Permite os seguintes métodos: GET, PATCH, POST, PUT, DELETE, OPTIONS.
     * Permite os seguintes headers: Authorization, Content-Type, Accept, Cache-Control, Last-Event-ID.
     * Também permite o envio de cookies e outras credenciais nas requisições CORS, o que é importante para autenticação de sessões.
     */
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

    /**
     * Retorna o gerenciador de autenticação da API.
     * Este gerenciador é responsável por verificar se o usuário e senha informados na requisição
     * HTTP são válidos e carregar o usuário autenticado no contexto de segurança da API.
     * Se a autenticação for bem sucedida, coloca o usuário autenticado no contexto de segurança da API.
     * Se a autenticação falhar, lança uma exceção de tipo AuthenticationException.
     * 
     * @param config a configuração de autenticação da API
     * @return o gerenciador de autenticação da API
     * @throws Exception se ocorrer um erro durante a configuração do gerenciador de autenticação
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
  
    /**
     * Retorna um gerador de hash para senhas na API.
     * Este gerador utiliza o algoritmo de hash BCrypt para gerar senhas.
     * 
     * @return o gerador de hash para senhas da API
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}

