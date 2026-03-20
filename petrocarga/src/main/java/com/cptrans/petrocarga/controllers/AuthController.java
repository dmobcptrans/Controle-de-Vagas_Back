package com.cptrans.petrocarga.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.dto.AccountActivationRequest;
import com.cptrans.petrocarga.dto.ApiResponse;
import com.cptrans.petrocarga.dto.AuthRequestDTO;
import com.cptrans.petrocarga.dto.AuthResponseDTO;
import com.cptrans.petrocarga.dto.CompletarCadastroDTO;
import com.cptrans.petrocarga.dto.ForgotPasswordRequest;
import com.cptrans.petrocarga.dto.ResendCodeRequest;
import com.cptrans.petrocarga.dto.ResetPasswordRequest;
import com.cptrans.petrocarga.dto.UsuarioRequestDTO;
import com.cptrans.petrocarga.dto.UsuarioResponseDTO;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.models.Usuario;
import com.cptrans.petrocarga.repositories.VeiculoRepository;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.services.AuthService;
import com.cptrans.petrocarga.services.UsuarioService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // TODO: Remover instância depois de cadastrar o primeiro admin em deploy
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private VeiculoRepository veiculoRepository;
    
    @Value("${app.cookie-settings.secure:true}")
    private boolean secure;

    @Value("${app.cookie-settings.same-site:None}")
    private String sameSite;

    /**
     * Faz login com as credenciais informadas no corpo da requisição.
     * Retorna um objeto AuthResponseDTO com o token de autenticação.
     * O token será armazenado em um cookie chamado "auth-token" com validade de 2 horas.
     *
     * @param request O corpo da requisição AuthRequestDTO com as credenciais do usuário.
     * @param response O objeto HttpServletResponse que será usado para adicionar o cookie com o token de autenticação.
     * @return Um objeto ResponseEntity com o corpo AuthResponseDTO e o status de resposta HTTP OK.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid AuthRequestDTO request, HttpServletResponse response) {
        AuthResponseDTO auth = authService.login(request);
        ResponseCookie cookie = ResponseCookie.from("auth-token", auth.getToken())
            .httpOnly(true)
            .secure(secure)
            .sameSite(sameSite)
            .path("/")
            .maxAge(java.time.Duration.ofHours(2))
            .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(auth);
    }

    /**
     * Faz login com o token do Google informado no parâmetro da requisição.
     * Retorna um objeto AuthResponseDTO com o token de autenticação.
     * O token será armazenado em um cookie chamado "auth-token" com validade de 2 horas.
     *
     * @param token O token do Google recebido do cliente.
     * @param response O objeto HttpServletResponse que será usado para adicionar o cookie com o token de autenticação.
     * @return Um objeto ResponseEntity com o corpo AuthResponseDTO e o status de resposta HTTP OK.
     */
    @PostMapping("/loginWithGoogle")
    public ResponseEntity<AuthResponseDTO> loginWithGoogle(@RequestParam(required = true) String token, HttpServletResponse response) {
        AuthResponseDTO auth = authService.loginWithGoogle(token);
        ResponseCookie cookie = ResponseCookie.from("auth-token", auth.getToken())
            .httpOnly(true)
            .secure(secure)
            .sameSite(sameSite)
            .path("/")
            .maxAge(java.time.Duration.ofHours(2))
            .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(auth);
    }

/**
 * Completar cadastro de usuário com as informações informadas no corpo da requisição.
 * Retorna um objeto UsuarioResponseDTO com as informações do usuário.
 * O token de autenticação do usuário deve ser informado no header da requisição.
 *
 * @param userAuthenticated O objeto UserAuthenticated com o token de autenticação do usuário.
 * @param request O corpo da requisição CompletarCadastroDTO com as informações do usuário.
 * @return Um objeto ResponseEntity com o corpo UsuarioResponseDTO e o status de resposta HTTP OK.
 */
    @PostMapping("/completarCadastro")
    public ResponseEntity<UsuarioResponseDTO> completarCadastro(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @RequestBody @Valid CompletarCadastroDTO request) {
        Usuario usuarioCompleto = authService.completarCadastro(request, userAuthenticated.id());
        return ResponseEntity.ok(usuarioCompleto.toResponseDTO());
    }
    

    //TODO: Remover rota depois de cadastrar o primeiro admin em deploy
    @PostMapping("/admin")
    public ResponseEntity<UsuarioResponseDTO> createAdmin(@RequestBody @Valid UsuarioRequestDTO usuario) {
        Usuario novoUsuario = usuarioService.createUsuario(usuario.toEntity(), PermissaoEnum.ADMIN, usuario.getCpf());
        return ResponseEntity.ok(novoUsuario.toResponseDTO());
    }

/**
 * Retorna as informações do usuário logado.
 * O token de autenticação do usuário deve ser informado no header da requisição.
 * @return Um objeto ResponseEntity com o corpo UsuarioResponseDTO e o status de resposta HTTP OK.
 */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> getMe(@AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        if(userAuthenticated == null) {
            throw new AuthorizationDeniedException("Usuário não autenticado");
        }
        UUID usuarioIdFromToken = userAuthenticated.id();
        Usuario usuarioLogado = usuarioService.findByIdAndAtivo(usuarioIdFromToken, true);
        UsuarioResponseDTO response = usuarioLogado.toResponseDTO();
        if(usuarioLogado.getPermissao().equals(PermissaoEnum.MOTORISTA)){
            Boolean possuiVeiculo = veiculoRepository.existsByUsuarioIdAndAtivo(usuarioIdFromToken, true);
            response.setVeiculoCadastrado(possuiVeiculo);
        }
        return ResponseEntity.ok(response);
    }

/**
 * Realiza logout do usuário logado.
 * O token de autenticação do usuário deve ser informado no header da requisição.
 * Após a chamada dessa rota, o token de autenticação do usuário não será mais válido.
 * @param response O objeto HttpServletResponse que será usado para remover o cookie com o token de autenticação.
 * @return Um objeto ResponseEntity com o corpo Void e o status de resposta HTTP NO CONTENT.
 */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("auth-token", "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
   }

    /**
     * Ativa a conta do usuário com base no código de ativação informado.
     * 
     * @param request o objeto AccountActivationRequest com os dados da ativação da conta
     * @return uma resposta com o status de sucesso ou erro
     * 
     */
    @PostMapping("/activate")
    public ResponseEntity<ApiResponse> activateAccount(@RequestBody @Valid AccountActivationRequest request) {
        try {
            usuarioService.activateAccount(request.aceitarTermos(), request.cpf(), request.codigo());
            return ResponseEntity.ok(ApiResponse.success(
                "Conta ativada com sucesso! Você já pode fazer login.",
                "ACCOUNT_ACTIVATED"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                "Código de ativação inválido ou expirado.",
                "INVALID_ACTIVATION_CODE"
            ));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(
                "Email não encontrado. Verifique se o email está correto.",
                "EMAIL_NOT_FOUND"
            ));
        }
    }

/**
 * Reenvia o código de ativação para o email ou CPF informado.
 * 
 * @param request o objeto ResendCodeRequest com os dados do email ou CPF.
 * @return uma resposta com o status de sucesso ou erro.
 * 
 */
    @PostMapping("/resend-code")
    public ResponseEntity<ApiResponse> resendCode(@RequestBody @Valid ResendCodeRequest request) {
        try {
            usuarioService.resendActivationCode(request.email(), request.cpf());
            return ResponseEntity.ok(ApiResponse.success(
                "Código de ativação reenviado! Verifique sua caixa de entrada e spam.",
                "ACTIVATION_CODE_SENT"
            ));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(
                "Email ou CPF não encontrado. Verifique se o email ou CPF está correto.",
                "EMAIL_OR_CPF_NOT_FOUND"
            ));
        }
    }

    /**
     * Reenvia o código de recuperação de senha para o email ou CPF informado.
     * Se o email ou CPF estiver cadastrado, o usuário receberá um código de recuperação para redefinir sua senha.
     * A resposta será um objeto ApiResponse com o status de sucesso ou erro.
     * Se o email ou CPF não estiver cadastrado, a resposta será um objeto ApiResponse com uma mensagem genérica para não expor se o email existe.
     * 
     * @param request o objeto ForgotPasswordRequest com os dados do email ou CPF.
     * @return uma resposta com o status de sucesso ou erro.
     * 
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        try {
            usuarioService.forgotPassword(request.email(), request.cpf());
            return ResponseEntity.ok(ApiResponse.success(
                "Se o email ou CPF estiver cadastrado, você receberá um código de recuperação. Verifique sua caixa de entrada e spam.",
                "RESET_CODE_SENT"
            ));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            // Retorna mensagem genérica para não expor se o email existe
            return ResponseEntity.ok(ApiResponse.success(
                "Se o email ou CPF estiver cadastrado, você receberá um código de recuperação. Verifique sua caixa de entrada e spam.",
                "RESET_CODE_SENT"
            ));
        }
    }


/**
 * Reenvia o código de recuperação de senha para o email ou CPF informado.
 * Se o email ou CPF estiver cadastrado, o usuário receberá um código de recuperação para redefinir sua senha.
 * A resposta será um objeto ApiResponse com o status de sucesso ou erro.
 * Se o email ou CPF não estiver cadastrado, a resposta será um objeto ApiResponse com uma mensagem genérica para não expor se o email existe.
 * 
 * @param request o objeto ResetPasswordRequest com os dados do email ou CPF e do código de recuperação.
 * @return uma resposta com o status de sucesso ou erro.
 * 
 */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        try {
            usuarioService.resetPassword(request.email(), request.cpf(), request.codigo(), request.novaSenha());
            return ResponseEntity.ok(ApiResponse.success(
                "Senha alterada com sucesso! Você já pode fazer login com a nova senha.",
                "PASSWORD_RESET_SUCCESS"
            ));
        } 
        catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(
                "Email ou CPF não encontrado. Verifique se o email ou CPF está correto.",
                "EMAIL_NOT_FOUND"
            ));
        }
    }

}