package com.cptrans.petrocarga.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cptrans.petrocarga.infrastructure.email.EmailSender;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * =============================================================================
 * EmailTestController - Endpoint para teste de envio de emails
 * =============================================================================
 * 
 * APENAS PARA DESENVOLVIMENTO/DEMONSTRAÇÃO!
 * 
 * Este controller permite testar o envio de emails sem precisar
 * passar pelo fluxo completo de registro/recuperação de senha.
 * 
 * Em produção, este endpoint deve ser:
 * - Removido, ou
 * - Protegido por autenticação de admin
 * 
 * =============================================================================
 */
@RestController
@RequestMapping("/api/test")
@Tag(name = "Email Test", description = "Endpoints para teste de envio de emails (DEV/DEMO)")
public class EmailTestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailTestController.class);

    @Autowired
    private EmailSender emailSender;

    @Value("${app.mailSender.enabled:true}")
    private Boolean enabled;

    /**
     * Testa envio de email de ativação.
     * 
     * Exemplo de uso:
     * POST /petrocarga/api/test/send-activation
     * Body: { "email": "destino@gmail.com" }
     */
    @PostMapping("/send-activation")
    @Operation(summary = "Testa envio de email de ativação", 
               description = "Envia um email de teste de ativação para o endereço especificado")
    public ResponseEntity<Map<String, String>> testSendActivation(
            @RequestBody Map<String, String> request) {
        if(enabled.equals(Boolean.FALSE)){
            LOGGER.info("Mail sender is not enabled");
            return ResponseEntity.badRequest().body(Map.of("error","mailSender isn't enabled"));
        }
        
        String email = request.get("email");
        
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email é obrigatório"));
        }
        
        LOGGER.info("=== TESTE: Enviando email de ativação para: {} ===", email);
        
        // Gera código fake para teste
        String testCode = "123456";
        
        try {
            emailSender.sendActivationCode(email, testCode, null);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Email de ativação enviado para " + email,
                    "code", testCode,
                    "note", "Verifique sua caixa de entrada (e spam)"
            ));
        } catch (Exception e) {
            LOGGER.error("Erro ao enviar email de teste: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Testa envio de email de recuperação de senha.
     * 
     * Exemplo de uso:
     * POST /petrocarga/api/test/send-reset
     * Body: { "email": "destino@gmail.com" }
     */
    @PostMapping("/send-reset")
    @Operation(summary = "Testa envio de email de recuperação", 
               description = "Envia um email de teste de recuperação de senha para o endereço especificado")
    public ResponseEntity<Map<String, String>> testSendReset(
            @RequestBody Map<String, String> request) {
                
        if(enabled.equals(Boolean.FALSE)){
            LOGGER.info("Mail sender is not enabled");
            return ResponseEntity.badRequest().body(Map.of("error","mailSender isn't enabled"));
        }
        
        String email = request.get("email");
        
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Email é obrigatório"));
        }
        
        LOGGER.info("=== TESTE: Enviando email de recuperação para: {} ===", email);
        
        // Gera código fake para teste
        String testCode = "654321";
        
        try {
            emailSender.sendPasswordResetCode(email, testCode);
            
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Email de recuperação enviado para " + email,
                    "code", testCode,
                    "note", "Verifique sua caixa de entrada (e spam)"
            ));
        } catch (Exception e) {
            LOGGER.error("Erro ao enviar email de teste: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Verifica qual implementação de EmailSender está ativa.
     */
    @GetMapping("/email-status")
    @Operation(summary = "Verifica status do serviço de email", 
               description = "Retorna qual implementação de EmailSender está ativa")
    public ResponseEntity<Map<String, String>> getEmailStatus() {
        String implementation = emailSender.getClass().getSimpleName();
        
        return ResponseEntity.ok(Map.of(
                "implementation", implementation,
                "status", "active",
                "note", "Use /send-activation ou /send-reset para testar"
        ));
    }
}
