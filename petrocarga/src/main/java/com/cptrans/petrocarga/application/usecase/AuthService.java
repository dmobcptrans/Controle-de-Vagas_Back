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
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class AuthService {
    
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
        if((request.getEmail() == null && request.getCpf() == null) || (request.getEmail() != null && request.getCpf() != null)) throw new IllegalArgumentException("Informe um email OU CPF.");
       
        
        Usuario usuario = usuarioRepository.findByEmailOrCpfHash(request.getEmail(), request.getCpf() == null ? null : hashService.hash(request.getCpf())).orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

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
     * Se o usuário não existir, um novo usuário será criado com as informações do Google e permnissão de motorista.
     * Se o usuário existir, mas ainda não foi ativado, ele será ativado.
     * Se o usuário existir e não for motorista, o email do usuário e google id serão atualizados.
     * O token de acesso será gerado com base nas informações do usu rio.
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

        Optional<Usuario> usuario = usuarioRepository.findByEmailOrGoogleId(email, googleId);

        if (!usuario.isPresent()) {

            Usuario novoUsuario = usuarioService.createMotoristaByGoogleAccount(name, email, googleId);

            String jwt = jwtService.gerarToken(novoUsuario);

            return new AuthResponseDTO(novoUsuario.toResponseDTO(), jwt);
        }

        if (usuario.get().isAtivo().equals(false)) {
            usuarioService.resendActivationCode(email,null);
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
}
