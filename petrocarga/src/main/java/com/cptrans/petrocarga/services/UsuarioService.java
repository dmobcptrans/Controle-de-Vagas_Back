package com.cptrans.petrocarga.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cptrans.petrocarga.domain.event.UsuarioCriadoEvent;
import com.cptrans.petrocarga.dto.GestorFiltrosDTO;
import com.cptrans.petrocarga.dto.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.infrastructure.email.EmailSender;
import com.cptrans.petrocarga.infrastructure.event.SpringDomainEventPublisher;
import com.cptrans.petrocarga.models.Usuario;
import com.cptrans.petrocarga.repositories.UsuarioRepository;
import com.cptrans.petrocarga.specification.GestorSpecification;
import com.cptrans.petrocarga.utils.DateUtils;
import com.cptrans.petrocarga.utils.ReservaUtils;

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
    public Usuario createUsuario(Usuario novoUsuario, PermissaoEnum permissao) {
        if(usuarioRepository.findByEmail(novoUsuario.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
        if(usuarioRepository.findByCpf(novoUsuario.getCpf()).isPresent()) {
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

        Usuario saved = usuarioRepository.save(novoUsuario);

        // Envia código de ativação via email (assíncrono)
        // O EmailSender é @Async, exceções são tratadas pelo AsyncUncaughtExceptionHandler
        eventPublisher.publish(new UsuarioCriadoEvent(saved.getEmail(), saved.getVerificationCode(), password));

        return saved;
    }

    @Transactional
    public Usuario activateAccount(String email, String cpf, String code) {
        if((email == null && cpf == null) || (email != null && cpf != null)) {
            throw new IllegalArgumentException("Informe um email OU CPF.");
        }
        Usuario usuario = usuarioRepository.findByEmailOrCpf(email, cpf).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado."));

        if (usuario.getVerificationCode() == null || !usuario.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Código inválido.");
        }

        if (usuario.getVerificationCodeExpiresAt() == null || usuario.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Código expirado.");
        }

        if(usuario.getDesativadoEm() != null) {
            usuario.setDesativadoEm(null);
        }

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
        Usuario usuario = usuarioRepository.findByEmailOrCpf(email, cpf).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado."));

        if (usuario.getAtivo() != null && usuario.getAtivo()) {
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
        Optional<Usuario> optUsuario = usuarioRepository.findByEmailOrCpfAndAtivo(email, cpf, true);
        
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

        Usuario usuario = usuarioRepository.findByEmailOrCpf(email, cpf)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!usuario.getAtivo()) {
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

    public Usuario updateUsuario(UUID id, Usuario novoUsuario, PermissaoEnum permissao) {
        Usuario usuarioExistente = findByIdAndAtivo(id, true);

        if(!usuarioExistente.getEmail().equals(novoUsuario.getEmail())) {
            Optional<Usuario> usuarioByEmail = usuarioRepository.findByEmail(novoUsuario.getEmail());
            if (usuarioByEmail.isPresent() && !usuarioByEmail.get().getId().equals(id)) {
                throw new IllegalArgumentException("Email já cadastrado");
            }
            usuarioExistente.setEmail(novoUsuario.getEmail());
        }

        if (!usuarioExistente.getCpf().equals(novoUsuario.getCpf())) {
            Optional<Usuario> usuarioByCpf = usuarioRepository.findByCpf(novoUsuario.getCpf());
            if (usuarioByCpf.isPresent() && !usuarioByCpf.get().getId().equals(id)) {
                throw new IllegalArgumentException("CPF já cadastrado");
            }
            usuarioExistente.setCpf(novoUsuario.getCpf());
        }

        usuarioExistente.setNome(novoUsuario.getNome());
        usuarioExistente.setTelefone(novoUsuario.getTelefone());

        if (novoUsuario.getSenha() != null && !novoUsuario.getSenha().isEmpty()) {
            usuarioExistente.setSenha(passwordEncoder.encode(novoUsuario.getSenha()));
        }
        usuarioExistente.setPermissao(permissao);
        return usuarioRepository.save(usuarioExistente);
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
            Optional<Usuario> usuarioByCpf = usuarioRepository.findByCpf(patchRequestDTO.getCpf());
            if (usuarioByCpf.isPresent() && !usuarioByCpf.get().getId().equals(id)) throw new IllegalArgumentException("CPF já cadastrado");
            usuarioExistente.setCpf(patchRequestDTO.getCpf());
        }
        if (patchRequestDTO.getSenha() != null) {
            usuarioExistente.setSenha(passwordEncoder.encode(patchRequestDTO.getSenha()));
        }
        // if(permissao.equals(PermissaoEnum.MOTORISTA)) {
        //     Motorista motorista = motoristaRepository.findByUsuario(usuarioExistente).orElseThrow(() -> new IllegalArgumentException("Motorista não encontrado."));
        //     if(patchRequestDTO.getNumeroCnh().isPresent()) {
        //        Motorista motoristaByCnh = motoristaRepository.findByNumeroCnh(patchRequestDTO.getNumeroCnh().get()).get();
        //        if(!motoristaByCnh.getId().equals(motorista.getId())) throw new IllegalArgumentException("CNH já cadastrada");
        //         motorista.setNumeroCNH(patchRequestDTO.getNumeroCnh().get());
        //     }
        //     if(patchRequestDTO.getTipoCnh().isPresent()) {
        //         motorista.setTipoCNH(patchRequestDTO.getTipoCnh().get());
        //     }
        //     if(patchRequestDTO.getDataValidadeCnh().isPresent()) {
        //         if(patchRequestDTO.getDataValidadeCnh().get().isBefore(LocalDate.now())) {
        //             throw new IllegalArgumentException("Data de validade da CNH nao pode ser menor que a data atual.");
        //         }
        //         motorista.setDataValidadeCNH(patchRequestDTO.getDataValidadeCnh().get());
        //     }
        //     return motoristaRepository.save(motorista).getUsuario();
        // }
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
}