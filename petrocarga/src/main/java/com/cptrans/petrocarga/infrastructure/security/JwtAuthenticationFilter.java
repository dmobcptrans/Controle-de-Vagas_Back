package com.cptrans.petrocarga.infrastructure.security;

import java.io.IOException;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
    * Filtra as requisições HTTP para verificar se o token de autenticação (Bearer) foi informado na requisição.
     * Se o token for informado e for válido, carrega o usuário autenticado com base no email do token e
     * coloca o usuário autenticado no contexto de segurança da API.
     * Se não houver token de autenticação na requisição, apenas permite a requisição passar se a
     * requisição for uma das seguintes URLs: /auth/login, /auth/login/, /auth/resend-code,
     * /auth/resend-code/, /auth/activate, /auth/activate/, /auth/forgot-password,
     * /auth/forgot-password/, /auth/reset-password, /auth/reset-password/, /auth/admin/, /auth/admin,
     * /auth/me/, /auth/me, /swagger-ui/**, /swagger-ui.html, /v3/api-docs/**, /v3/api-docs.yaml,
     * /motoristas/cadastro/, /motoristas/cadastro, /notificacoes/stream/, /notificacoes/stream.
     * Se a requisição for qualquer outra URL, não permite a requisição passar e limpa o contexto de segurança da API.
     *
     * @param request a requisição HTTP
     * @param response a resposta HTTP
     * @param filterChain a cadeia de filtros
     * @throws ServletException se ocorrer um erro durante o processamento da requisição
     * @throws IOException se ocorrer um erro durante a leitura da requisição ou escrita da resposta
     * 
     */
    @Override
    protected void doFilterInternal(
            @NonNull
            HttpServletRequest request,
            @NonNull
            HttpServletResponse response,
            @NonNull
            FilterChain filterChain) throws ServletException, IOException {

        try{
             final String token = resolveToken(request);
        
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                final String email = jwtService.getEmailDoToken(token);
                if(email != null && !email.isEmpty() && jwtService.validarToken(token)){
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                    UUID id = jwtService.getIdDoToken(token);
                    UserAuthenticated userAuthenticated = new UserAuthenticated(id, userDetails);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userAuthenticated, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
    /**
     * Resolve o token de autenticação a partir da requisição.
     * O token pode ser informado via header "Authorization" com o valor "Bearer <token>" ou via cookie com o nome "auth-token".
     * @param request a requisição HTTP
     * @return o token de autenticação da requisição, ou null caso o token não seja encontrado
     */
    private String resolveToken(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("auth-token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Retorna false para garantir que o filtro seja aplicado a dispatches assíncronos.
     * Isso é necessário para garantir que o filtro seja aplicado a requisições assíncronas, como aquelas feitas por SseEmitter.
     * Se este método retornar true, o filtro não será aplicado a dispatches assíncronos, o que pode causar problemas de autenticação para requisições assíncronas.
     * Ao retornar false, garantimos que o filtro seja aplicado a todas as requisições
     * 
     * @return false
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    /** 
     * Retorna false para garantir que o filtro seja aplicado a dispatches de erro.
     * Se este método retornar true, o filtro não será aplicado a dispatches de erro, o que pode causar problemas de autenticação para requisições de erro.
     * Ao retornar false, garantimos que o filtro seja aplicado a todas as requisições, incluindo dispatches de erro.
    */
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
