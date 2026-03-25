package com.cptrans.petrocarga.application.usecase;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cptrans.petrocarga.application.dto.CompletarCadastroDTO;
import com.cptrans.petrocarga.application.dto.GestorFiltrosDTO;
import com.cptrans.petrocarga.application.dto.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.application.port.out.EmailSender;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;
import com.cptrans.petrocarga.domain.enums.UsuarioProviderEnum;
import com.cptrans.petrocarga.domain.events.UsuarioCriadoEvent;
import com.cptrans.petrocarga.domain.repositories.UsuarioRepository;
import com.cptrans.petrocarga.domain.specification.GestorSpecification;
import com.cptrans.petrocarga.infrastructure.event.SpringDomainEventPublisher;
import com.cptrans.petrocarga.infrastructure.security.CriptoService;
import com.cptrans.petrocarga.infrastructure.security.HashService;
import com.cptrans.petrocarga.shared.utils.DateUtils;
import com.cptrans.petrocarga.shared.utils.ReservaUtils;
import com.cptrans.petrocarga.shared.utils.UsuarioUtils;

import jakarta.persistence.EntityNotFoundException;


@Service
public class UsuarioService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private SpringDomainEventPublisher eventPublisher;

    @Autowired
    private ReservaUtils reservaUtils;

    @Autowired
    private HashService hashService;

    @Autowired
    private CriptoService criptoService;

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
    public Usuario createUsuario(Usuario novoUsuario, PermissaoEnum permissao, String cpf_string) {
        String emailString = novoUsuario.getEmailHash();
        if(usuarioRepository.findByEmailHash(hashService.hash(emailString)).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        if(usuarioRepository.findByCpfHash(hashService.hash(cpf_string)).isPresent()) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }
        novoUsuario.setPermissao(permissao);
        novoUsuario.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));
        // Ensure user is created as inactive and generate activation code
        novoUsuario.setAtivo(false);

        SecureRandom random = new SecureRandom();
        int code = random.nextInt(1_000_000);
        String codeStr = String.format("%06d", code);
        novoUsuario.setVerificationCode(codeStr);
        novoUsuario.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        String password = null;
        if(novoUsuario.getPermissao().equals(PermissaoEnum.GESTOR) || novoUsuario.getPermissao().equals(PermissaoEnum.AGENTE)) {
            SecureRandom randomPassword = new SecureRandom();
            password = String.format("%06d", randomPassword.nextInt(1_000_000));
            novoUsuario.setSenha(passwordEncoder.encode(password));
        }
        if(novoUsuario.getPermissao().equals(PermissaoEnum.ADMIN)){
            novoUsuario.setAtivo(true);
            novoUsuario.setVerificationCode(null);
            novoUsuario.setVerificationCodeExpiresAt(null);
        }
        novoUsuario.setEmailHash(hashService.hash(emailString));
        novoUsuario.setEmailCripto(criptoService.encrypt(emailString));
        novoUsuario.setCpfHash(hashService.hash(cpf_string));
        novoUsuario.setCpfCripto(criptoService.encrypt(cpf_string));
        novoUsuario.setPersonalDataKeyVersion(activeKeyVersion);
        novoUsuario.setCpfLast5(UsuarioUtils.gerarLastN(cpf_string, 5));
        String telefone = novoUsuario.getTelefoneHash();
        novoUsuario.setTelefoneHash(hashService.hash(telefone));
        novoUsuario.setTelefoneCripto(criptoService.encrypt(telefone));
        novoUsuario.setTelefoneLast4(UsuarioUtils.gerarLastN(telefone, 4));
        Usuario saved = usuarioRepository.save(novoUsuario);

        // Envia código de ativação via email (assíncrono)
        // O EmailSender é @Async, exceções são tratadas pelo AsyncUncaughtExceptionHandler
        eventPublisher.publish(new UsuarioCriadoEvent(emailString, saved.getVerificationCode(), password));

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
        usuario.setAceitouTermosEm(OffsetDateTime.now(DateUtils.FUSO_BRASIL));
        usuario.setAtivo(true);
        usuario.setVerificationCode(null);
        usuario.setVerificationCodeExpiresAt(null);

        return usuarioRepository.save(usuario);
    }

    public String visualizarTelefone(UUID usuarioId){
        Usuario usuario = findByIdAndAtivo(usuarioId, true);
        return criptoService.decrypt(usuario.getTelefoneCripto(), usuario.getPersonalDataKeyVersion());
    }

    public String visualizarCpf(UUID usuarioId){
        Usuario usuario = findByIdAndAtivo(usuarioId, true);
        return criptoService.decrypt(usuario.getCpfCripto(), usuario.getPersonalDataKeyVersion());
    }

    @Transactional
    public void resendActivationCode(String email, String cpf) {
        if ((email == null && cpf == null) || (email != null && cpf != null)) {
            throw new IllegalArgumentException("Informe um email OU CPF.");
        }
        Usuario usuario = usuarioRepository.findByEmailHashOrCpfHash(email == null ? null : hashService.hash(email), cpf == null ? null : hashService.hash(cpf)).orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

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

        usuarioRepository.save(usuario);
        String randomPassword = null;
        // Reenvia código de ativação via email (assíncrono)
        emailSender.sendActivationCode(email, codeStr, randomPassword);
    }

    // ==================== RECUPERAÇÃO DE SENHA ====================

    @Transactional
    public void forgotPassword(String email, String cpf) {
        if ((email == null && cpf == null) || (email != null && cpf != null)) {
            throw new IllegalArgumentException("Informe um email OU CPF.");
        }
        // Busca usuário pelo email (silenciosamente ignora se não existir por segurança)
        Optional<Usuario> optUsuario = usuarioRepository.findByEmailOrCpfHashAndAtivo(email, cpf == null ? null : hashService.hash(cpf), true);
        
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
        emailSender.sendPasswordResetCode(email, codeStr);
    }

    @Transactional
    public void resetPassword(String email, String cpf, String code, String novaSenha) {
        if ((email == null && cpf == null) || (email != null && cpf != null)) {
            throw new IllegalArgumentException("Informe um email OU CPF.");
        }

        Usuario usuario = usuarioRepository.findByEmailHashOrCpfHash(email == null ? null : hashService.hash(email), cpf == null ? null : hashService.hash(cpf)).orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));


        if (!usuario.isAtivo()) {
            throw new IllegalArgumentException("Usuário desativado.");
        }

        // Valida código
        if (usuario.getVerificationCode() == null || !usuario.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Código inválido ou expirado.");
        }

        // Valida expiração
        if (usuario.getVerificationCodeExpiresAt() == null || 
            usuario.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Código inválido ou expirado.");
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
            Optional<Usuario> usuarioByEmail = usuarioRepository.findByEmailHash(hashService.hash(patchRequestDTO.getEmail()));
            if (usuarioByEmail.isPresent() && !usuarioByEmail.get().getId().equals(id))  throw new IllegalArgumentException("Email já cadastrado");
            usuarioExistente.setEmailHash(hashService.hash(patchRequestDTO.getEmail()));
        }
        if (patchRequestDTO.getCpf() != null) {
            Optional<Usuario> usuarioByCpf = usuarioRepository.findByCpfHash(hashService.hash(patchRequestDTO.getCpf()));
            if (usuarioByCpf.isPresent() && !usuarioByCpf.get().getId().equals(id)) throw new IllegalArgumentException("CPF já cadastrado");
            usuarioExistente.setCpfHash(hashService.hash(patchRequestDTO.getCpf()));
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
        usuario.setDesativadoEm(OffsetDateTime.now(DateUtils.FUSO_BRASIL));
        usuarioRepository.save(usuario);
    }

    public List<Usuario> findAllGestoresWithFiltros(GestorFiltrosDTO filtros) {
        return usuarioRepository.findAll(GestorSpecification.filtrar(filtros));
    }

    @Transactional
    public Usuario createMotoristaByGoogleAccount(String name, String email, String googleId){
        Usuario novoUsuario = new Usuario();
        novoUsuario.setAtivo(true);
        novoUsuario.setEmailHash(hashService.hash(email));
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
        usuarioCadastrado.setAceitouTermosEm(OffsetDateTime.now(DateUtils.FUSO_BRASIL));
        usuarioCadastrado.setVersaoTermos(UsuarioUtils.VERSAO_ATUAL_TERMOS);
        usuarioCadastrado.setTelefoneHash(hashService.hash(request.getTelefone()));
        usuarioCadastrado.setTelefoneCripto(criptoService.encrypt(request.getTelefone()));
        usuarioCadastrado.setTelefoneLast4(UsuarioUtils.gerarLastN(request.getTelefone(), 4));
        usuarioCadastrado.setPersonalDataKeyVersion(activeKeyVersion);
        usuarioCadastrado.setSenha((request.getSenha() != null ? passwordEncoder.encode(request.getSenha()) : null));

        return usuarioRepository.save(usuarioCadastrado);

    }  
}