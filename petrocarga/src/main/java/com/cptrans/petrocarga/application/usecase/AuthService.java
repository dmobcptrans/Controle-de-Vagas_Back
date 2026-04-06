package com.cptrans.petrocarga.application.usecase;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.dto.AuthRequestDTO;
import com.cptrans.petrocarga.application.dto.AuthResponseDTO;
import com.cptrans.petrocarga.application.dto.CompletarCadastroDTO;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;
import com.cptrans.petrocarga.domain.enums.UsuarioProviderEnum;
import com.cptrans.petrocarga.domain.repositories.UsuarioRepository;
import com.cptrans.petrocarga.infrastructure.security.GoogleAuthService;
import com.cptrans.petrocarga.infrastructure.security.HashService;
import com.cptrans.petrocarga.infrastructure.security.JwtService;
import com.cptrans.petrocarga.infrastructure.security.UserAuthenticated;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private HashService hashService;

    @Autowired
    private GoogleAuthService googleAuthService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private MotoristaService motoristaService;

    /**
     * Faz o login do usuário com base em (email ou cpf) e senha.
     * 
     * @param request DTO contendo (email ou cpf) e senha do usuário.
     * @return DTO contendo informações do usuário e token de acesso.
     * @throws IllegalArgumentException se o (email e cpf) for nulo ou se as credenciais forem inválidas.
     */
    public AuthResponseDTO login(AuthRequestDTO request) {
        if((request.getEmail() == null && request.getCpf() == null) || (request.getEmail() != null && request.getCpf() != null)) 
            throw new IllegalArgumentException("Informe um email OU CPF.");
        
        Usuario usuario = usuarioRepository.findByEmailHashOrCpfHash(
            request.getEmail() == null ? null : hashService.hash(request.getEmail()), 
            request.getCpf() == null ? null : hashService.hash(request.getCpf())
        ).orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        if(usuario.isAtivo().equals(false)) {
            throw new IllegalArgumentException("Usuário desativado.");
        }
        if(!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) {
            throw new IllegalArgumentException("Credenciais inválidas.");
        }
        String token = jwtService.gerarToken(usuario);

        return new AuthResponseDTO(usuario.toResponseDTO(), token);
    }

    /**
     * Faz o login do usuário com base no token do Google informado.
     * Se o usuário não existir, um novo usuário será criado com as informações do Google e permissão de motorista.
     * Se o usuário existir, mas ainda não foi ativado, ele será ativado.
     * Se o usuário existir e não for motorista, o email do usuário e google id serão atualizados.
     * O token de acesso será gerado com base nas informações do usuário.
     * 
     * @param token O token do Google recebido do cliente.
     * @return Um objeto AuthResponseDTO com as informações do usuário e o token de acesso.
     * @throws IllegalArgumentException se o token do Google for nulo ou inválido.
     */
    public AuthResponseDTO loginWithGoogle(String token)  {

        Payload payload = googleAuthService.verifyGoogleToken(token);

        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String name = (String) payload.get("name");

        Optional<Usuario> usuario = usuarioRepository.findByEmailHashOrGoogleId(hashService.hash(email), googleId);

        if (!usuario.isPresent()) {
            Usuario novoUsuario = usuarioService.createMotoristaByGoogleAccount(name, email, googleId);
            String jwt = jwtService.gerarToken(novoUsuario);
            return new AuthResponseDTO(novoUsuario.toResponseDTO(), jwt);
        }

        if (usuario.get().isAtivo().equals(false)) {
            if ((usuario.get().getPermissao().equals(PermissaoEnum.GESTOR) || 
                 usuario.get().getPermissao().equals(PermissaoEnum.AGENTE) || 
                 usuario.get().getPermissao().equals(PermissaoEnum.ADMIN)) && 
                 usuario.get().getDesativadoEm() != null) {
                throw new IllegalArgumentException("Usuário desativado em " + usuario.get().getDesativadoEm() + ". Para mais informações, entre em contato com a CPTrans.");
            }
            usuarioService.resendActivationCode(email, null);
            throw new IllegalArgumentException("Usuário desativado. Se deseja ativar a conta, siga as instruções enviadas para o email '" + email + "'.");
        }

        if(usuario.isPresent() && usuario.get().getGoogleId() == null) {
            usuario.get().setGoogleId(googleId);
            usuario.get().setProvider(UsuarioProviderEnum.GOOGLE);
            usuarioRepository.save(usuario.get());
        }
        String jwt = jwtService.gerarToken(usuario.get());

        return new AuthResponseDTO(usuario.get().toResponseDTO(), jwt);
    }

    /**
     * Completar cadastro de usuário com as informações informadas no corpo da requisição.
     * Retorna um objeto Usuario com as informações do usuário.
     * O token de autenticação do usuário deve ser informado no header da requisição.
     * 
     * @param request O corpo da requisição CompletarCadastroDTO com as informações do usuário.
     * @param usuarioId O ID do usuário a ser completado.
     * @return Um objeto Usuario com as informações do usuário.
     * @throws EntityNotFoundException se o usuário não for encontrado para completar cadastro.
     */
    @Transactional
    public Usuario completarCadastro(CompletarCadastroDTO request, UUID usuarioId){
        Usuario usuario = usuarioService.completarCadastro(request, usuarioId);
        if(usuario == null){
            throw new EntityNotFoundException("Usuário não encontrado para completar cadastro.");
        }
        if(usuario.getPermissao().equals(PermissaoEnum.MOTORISTA)){
            motoristaService.completarCadastro(usuario, request.getNumeroCnh(), request.getDataValidadeCnh(), request.getTipoCnh());
        }
        return usuario;
    }

    /**
     * Autentica um usuário a partir de um token JWT (usado principalmente para SSE em dispositivos mobile)
     * 
     * @param token O token JWT a ser validado (pode vir de query parameter ou header)
     * @return UserAuthenticated se o token for válido e o usuário estiver ativo, null caso contrário
     */
    public UserAuthenticated authenticateByToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                log.debug("Token vazio ou nulo para autenticação SSE");
                return null;
            }
            
            log.debug("Tentando autenticar via token para SSE");
            
            // Extrai o username/email do token
            String username = jwtService.extractUsername(token);
            if (username == null || username.trim().isEmpty()) {
                log.debug("Não foi possível extrair username do token");
                return null;
            }
            
            log.debug("Username extraído do token: {}", username);
            
            // Valida se o token é válido (assinatura, expiração, etc)
            if (!jwtService.isTokenValid(token)) {
                log.debug("Token inválido ou expirado");
                return null;
            }
            
            // Busca o usuário pelo username (pode ser email ou CPF)
            Usuario usuario = null;
            
            // Tenta buscar por email primeiro
            String emailHash = hashService.hash(username);
            if (emailHash != null) {
                Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailHash(emailHash);
                if (usuarioOpt.isPresent()) {
                    usuario = usuarioOpt.get();
                }
            }
            
            // Se não encontrou por email, tenta por CPF
            if (usuario == null) {
                String cpfHash = hashService.hash(username);
                if (cpfHash != null) {
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByCpfHash(cpfHash);
                    if (usuarioOpt.isPresent()) {
                        usuario = usuarioOpt.get();
                    }
                }
            }
            
            if (usuario == null) {
                log.debug("Usuário não encontrado para o username: {}", username);
                return null;
            }
            
            if (!usuario.isAtivo()) {
                log.debug("Usuário está desativado: {}", usuario.getId());
                return null;
            }
            
            log.debug("Usuário autenticado via token para SSE: {}", usuario.getId());
            return new UserAuthenticated(usuario);
            
        } catch (Exception e) {
            log.error("Erro ao autenticar via token para SSE: {}", e.getMessage(), e);
            return null;
        }
    }
}