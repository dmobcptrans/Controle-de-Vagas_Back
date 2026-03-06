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
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.dto.AuthRequestDTO;
import com.cptrans.petrocarga.dto.AuthResponseDTO;
import com.cptrans.petrocarga.dto.UsuarioRequestDTO;
import com.cptrans.petrocarga.dto.UsuarioResponseDTO;
import com.cptrans.petrocarga.dto.AccountActivationRequest;
import com.cptrans.petrocarga.dto.ResendCodeRequest;
import com.cptrans.petrocarga.dto.ForgotPasswordRequest;
import com.cptrans.petrocarga.dto.ResetPasswordRequest;
import com.cptrans.petrocarga.dto.ApiResponse;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.models.Usuario;
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
    
    @Value("${app.cookie-settings.secure:true}")
    private boolean secure;

    @Value("${app.cookie-settings.same-site:None}")
    private String sameSite;

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

    //TODO: Remover rota depois de cadastrar o primeiro admin em deploy
    @PostMapping("/admin")
    public ResponseEntity<UsuarioResponseDTO> createAdmin(@RequestBody @Valid UsuarioRequestDTO usuario) {
        Usuario novoUsuario = usuarioService.createUsuario(usuario.toEntity(), PermissaoEnum.ADMIN);
        return ResponseEntity.ok(novoUsuario.toResponseDTO());
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> getMe(@AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        if(userAuthenticated == null) {
            throw new AuthorizationDeniedException("Usuário não autenticado");
        }
        UUID usuarioIdFromToken = userAuthenticated.id();
        Usuario usuarioLogado = usuarioService.findByIdAndAtivo(usuarioIdFromToken, true);
        return ResponseEntity.ok(usuarioLogado.toResponseDTO());
    }

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