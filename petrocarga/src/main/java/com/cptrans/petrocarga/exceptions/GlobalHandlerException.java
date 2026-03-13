package com.cptrans.petrocarga.exceptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalHandlerException {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalHandlerException.class);

    /**
    * Trata ClientAbortException, que ocorre quando o cliente desconecta durante uma requisição, especialmente em conexões SSE (Server-Sent Events).
    * Esta exceção é comum e esperada em cenários de SSE, portanto, apenas ignore-as.
    * @param ex a exceção lançada quando o cliente desconecta
    * @param request a requisição HTTP que originou a exceção
    * @return void (nenhuma resposta é enviada, pois o cliente já desconectou)
    */
    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbort(ClientAbortException ex, HttpServletRequest request) {
        // Client disconnected - this is normal for SSE, just ignore
        log.debug("Client disconnected from {}: {}", request.getRequestURI(), ex.getMessage());
    }
    
    /**
     * Trata AsyncRequestNotUsableException, que pode ocorrer quando o cliente desconecta durante uma requisição assíncrona. Esta exceção é comum e esperada em cenários de SSE, portanto, apenas ignore-as.
     * @param ex a exceção lançada quando o cliente desconecta
     * @param request a requisição HTTP que originou a exceção
     * @return void (nenhuma resposta é enviada)
     */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleAsyncNotUsable(AsyncRequestNotUsableException ex, HttpServletRequest request) {
        log.debug("Async request no longer usable for {}: {}", request.getRequestURI(), ex.getMessage());
    }
    
    /**
    *Trata IOException, que pode ocorrer quando o cliente desconecta durante uma requisição. 
    *Se a mensagem da exceção indicar que o cliente desconectou (ex: "Broken pipe" ou "Connection reset"), apenas ignore a exceção.
    * Para outras IOExceptions, retorna um erro 500 com detalhes.
     * @param ex a exceção de IO lançada
     * @param request a requisição HTTP que originou a exceção
     * @return ResponseEntity com status 500 e detalhes do erro, ou null se o cliente desconectou
    * 
    */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIOException(IOException ex, HttpServletRequest request) {
        String message = ex.getMessage();
        if (message != null && (message.contains("Broken pipe") || message.contains("Connection reset"))) {
            // Client disconnected - no response needed
            log.debug("Client disconnected (IO) from {}: {}", request.getRequestURI(), message);
            return null;
        }
        // Other IO errors - return 500
        Map<String, String> error = new HashMap<>();
        error.put("erro", "Erro de I/O");
        error.put("cause", message != null ? message : "unknown");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
    * Trata EntityNotFoundException (404) - quando uma entidade não é encontrada no banco de dados.
    * 
    * @param ex a exceção lançada quando a entidade não é encontrada
    * @return ResponseEntity com status 404 e um mapa contendo a mensagem de erro e causa
    */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(EntityNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("erro", ex.getMessage());
        String causeMessage = "unknown";
        if (ex.getCause() != null && ex.getCause().toString() != null) {
            causeMessage = ex.getCause().getMessage();
        }
        error.put("cause", causeMessage);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Trata IllegalArgumentException (400) - quando o algum parametro não é válido ou quando o estado do objeto é inválido para a operação solicitada.
     * @return ResponseEntity com status 400 e um mapa contendo a mensagem de erro e causa
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("erro", ex.getMessage());
        String causeMessage = "unknown";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            causeMessage = ex.getCause().getMessage();
        }
        error.put("cause", causeMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Trata erros genéricos que não foram tratados por
     * outros métodos de tratamento de exceções.
     * 
     * @param ex Exceção a ser tratada
     * @param request Requisição HTTP que originou a exceção
     * @return Resposta com status 500 e a mensagem contendo o erro e causa
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex, HttpServletRequest request) {
        Map<String, String> error = new HashMap<>();
        error.put("erro", "Erro interno no servidor");

        // Monta mensagem detalhada para diagnóstico
        String exType = ex.getClass().getName();
        String exMsg = ex.getMessage();
        String causeMessage;

        if (exMsg != null && !exMsg.isBlank()) {
            causeMessage = exType + ": " + exMsg;
        } else if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            causeMessage = exType + " causado por " + ex.getCause().getClass().getName() + ": " + ex.getCause().getMessage();
        } else {
            causeMessage = exType + " (sem mensagem)";
        }

        error.put("cause", causeMessage);

        log.error("=== ERRO NÃO TRATADO em {} {} ===", request.getMethod(), request.getRequestURI());
        log.error("Tipo: {} | Mensagem: {}", exType, exMsg);
        log.error("Stack trace completo:", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Trata DataIntegrityViolationException (409) - quando uma solicitação viola a regra de integridade de dados.
     * Exibe a mensagem de erro com a causa mais detalhada.
     * @return Resposta com status 409 e um mapa contendo a mensagem de erro e causa
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("erro", "Integridade de dados violada.");
        String causeMessage = ex.getMostSpecificCause().getMessage().split("Detalhe:")[0].trim();
        error.put("cause", causeMessage);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

/**
 * Trata MethodArgumentNotValidException (400) - quando o parâmetro de uma solicitação não é válido.
 * Exibe a mensagem de erro com a causa mais detalhada.
 * 
 * @return Resposta com status 400 e um mapa contendo a mensagem de erro e causa
 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> error = new HashMap<>();
         if (!ex.getAllErrors().isEmpty()) {
        error.put("erro", ex.getAllErrors().get(0).getDefaultMessage());
        } else {
            error.put("erro", "Erro de validação desconhecido");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

/**
 * Trata ConstraintViolationException (400) - quando a solicitação viola uma regra de validação.
 * Exibe uma mensagem de erro com a causa mais detalhada.
 * @return Resposta com status 400 e um mapa contendo a mensagem de erro e causa
 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> error = new HashMap<>();
        if (!ex.getConstraintViolations().isEmpty()) {
            error.put("erro", ex.getConstraintViolations().iterator().next().getMessage());
        } else {
            error.put("erro", "Erro de validação desconhecido");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

/**
 * Trata BadCredentialsException (401) - quando as credenciais fornecidas são inválidas.
 * Exibe uma mensagem de erro com a causa mais detalhada.
 * @return Resposta com status 401 e um mapa contendo a mensagem de erro e causa
 */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("erro", ex.getMessage());
        error.put("cause", "Credenciais inválidas fornecidas.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

/**
 * Trata AuthorizationDeniedException (403) - quando o acesso é negado.
 * Exibe uma mensagem de erro com a causa mais detalhada.
 * @return Resposta com status 403 e um mapa contendo a mensagem de erro e causa
 */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("erro", "Acesso negado.");
        String causeMessage = "unknown";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            causeMessage = ex.getCause().getMessage();
        }
        error.put("cause", causeMessage);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

/**
 * Trata IllegalStateExcepion (400) - quando o estado do objeto é inválido para uma operação solicitada.
 * Exibe uma mensagem de erro com a causa mais detalhada.
 * @return Resposta com status 400 e um mapa contendo a mensagem de erro e causa
 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("erro", ex.getMessage());
        String causeMessage = "unknown";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            causeMessage = ex.getCause().getMessage();
        }
        error.put("cause", causeMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}