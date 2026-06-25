package com.cptrans.petrocarga.modules.usuario.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.UsuarioProviderEnum;
import com.cptrans.petrocarga.modules.messaging.email.EmailSender;
import com.cptrans.petrocarga.modules.auth.dto.request.CompletarCadastroDTO;
import com.cptrans.petrocarga.modules.cripto.CriptoService;
import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.events.SpringDomainEventPublisher;
import com.cptrans.petrocarga.modules.events.UsuarioCriadoEvent;
import com.cptrans.petrocarga.modules.gestor.dto.request.GestorFiltrosDTO;
import com.cptrans.petrocarga.modules.gestor.specification.GestorSpecification;
import com.cptrans.petrocarga.modules.reserva.utils.ReservaUtils;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioRequestDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.exceptions.UsuarioExceptions;
import com.cptrans.petrocarga.modules.usuario.repository.UsuarioRepository;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final EmailSender emailSender;
    private final SpringDomainEventPublisher eventPublisher;
    private final ReservaUtils reservaUtils;
    private final HashService hashService;
    private final CriptoService criptoService;

    @Value("${app.security.aes-criptography.active-key-version}")
    private Integer activeKeyVersion;

    
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Usuario findById(UUID id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado."));
    }
    
    public Usuario findByIdAndAtivo(UUID id, Boolean ativo) {
        return usuarioRepository.findByIdAndAtivo(id, ativo).orElseThrow(() -> new EntityNotFoundException("Usuario não encontrado."));
    }

    public List<Usuario> findByPermissao(PermissaoEnum permissao) {
        return usuarioRepository.findByPermissao(permissao);
    }

    public List<Usuario> findByPermissaoAndAtivo(PermissaoEnum permissao, Boolean ativo) {
        return usuarioRepository.findByPermissaoAndAtivo(permissao, ativo);
    }

    @Transactional
    public Usuario createUsuario(UsuarioRequestDTO request, PermissaoEnum permissao) {
        String emailString = request.getEmail().trim().toLowerCase();
        String emailHash = hashService.hash(emailString);
        String cpfString = request.getCpf().trim();
        String cpfHash = hashService.hash(cpfString);
        
        Usuario novoUsuario = UsuarioMapper.toEntity(request);
        
        if (usuarioRepository.existsByEmailHash(emailHash)) {
            throw new UsuarioExceptions.EmailAlreadyExistsException();
        }
        if (usuarioRepository.existsByCpfHash(cpfHash)) {
            throw new UsuarioExceptions.CpfAlreadyExistsException();
        }
        
        novoUsuario.setPermissao(permissao);
        if (request.getSenha() != null && request.getSenha().length() >= 6) novoUsuario.setSenha(passwordEncoder.encode(request.getSenha()));
        novoUsuario.setAtivo(false);
        
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(1_000_000);
        String codeStr = String.format("%06d", code);
        
        novoUsuario.setVerificationCode(codeStr);
        novoUsuario.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        
        String firstPassword = null;
        if (novoUsuario.getPermissao().equals(PermissaoEnum.GESTOR) || novoUsuario.getPermissao().equals(PermissaoEnum.AGENTE)) {
            SecureRandom randomPassword = new SecureRandom();
            firstPassword = String.format("%06d", randomPassword.nextInt(1_000_000));
            novoUsuario.setSenha(passwordEncoder.encode(firstPassword));
        }

        if (novoUsuario.getPermissao().equals(PermissaoEnum.ADMIN)){
            novoUsuario.setAtivo(true);
            novoUsuario.setVerificationCode(null);
            novoUsuario.setVerificationCodeExpiresAt(null);
        }

        String emailCripto = criptoService.encrypt(emailString);
        novoUsuario.setEmailHash(emailHash);
        novoUsuario.setEmailCripto(emailCripto);

        String cpfCripto = criptoService.encrypt(cpfString);
        novoUsuario.setCpfHash(cpfHash);
        novoUsuario.setCpfCripto(cpfCripto);
        novoUsuario.setPersonalDataKeyVersion(activeKeyVersion);
        novoUsuario.setCpfLast5(UsuarioUtils.gerarLastN(cpfString, 5));

        String telefoneString = request.getTelefone().trim();
        String telefoneHash = hashService.hash(telefoneString);
        String telefoneCripto = criptoService.encrypt(telefoneString);
        novoUsuario.setTelefoneHash(telefoneHash);
        novoUsuario.setTelefoneCripto(telefoneCripto);
        novoUsuario.setTelefoneLast4(UsuarioUtils.gerarLastN(telefoneString, 4));
        Usuario saved = usuarioRepository.save(novoUsuario);

        eventPublisher.publish(new UsuarioCriadoEvent(emailString, saved.getVerificationCode(), firstPassword));

        return saved;
    }

    @Transactional
    public Usuario activateAccount(Boolean aceitarTermos, String cpf, String code) {
        if (aceitarTermos.equals(Boolean.FALSE)) throw new IllegalArgumentException("É necessário aceitar os termos de uso e política de privacidade para ativar a conta");
        if (cpf == null) throw new IllegalArgumentException("É necessário informar o CPF.");
        
        Usuario usuario = usuarioRepository.findByCpfHash(hashService.hash(cpf)).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado."));

        if (usuario.getVerificationCode() == null || !usuario.getVerificationCode().equals(code)) throw new IllegalArgumentException("Código inválido.");

        if (usuario.getVerificationCodeExpiresAt() == null || usuario.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Código expirado.");
        }

        if (usuario.getDesativadoEm() != null) {
            if(usuario.getPermissao().equals(PermissaoEnum.GESTOR) || usuario.getPermissao().equals(PermissaoEnum.AGENTE)) {
                throw new IllegalArgumentException("Usuario desativado em " + usuario.getDesativadoEm() + ". Para mais informações, entre em contato com a CPTrans.");
            }
        }

        usuario.setAceitarTermos(aceitarTermos);
        usuario.setAceitouTermosEm(DateUtils.agora());
        usuario.setAtivo(true);
        usuario.setVerificationCode(null);
        usuario.setVerificationCodeExpiresAt(null);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void resendActivationCode(String email, String cpf) {
        Optional<Usuario> usuarioOptional = findByEmailOrCpfAndAtivoTrue(email, cpf);

        if (usuarioOptional.isEmpty()) return;

        Usuario usuario = usuarioOptional.get();
        
        if (usuario.isAtivo() != null && usuario.isAtivo()) {
            throw new IllegalArgumentException("Usuário já ativado.");
        }
        if ((usuario.getPermissao().equals(PermissaoEnum.GESTOR) || usuario.getPermissao().equals(PermissaoEnum.AGENTE) || usuario.getPermissao().equals(PermissaoEnum.ADMIN)) && usuario.getDesativadoEm() != null)  {
            throw new IllegalArgumentException("Usuário desativado em " + usuario.getDesativadoEm() + ". Para mais informações, entre em contato com a CPTrans.");
        }

        SecureRandom random = new SecureRandom();
        int code = random.nextInt(1_000_000);
        String codeStr = String.format("%06d", code);
        usuario.setVerificationCode(codeStr);
        usuario.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        String randomPassword = null;
        // Reenvia código de ativação via email (assíncrono)
        emailSender.sendActivationCode((email != null ? email : criptoService.decrypt(usuarioSalvo.getEmailHash(), usuarioSalvo.getPersonalDataKeyVersion())), codeStr, randomPassword);
    }

    // ==================== RECUPERAÇÃO DE SENHA ====================

    @Transactional
    public void forgotPassword(String email, String cpf) {
        Optional<Usuario> optUsuario = findByEmailOrCpfAndAtivoTrue(email, cpf);
        
        if (optUsuario.isEmpty()) {
            // Por segurança, não revelamos se o email/cpf existe ou não
            return;
        }

        Usuario usuario = optUsuario.get();

        // Gera código de 6 dígitos
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(1_000_000);
        String codeStr = String.format("%06d", code);

        // Reutiliza os campos de verification_code para reset de senha
        usuario.setVerificationCode(codeStr);
        usuario.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        usuarioRepository.save(usuario);

        // Envia email de recuperação de forma assíncrona
        emailSender.sendPasswordResetCode(email != null ? email : criptoService.decrypt(usuario.getEmailHash(), usuario.getPersonalDataKeyVersion()), codeStr);
    }

    @Transactional
    public void resetPassword(String email, String cpf, String code, String novaSenha) {
        
        Usuario usuario = findByEmailOrCpfAndAtivoTrue(email, cpf).orElseThrow(() -> new EntityNotFoundException("Credenciais inválidas ou código expirado."));
        
        if (!usuario.isAtivo()) {
            throw new IllegalArgumentException("Usuário desativado.");
        }

        // Valida código
        if (usuario.getVerificationCode() == null || !usuario.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Credenciais inválidas ou código expirado.");
        }

        // Valida expiração
        if (usuario.getVerificationCodeExpiresAt() == null || 
            usuario.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Credenciais inválidas ou código expirado.");
        }

        // Atualiza senha
        usuario.setSenha(passwordEncoder.encode(novaSenha));

        // Limpa código após uso
        usuario.setVerificationCode(null);
        usuario.setVerificationCodeExpiresAt(null);

        usuarioRepository.save(usuario);
    }


    public Usuario patchUpdate(UUID id, PermissaoEnum permissao, UsuarioPATCHRequestDTO patchRequestDTO) {
        Usuario usuarioExistente = findByIdAndAtivo(id, true);

        if (!usuarioExistente.getPermissao().equals(permissao)) throw new IllegalArgumentException("Permissão inválida para o usuário.");

        if (patchRequestDTO.getNome() != null) usuarioExistente.setNome(patchRequestDTO.getNome());
        if (patchRequestDTO.getTelefone() != null) {
            usuarioExistente.setTelefoneHash(hashService.hash(patchRequestDTO.getTelefone()));
            usuarioExistente.setTelefoneCripto(criptoService.encrypt(patchRequestDTO.getTelefone()));
            usuarioExistente.setTelefoneLast4(UsuarioUtils.gerarLastN(patchRequestDTO.getTelefone(), 4));
            usuarioExistente.setPersonalDataKeyVersion(activeKeyVersion);
        }
        if (patchRequestDTO.getEmail() != null) {
            String emailString = patchRequestDTO.getEmail().trim().toLowerCase();
            String emailHash = hashService.hash(emailString);
            Optional<Usuario> usuarioByEmail = usuarioRepository.findByEmailHash(emailHash);
            if (usuarioByEmail.isPresent() && !usuarioByEmail.get().getId().equals(id))  throw new IllegalArgumentException("Email já cadastrado");
            String emailCripto = criptoService.encrypt(emailString);
            usuarioExistente.setEmailHash(emailHash);
            usuarioExistente.setEmailCripto(emailCripto);
            usuarioExistente.setPersonalDataKeyVersion(activeKeyVersion);
        }
        if (patchRequestDTO.getCpf() != null) {
            String cpfHash = hashService.hash(patchRequestDTO.getCpf());
            Optional<Usuario> usuarioByCpf = usuarioRepository.findByCpfHashAndIdNot(cpfHash, id);
            if (usuarioByCpf.isPresent()) throw new IllegalArgumentException("CPF já cadastrado");
            String cpfCripto = criptoService.encrypt(patchRequestDTO.getCpf());
            usuarioExistente.setCpfHash(cpfHash);
            usuarioExistente.setCpfCripto(cpfCripto);
            usuarioExistente.setCpfLast5(UsuarioUtils.gerarLastN(patchRequestDTO.getCpf(), 5));
            usuarioExistente.setPersonalDataKeyVersion(activeKeyVersion);
        }
        if (patchRequestDTO.getSenha() != null) {
            usuarioExistente.setSenha(passwordEncoder.encode(patchRequestDTO.getSenha()));
        }
        return usuarioRepository.save(usuarioExistente);
    }
    public void deleteById(UUID id) {
        Usuario usuario = findByIdAndAtivo(id, true);
        if(usuario.getPermissao().equals(PermissaoEnum.MOTORISTA) && reservaUtils.existsByUsuarioId(id)){
            throw new IllegalArgumentException("Motorista não pode ser excluido pois possui reserva ativa.");
        }
        usuario.setAtivo(false);
        usuario.setDesativadoEm(DateUtils.agora());
        usuarioRepository.save(usuario);
    }

    public List<Usuario> findAllGestoresWithFiltros(GestorFiltrosDTO filtros) {
        return usuarioRepository.findAll(GestorSpecification.filtrar(filtros));
    }

    public Usuario createMotoristaByGoogleAccount(String name, String email, String googleId){
        Usuario novoUsuario = new Usuario();
        if(email != null) email = email.trim().toLowerCase();
        novoUsuario.setAtivo(true);
        novoUsuario.setEmailHash(hashService.hash(email));
        novoUsuario.setEmailCripto(criptoService.encrypt(email));
        novoUsuario.setNome(name);
        novoUsuario.setGoogleId(googleId);
        novoUsuario.setProvider(UsuarioProviderEnum.GOOGLE);
        novoUsuario.setPermissao(PermissaoEnum.MOTORISTA);
        novoUsuario.setVersaoTermos(UsuarioUtils.VERSAO_ATUAL_TERMOS);
        novoUsuario.setPersonalDataKeyVersion(activeKeyVersion);

        return usuarioRepository.save(novoUsuario);

    }

    public Usuario completarCadastro(CompletarCadastroDTO request, UUID usuarioId){
        if(request.getAceitarTermos().equals(Boolean.FALSE)) throw new IllegalArgumentException("Usuário não aceitou os termmos de uso e privacidade dos dados.");

        Usuario usuarioCadastrado = usuarioRepository.findByIdAndAtivo(usuarioId, true).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado ou desativado"));
        
        if (usuarioCadastrado.getCpfHash() != null && 
        !usuarioCadastrado.getCpfHash().isEmpty() &&
        usuarioCadastrado.getCpfCripto() != null &&
        !usuarioCadastrado.getCpfCripto().isEmpty() &&
        usuarioCadastrado.getCpfLast5() != null &&
        !usuarioCadastrado.getCpfLast5().isEmpty() &&
        usuarioCadastrado.getAceitouTermosEm() != null &&
        usuarioCadastrado.getVersaoTermos().equals(UsuarioUtils.VERSAO_ATUAL_TERMOS))
            throw new IllegalArgumentException("Cadastro já completo.");

        String cpfHash = hashService.hash(request.getCpf());
        Optional<Usuario> usuarioByCpf = usuarioRepository.findByCpfHash(cpfHash);

        if (usuarioByCpf.isPresent() && !usuarioByCpf.get().getId().equals(usuarioCadastrado.getId())) throw new IllegalArgumentException("CPF já cadastrado.");

        usuarioCadastrado.setCpfHash(cpfHash);
        usuarioCadastrado.setCpfCripto(criptoService.encrypt(request.getCpf()));
        usuarioCadastrado.setCpfLast5(UsuarioUtils.gerarLastN(request.getCpf(), 5));
        usuarioCadastrado.setAceitarTermos(request.getAceitarTermos());
        usuarioCadastrado.setAceitouTermosEm(DateUtils.agora());
        usuarioCadastrado.setVersaoTermos(UsuarioUtils.VERSAO_ATUAL_TERMOS);
        usuarioCadastrado.setTelefoneHash(hashService.hash(request.getTelefone()));
        usuarioCadastrado.setTelefoneCripto(criptoService.encrypt(request.getTelefone()));
        usuarioCadastrado.setTelefoneLast4(UsuarioUtils.gerarLastN(request.getTelefone(), 4));
        usuarioCadastrado.setPersonalDataKeyVersion(activeKeyVersion);
        usuarioCadastrado.setSenha((request.getSenha() != null ? passwordEncoder.encode(request.getSenha()) : null));

        return usuarioRepository.save(usuarioCadastrado);

    }  

    public void reativar(UUID usuarioId){
        Usuario usuario = usuarioRepository.findByIdAndAtivoAndPermissaoInAndDesativadoEmNotNull(usuarioId, false, List.of(PermissaoEnum.AGENTE, PermissaoEnum.GESTOR))
            .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado."));

        usuario.setDesativadoEm(null);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    public Optional<Usuario> findByEmailOrCpfAndAtivoTrue(String email, String cpf) {
        if ((email == null && cpf == null) || (email != null && cpf != null)) {
            throw new IllegalArgumentException("Informe um email OU CPF.");
        }

        Optional<Usuario> usuarOptional = Optional.empty();

        if (email != null) {
            email = email.trim().toLowerCase();
            usuarOptional = usuarioRepository.findByEmailHashAndAtivoTrue(hashService.hash(email));
        }

        if (cpf != null) {
            usuarOptional = usuarioRepository.findByCpfHashAndAtivoTrue(hashService.hash(cpf));
        }

        return usuarOptional;
    }
}