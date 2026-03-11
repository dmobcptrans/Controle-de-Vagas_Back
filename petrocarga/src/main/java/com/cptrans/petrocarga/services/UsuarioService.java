package com.cptrans.petrocarga.services;

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

import com.cptrans.petrocarga.domain.event.UsuarioCriadoEvent;
import com.cptrans.petrocarga.dto.CompletarCadastroDTO;
import com.cptrans.petrocarga.dto.GestorFiltrosDTO;
import com.cptrans.petrocarga.dto.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.UsuarioProviderEnum;
import com.cptrans.petrocarga.infrastructure.email.EmailSender;
import com.cptrans.petrocarga.infrastructure.event.SpringDomainEventPublisher;
import com.cptrans.petrocarga.models.Usuario;
import com.cptrans.petrocarga.repositories.UsuarioRepository;
import com.cptrans.petrocarga.specification.GestorSpecification;
import com.cptrans.petrocarga.utils.DateUtils;
import com.cptrans.petrocarga.utils.ReservaUtils;
import com.cptrans.petrocarga.utils.UsuarioUtils;

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
    private CpfHashService cpfHashService;

    @Autowired
    private CpfCriptoService cpfCriptoService;

    @Value("${app.security.cpf.active-key-version}")
    private Integer activeCpfKeyVersion;

    
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
        if(usuarioRepository.findByEmail(novoUsuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        if(usuarioRepository.findByCpfHash(cpfHashService.hash(cpf_string)).isPresent()) {
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
        novoUsuario.setCpfHash(cpfHashService.hash(cpf_string));
        novoUsuario.setCpfCripto(cpfCriptoService.encrypt(cpf_string));
        novoUsuario.setCpfKeyVersion(activeCpfKeyVersion);
        novoUsuario.setCpfLast5(cpf_string.substring((cpf_string.length() - 5)));
        
        System.out.println("cpf_string: " + cpf_string);
        System.out.println("cpf_hash: " + novoUsuario.getCpfHash());
        System.out.println("cpf_cripto: " + novoUsuario.getCpfCripto());
        System.out.println("cpf_key_version: " + novoUsuario.getCpfKeyVersion());
        System.out.println("cpf_last5: " + novoUsuario.getCpfLast5());
        Usuario saved = usuarioRepository.save(novoUsuario);

        // Envia código de ativação via email (assíncrono)
        // O EmailSender é @Async, exceções são tratadas pelo AsyncUncaughtExceptionHandler
        eventPublisher.publish(new UsuarioCriadoEvent(saved.getEmail(), saved.getVerificationCode(), password));

        return saved;
    }

    @Transactional
    public Usuario activateAccount(Boolean aceitarTermos, String cpf, String code) {
        if (aceitarTermos.equals(Boolean.FALSE)) throw new IllegalArgumentException("É necessário aceitar os termos de uso e política de privacidade para ativar a conta");
        if (cpf == null) throw new IllegalArgumentException("É necessário informar o CPF.");
        
        Usuario usuario = usuarioRepository.findByCpfHash(cpfHashService.hash(cpf)).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado."));

        if (usuario.getVerificationCode() == null || !usuario.getVerificationCode().equals(code)) throw new IllegalArgumentException("Código inválido.");

        if (usuario.getVerificationCodeExpiresAt() == null || usuario.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Código expirado.");
        }

        if (usuario.getDesativadoEm() != null) usuario.setDesativadoEm(null);

        usuario.setAceitarTermos(aceitarTermos);
        usuario.setAceitouTermosEm(OffsetDateTime.now(DateUtils.FUSO_BRASIL));
        usuario.setAtivo(true);
        usuario.setVerificationCode(null);
        usuario.setVerificationCodeExpiresAt(null);

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void resendActivationCode(String email, String cpf) {
        if ((email == null && cpf == null) || (email != null && cpf != null)) {
            throw new IllegalArgumentException("Informe um email OU CPF.");
        }
        Usuario usuario = usuarioRepository.findByEmailOrCpfHash(email, cpf == null ? null : cpfHashService.hash(cpf)).orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));

        if (usuario.isAtivo() != null && usuario.isAtivo()) {
            throw new IllegalArgumentException("Usuário já ativado.");
        }

        SecureRandom random = new SecureRandom();
        int code = random.nextInt(1_000_000);
        String codeStr = String.format("%06d", code);
        usuario.setVerificationCode(codeStr);
        usuario.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        usuarioRepository.save(usuario);
        String randomPassword = null;
        // Reenvia código de ativação via email (assíncrono)
        emailSender.sendActivationCode(usuario.getEmail(), codeStr, randomPassword);
    }

    // ==================== RECUPERAÇÃO DE SENHA ====================

    @Transactional
    public void forgotPassword(String email, String cpf) {
        if ((email == null && cpf == null) || (email != null && cpf != null)) {
            throw new IllegalArgumentException("Informe um email OU CPF.");
        }
        // Busca usuário pelo email (silenciosamente ignora se não existir por segurança)
        Optional<Usuario> optUsuario = usuarioRepository.findByEmailOrCpfHashAndAtivo(email, cpf == null ? null : cpfHashService.hash(cpf), true);
        
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
        emailSender.sendPasswordResetCode(usuario.getEmail(), codeStr);
    }

    @Transactional
    public void resetPassword(String email, String cpf, String code, String novaSenha) {
        if ((email == null && cpf == null) || (email != null && cpf != null)) {
            throw new IllegalArgumentException("Informe um email OU CPF.");
        }

        Usuario usuario = usuarioRepository.findByEmailOrCpfHash(email, cpf == null ? null : cpfHashService.hash(cpf)).orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas."));


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
        if (patchRequestDTO.getTelefone() != null) usuarioExistente.setTelefone(patchRequestDTO.getTelefone());
        if (patchRequestDTO.getEmail() != null) {
            Optional<Usuario> usuarioByEmail = usuarioRepository.findByEmail(patchRequestDTO.getEmail());
            if (usuarioByEmail.isPresent() && !usuarioByEmail.get().getId().equals(id))  throw new IllegalArgumentException("Email já cadastrado");
            usuarioExistente.setEmail(patchRequestDTO.getEmail());
        }
        if (patchRequestDTO.getCpf() != null) {
            Optional<Usuario> usuarioByCpf = usuarioRepository.findByCpfHash(cpfHashService.hash(patchRequestDTO.getCpf()));
            if (usuarioByCpf.isPresent() && !usuarioByCpf.get().getId().equals(id)) throw new IllegalArgumentException("CPF já cadastrado");
            usuarioExistente.setCpfHash(cpfHashService.hash(patchRequestDTO.getCpf()));
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
        novoUsuario.setEmail(email);
        novoUsuario.setNome(name);
        novoUsuario.setGoogleId(googleId);
        novoUsuario.setProvider(UsuarioProviderEnum.GOOGLE);
        novoUsuario.setPermissao(PermissaoEnum.MOTORISTA);
        novoUsuario.setVersaoTermos(UsuarioUtils.VERSAO_ATUAL_TERMOS);
        novoUsuario.setCpfKeyVersion(activeCpfKeyVersion);

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

        String cpfHash = cpfHashService.hash(request.getCpf());
        Optional<Usuario> usuarioByCpf = usuarioRepository.findByCpfHash(cpfHash);

        if (usuarioByCpf.isPresent() && !usuarioByCpf.get().getId().equals(usuarioCadastrado.getId())) throw new IllegalArgumentException("CPF já cadastrado.");

        usuarioCadastrado.setCpfHash(cpfHash);
        usuarioCadastrado.setCpfCripto(cpfCriptoService.encrypt(request.getCpf()));
        usuarioCadastrado.setCpfLast5(UsuarioUtils.gerarLast5(request.getCpf()));
        usuarioCadastrado.setAceitarTermos(request.getAceitarTermos());
        usuarioCadastrado.setAceitouTermosEm(OffsetDateTime.now(DateUtils.FUSO_BRASIL));
        usuarioCadastrado.setCpfKeyVersion(activeCpfKeyVersion);
        usuarioCadastrado.setVersaoTermos(UsuarioUtils.VERSAO_ATUAL_TERMOS);
        usuarioCadastrado.setTelefone(request.getTelefone());
        usuarioCadastrado.setSenha((request.getSenha() != null ? passwordEncoder.encode(request.getSenha()) : null));

        return usuarioRepository.save(usuarioCadastrado);

    }  
}