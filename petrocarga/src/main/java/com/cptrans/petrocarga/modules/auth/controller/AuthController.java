package com.cptrans.petrocarga.modules.auth.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.config.swagger.response.DefaultResponses;
import com.cptrans.petrocarga.config.swagger.response.GetResponses;
import com.cptrans.petrocarga.config.swagger.response.PostResponses;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.auth.dto.request.AccountActivationRequest;
import com.cptrans.petrocarga.modules.auth.dto.request.AuthRequestDTO;
import com.cptrans.petrocarga.modules.auth.dto.request.CompletarCadastroDTO;
import com.cptrans.petrocarga.modules.auth.dto.request.ForgotPasswordRequest;
import com.cptrans.petrocarga.modules.auth.dto.request.GoogleAuthRequestDTO;
import com.cptrans.petrocarga.modules.auth.dto.request.ResendCodeRequest;
import com.cptrans.petrocarga.modules.auth.dto.request.ResetPasswordRequest;
import com.cptrans.petrocarga.modules.auth.dto.response.AuthResponseDTO;
import com.cptrans.petrocarga.modules.auth.service.AuthService;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioRequestDTO;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.security.UserAuthenticated;
import com.cptrans.petrocarga.shared.dto.response.SystemResponse;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final UsuarioUtils usuarioUtils;
    private final UsuarioMapper usuarioMapper;
    
    @Value("${app.cookie-settings.secure:true}")
    private boolean secure;

    @Value("${app.cookie-settings.same-site:None}")
    private String sameSite;

    //POST /auth/login
    @Operation(
        summary = "Realizar login",
        description = "Realiza o login e retorna o usuário logado."
    )
    @PostResponses
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(
            @RequestBody @Valid AuthRequestDTO request,
            HttpServletResponse response
        ) {
        AuthResponseDTO auth = authService.login(request);
        ResponseCookie cookie = ResponseCookie.from("auth-token", auth.getToken())
            .httpOnly(true)
            .secure(secure)
            .sameSite(sameSite)
            .path("/")
            .maxAge(java.time.Duration.ofHours(2))
            .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(auth.getUsuario());
    }

    //POST /auth/loginWithGoogle
    @Operation(
        summary = "Realizar login com Google",
        description = "Realiza o login utilizando o token da conta Google e retorna o usuário logado."
    )
    @PostResponses
    @PostMapping("/loginWithGoogle")
    public ResponseEntity<UsuarioResponseDTO> loginWithGoogle(@RequestBody(required = true) @Valid GoogleAuthRequestDTO googleRequest, HttpServletResponse response) {
        AuthResponseDTO auth = authService.loginWithGoogle(googleRequest.token());
        ResponseCookie cookie = ResponseCookie.from("auth-token", auth.getToken())
            .httpOnly(true)
            .secure(secure)
            .sameSite(sameSite)
            .path("/")
            .maxAge(java.time.Duration.ofHours(2))
            .build();
        
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(auth.getUsuario());
    }

    //POST /auth/completarCadastro
    @Operation(
        summary = "Completar cadastro",
        description = "Completar cadastro de dados obrigatórios do usuário."
    )
    @PostResponses
    @DefaultResponses
    @PostMapping("/completarCadastro")
    public ResponseEntity<UsuarioResponseDTO> completarCadastro(@AuthenticationPrincipal UserAuthenticated userAuthenticated, @Valid @RequestBody CompletarCadastroDTO request) {
        Usuario usuarioCompleto = authService.completarCadastro(request, userAuthenticated.id());
        String cpfOrCnpj = usuarioUtils.getCpfOrCnpjByPermissao(usuarioCompleto.getPermissao(), usuarioCompleto.getId());
        UsuarioResponseDTO response = usuarioMapper.toResponse(usuarioCompleto, cpfOrCnpj);
        return ResponseEntity.ok(response);
    }
    
    //POST /auth/admin
    @Operation(
        summary = "Cadastrar admin",
        description = "Cadastra um novo administrador."
    )
    @PostResponses
    @DefaultResponses
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public ResponseEntity<UsuarioResponseDTO> createAdmin(@RequestBody @Valid UsuarioRequestDTO request) {
        Usuario novoUsuario = usuarioService.createUsuario(request, null, PermissaoEnum.ADMIN);
        String cpfOrCnpj = usuarioUtils.getCpfOrCnpjByPermissao(novoUsuario.getPermissao(), novoUsuario.getId());
        return ResponseEntity.ok(usuarioMapper.toResponse(novoUsuario, cpfOrCnpj));
    }

    //GET /auth/me
    @Operation(
        summary = "Visualizar usuário logado",
        description = "Retorna o usuário logado no sistema."
    )
    @GetResponses
    @DefaultResponses
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> getMe(@AuthenticationPrincipal UserAuthenticated userAuthenticated) {
        UUID usuarioIdFromToken = userAuthenticated.id();
        Usuario usuarioLogado = usuarioService.findByIdAndAtivo(usuarioIdFromToken, true);
        String cpfOrCnpj = usuarioUtils.getCpfOrCnpjByPermissao(usuarioLogado.getPermissao(), usuarioIdFromToken);
        UsuarioResponseDTO response = usuarioMapper.toResponse(usuarioLogado, cpfOrCnpj);
        return ResponseEntity.ok(response);
    }

    //POST /auth/logout
    @Operation(
        summary = "Realizar logout",
        description = "Realiza o logout do usuário no sistema."
    )
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

    //POST /auth/activate
    @Operation(
        summary = "Ativar conta",
        description = "Ativa a conta do usuário no sistema."
    )
    @PostResponses
    @PostMapping("/activate")
    public ResponseEntity<SystemResponse> activateAccount(@RequestBody @Valid AccountActivationRequest request) {
        usuarioService.activateAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SystemResponse(
            "Conta ativada com sucesso!",
            200
        ));
    }

    //POST /auth/resend-code
    @Operation(
        summary = "Reenviar código de recuperação",
        description = "Reenvia o código de recuperação de senha para o email encontrado."
    )
    @PostResponses
    @PostMapping("/resend-code")
    public ResponseEntity<SystemResponse> resendCode(@RequestBody @Valid ResendCodeRequest request) {
        usuarioService.resendActivationCode(request.email(), request.cpf(), request.cnpj());
        return ResponseEntity.ok(new SystemResponse(
            "Se o email, CPF ou CNPJ estiver cadastrado, você receberá um novo código de recuperação. Verifique sua caixa de entrada e spam.",
            200
        ));
       
    }

    //POST /auth/forgot-password
    @Operation(
        summary = "Enviar email de redefinição de senha",
        description = "Envia o código de redefinição de senha para o email encontrado."
    )
    @PostResponses
    @PostMapping("/forgot-password")
    public ResponseEntity<SystemResponse> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        usuarioService.forgotPassword(request.email(), request.cpf(), request.cnpj());
        return ResponseEntity.ok(new SystemResponse(
            "Se o email, CPF ou CNPJ estiver cadastrado, você receberá um código de recuperação. Verifique sua caixa de entrada e spam.",
            200
        ));
    }

    //POST /auth/reset-password
    @Operation(
        summary = "Redefinir senha",
        description = "Redefine a senha do usuário no sistema."
    )
    @PostResponses
    @PostMapping("/reset-password")
    public ResponseEntity<SystemResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        usuarioService.resetPassword(request.email(), request.cpf(), request.cnpj(), request.codigo(), request.novaSenha());
        return ResponseEntity.ok(new SystemResponse(
            "Senha alterada com sucesso! Você já pode fazer login com a nova senha.",
            201
        ));
    }
}