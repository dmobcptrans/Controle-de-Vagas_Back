package com.cptrans.petrocarga.modules.auth.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.UsuarioProviderEnum;
import com.cptrans.petrocarga.modules.auth.dto.request.AuthRequestDTO;
import com.cptrans.petrocarga.modules.auth.dto.request.CompletarCadastroDTO;
import com.cptrans.petrocarga.modules.auth.dto.response.AuthResponseDTO;
import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.googleAuth.GoogleAuthService;
import com.cptrans.petrocarga.modules.motorista.service.MotoristaService;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.repository.UsuarioRepository;
import com.cptrans.petrocarga.modules.usuario.service.UsuarioService;
import com.cptrans.petrocarga.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final HashService hashService;
    private final GoogleAuthService googleAuthService;
    private final UsuarioService usuarioService;
    private final MotoristaService motoristaService;

 

    /**
     * Faz o login do usuário com base em (email ou cpf) e senha.
     * 
     * @param request DTO contendo (email ou cpf) e senha do usuário.
     * @return DTO contendo informações do usuário e token de acesso.
     * @throws IllegalArgumentException se o (email e cpf) for nulo ou se as credenciais forem inválidas.
     */
    public AuthResponseDTO login(AuthRequestDTO request) {

        Usuario usuario = usuarioService.findByEmailOrCpfAndAtivoTrue(request.getEmail(), request.getCpf()).orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenha())) throw new AuthExceptions.CredenciaisInvalidasException();
        
        String token = jwtService.gerarToken(usuario);

       return new AuthResponseDTO(UsuarioMapper.toResponse(usuario), token);
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

        Optional<Usuario> usuario = usuarioRepository.findByEmailHashOrGoogleId(hashService.hash(email), googleId);

        if (!usuario.isPresent()) {
            Usuario novoUsuario = usuarioService.createMotoristaByGoogleAccount(name, email, googleId);
            String jwt = jwtService.gerarToken(novoUsuario);

            return new AuthResponseDTO(UsuarioMapper.toResponse(novoUsuario), jwt);
        }

        if (usuario.get().isAtivo().equals(false)) {
            if ((usuario.get().getPermissao().equals(PermissaoEnum.GESTOR) || usuario.get().getPermissao().equals(PermissaoEnum.AGENTE) || usuario.get().getPermissao().equals(PermissaoEnum.ADMIN)) && usuario.get().getDesativadoEm() != null) {
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

        return new AuthResponseDTO(UsuarioMapper.toResponse(usuario.get()), jwt);
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