package com.cptrans.petrocarga.modules.usuario.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.UsuarioProviderEnum;
import com.cptrans.petrocarga.modules.messaging.email.EmailSender;
import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaEmpresaRequestDTO;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.motorista.exceptions.MotoristaExceptions;
import com.cptrans.petrocarga.modules.motorista.repository.MotoristaRepository;
import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.modules.agente.exceptions.AgenteExceptions;
import com.cptrans.petrocarga.modules.agente.repository.AgenteRepository;
import com.cptrans.petrocarga.modules.auth.dto.request.AccountActivationRequest;
import com.cptrans.petrocarga.modules.auth.dto.request.CompletarCadastroDTO;
import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.modules.cripto.CriptoService;
import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.empresa.repository.EmpresaRepository;
import com.cptrans.petrocarga.modules.events.SpringDomainEventPublisher;
import com.cptrans.petrocarga.modules.events.usuario.UsuarioCriadoEvent;
import com.cptrans.petrocarga.modules.gestor.entity.Gestor;
import com.cptrans.petrocarga.modules.gestor.exceptions.GestorExceptions;
import com.cptrans.petrocarga.modules.gestor.repository.GestorRepository;
import com.cptrans.petrocarga.modules.reserva.utils.ReservaUtils;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioPATCHRequestDTO;
import com.cptrans.petrocarga.modules.usuario.dto.request.UsuarioRequestDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.usuario.exceptions.UsuarioExceptions;
import com.cptrans.petrocarga.modules.usuario.repository.UsuarioRepository;
import com.cptrans.petrocarga.modules.usuario.specification.UsuarioSpecification;
import com.cptrans.petrocarga.modules.usuario.utils.UsuarioUtils;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.modules.veiculo.repository.VeiculoRepository;
import com.cptrans.petrocarga.shared.exceptions.GlobalHandlerExceptions;
import com.cptrans.petrocarga.shared.utils.DateUtils;
import com.cptrans.petrocarga.shared.utils.StringUtils;

import jakarta.validation.constraints.NotBlank;
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
    private final EmpresaRepository empresaRepository;
    private final MotoristaRepository motoristaRepository;
    private final AgenteRepository agenteRepository;
    private final GestorRepository gestorRepository;
    private final VeiculoRepository veiculoRepository;

    private final Sort SORT_ASC = Sort.by("nome").ascending();
    private final Sort SORT_DESC = Sort.by("nome").descending();

    public Page<Usuario> findAll(UsuarioFiltrosRequestDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, !ordem.equals(OrdemEnum.ASC) ? SORT_DESC : SORT_ASC);
        if (filtros != null){
            if (filtros.getEmail() != null && !filtros.getEmail().trim().isEmpty()){
                filtros.setEmail(hashService.hash(filtros.getEmail().trim().toLowerCase()));
            }
            if (filtros.getTelefone() != null && !filtros.getTelefone().trim().isEmpty()){
                filtros.setTelefone(hashService.hash(filtros.getTelefone().trim()));
            }
        }
        Page<Usuario> page = usuarioRepository.findAll(UsuarioSpecification.filtrar(filtros), pageable);
        return page;
    }

    @Transactional
    public Usuario findByIdAndAtivo(UUID id, Boolean ativo) {
        Usuario usuario = usuarioRepository.findByIdAndAtivo(id, ativo).orElseThrow(() -> new UsuarioExceptions.UsuarioNotFoundException());
        usuario = atualizarCriptografiaDosDadosSeNecessario(usuario);
        return usuario;
    }

    public Usuario findByIdAndAtivoTrue(UUID id) {
        return findByIdAndAtivo(id, true);
    }

    public List<Usuario> findByPermissao(PermissaoEnum permissao) {
        return usuarioRepository.findByPermissao(permissao);
    }

    public List<Usuario> findByPermissaoAndAtivo(PermissaoEnum permissao, Boolean ativo) {
        return usuarioRepository.findByPermissaoAndAtivo(permissao, ativo);
    }

    @Transactional
    public Usuario createUsuario(UsuarioRequestDTO request, String cpf, PermissaoEnum permissao) {
        String email = request.getEmail().trim().toLowerCase();
        
        if (existsByCpf(cpf)) throw new UsuarioExceptions.CpfAlreadyExistsException();

        Usuario novoUsuario = createUsuarioInativo(
            request.getNome(),
            request.getEmail(),
            request.getTelefone(),
            request.getSenha(),
            permissao
        );
        
        //Agentes e Gestores recebem uma senha aleatória na criação
        String firstPassword = null;
        if (novoUsuario.getPermissao().equals(PermissaoEnum.GESTOR) || novoUsuario.getPermissao().equals(PermissaoEnum.AGENTE)) {
            firstPassword = gerarCodigoAleatorio();
            novoUsuario.setSenha(passwordEncoder.encode(firstPassword));
        }

        //Admin já é criado ativo
        if (novoUsuario.getPermissao().equals(PermissaoEnum.ADMIN)){
            novoUsuario.setAtivo(true);
            novoUsuario.setVerificationCode(null);
            novoUsuario.setVerificationCodeExpiresAt(null);
            novoUsuario.setAceitarTermos(true);
            novoUsuario.setAceitouTermosEm(DateUtils.agora());
        }

        Usuario saved = usuarioRepository.save(novoUsuario);

        eventPublisher.publish(new UsuarioCriadoEvent(email, saved.getVerificationCode(), firstPassword));

        return saved;
    }

    @Transactional
    public void activateAccount(AccountActivationRequest request) {
        if (!request.aceitarTermos()) throw new UsuarioExceptions.TermosNotAcceptedException();
        
        String cpf = request.cpf() != null ? request.cpf().trim() : null;
        String cnpj = request.cnpj() != null ? request.cnpj().trim() : null;
        String code = request.codigo();

        //Por segurança, não revelamos se o email/cpf/cnpj existe ou não
        Optional<Usuario> usuarioOptional = findByEmailOrCpfOrCnpjAndAtivo(null, cpf, cnpj, false);

        if (usuarioOptional.isEmpty()) throw new GlobalHandlerExceptions.DadosInvalidosException();

        Usuario usuario = usuarioOptional.get();

        validarCodigoEExpiracao(code, usuario.getVerificationCode(), usuario.getVerificationCodeExpiresAt());

        if (usuario.getDesativadoEm() != null) {
            //impede que um GESTOR ou AGENTE reative seu cadastro após sua desativação
            if(usuario.getPermissao().equals(PermissaoEnum.GESTOR) || usuario.getPermissao().equals(PermissaoEnum.AGENTE)) {
                throw new IllegalArgumentException("Usuario desativado em " + usuario.getDesativadoEm() + ". Para mais informações, entre em contato com a CPTrans.");
            }
        }

        usuario.setAceitarTermos(request.aceitarTermos());
        usuario.setAceitouTermosEm(DateUtils.agora());
        usuario.setAtivo(true);
        usuario.setVerificationCode(null);
        usuario.setVerificationCodeExpiresAt(null);

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void resendActivationCode(String email, String cpf, String cnpj) {
        Optional<Usuario> usuarioOptional = findByEmailOrCpfOrCnpjAndAtivo(email, cpf, cnpj, false);

        // Por segurança, não revelamos se o email/cpf/cnpj existe ou não
        if (usuarioOptional.isEmpty()) return;

        Usuario usuario = usuarioOptional.get();
     
        if ((usuario.getPermissao().equals(PermissaoEnum.GESTOR) || usuario.getPermissao().equals(PermissaoEnum.AGENTE) || usuario.getPermissao().equals(PermissaoEnum.ADMIN)) && usuario.getDesativadoEm() != null)  {
            //impede que gestores e agentes que foram desativados reativem a conta 
            throw new IllegalArgumentException("Usuário desativado em " + usuario.getDesativadoEm() + ". Para mais informações, entre em contato com a CPTrans.");
        }

        String codeStr = gerarCodigoAleatorio();
        usuario.setVerificationCode(codeStr);
        usuario.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        String randomPassword = null;

        emailSender.sendActivationCode((email != null ? email : criptoService.decrypt(usuarioSalvo.getEmailCripto(), usuarioSalvo.getCriptoVersion())), codeStr, randomPassword);
    }

    @Transactional
    public void forgotPassword(String email, String cpf, String cnpj) {
        Optional<Usuario> optUsuario = findByEmailOrCpfOrCnpjAndAtivo(email, cpf, cnpj, true);
        
        // Por segurança, não revelamos se o email/cpf/cnpj existe ou não
        if (optUsuario.isEmpty()) return;

        Usuario usuario = optUsuario.get();

        String codeStr = gerarCodigoAleatorio();

        usuario.setVerificationCode(codeStr);
        usuario.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        usuarioRepository.save(usuario);

        emailSender.sendPasswordResetCode(email != null ? email : criptoService.decrypt(usuario.getEmailCripto(), usuario.getCriptoVersion()), codeStr);
    }

    @Transactional
    public void resetPassword(String email, String cpf, String cnpj, String code, String novaSenha) {
        
        Usuario usuario = findByEmailOrCpfOrCnpjAndAtivo(email, cpf, cnpj, true).orElseThrow(() -> new UsuarioExceptions.CodigoInvalidoOuExpiradoException());

        validarCodigoEExpiracao(code, usuario.getVerificationCode(), usuario.getVerificationCodeExpiresAt());

        usuario.setSenha(passwordEncoder.encode(novaSenha));

        usuario.setVerificationCode(null);
        usuario.setVerificationCodeExpiresAt(null);

        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario patchUpdate(UUID id, PermissaoEnum permissao, UsuarioPATCHRequestDTO patchRequestDTO) {
        Usuario usuarioExistente = findByIdAndAtivoTrue(id);

        if (!usuarioExistente.getPermissao().equals(permissao)) throw new AuthExceptions.UsuarioNaoAutorizadoException();
        
        usuarioExistente = atualizarCriptografiaDosDadosSeNecessario(usuarioExistente);

        if (patchRequestDTO.getNome() != null) usuarioExistente.setNome(StringUtils.formatarNome(patchRequestDTO.getNome().trim()));
        
        if (patchRequestDTO.getTelefone() != null) {
            usuarioExistente.setTelefoneHash(hashService.hash(patchRequestDTO.getTelefone()));
            usuarioExistente.setTelefoneCripto(criptoService.encrypt(patchRequestDTO.getTelefone()));
            usuarioExistente.setTelefoneLast4(UsuarioUtils.gerarLastN(patchRequestDTO.getTelefone(), 4));
        }

        if (patchRequestDTO.getEmail() != null) {
            String emailString = patchRequestDTO.getEmail().trim().toLowerCase();
            String emailHash = hashService.hash(emailString);
            if (usuarioRepository.existsByEmailHashAndIdNot(emailHash, id))  throw new UsuarioExceptions.EmailAlreadyExistsException();
            String emailCripto = criptoService.encrypt(emailString);
            usuarioExistente.setEmailHash(emailHash);
            usuarioExistente.setEmailCripto(emailCripto);
            // se o email for alterado, o googleId deve ser nulo
            if (usuarioExistente.getGoogleId() != null) {
                usuarioExistente.setGoogleId(null);
                usuarioExistente.setProvider(UsuarioProviderEnum.LOCAL);
            }
        }

        if (patchRequestDTO.getCpf() != null && existsByCpfAndIdNot(patchRequestDTO.getCpf(), id) ) throw new UsuarioExceptions.CpfAlreadyExistsException();

        if (patchRequestDTO.getSenha() != null) usuarioExistente.setSenha(passwordEncoder.encode(patchRequestDTO.getSenha()));

        return usuarioRepository.save(usuarioExistente);
    }
    public void desativarById(UUID id) {
        Usuario usuario = findByIdAndAtivo(id, true);
        if (
            (usuario.getPermissao().equals(PermissaoEnum.MOTORISTA) || 
            usuario.getPermissao().equals(PermissaoEnum.EMPRESA)) && 
            reservaUtils.existsAtivaByUsuarioId(id)){
            throw new UsuarioExceptions.PossuiReservaAtivaException();
        }
        usuario.setAtivo(false);
        usuario.setDesativadoEm(DateUtils.agora());
        usuarioRepository.save(usuario);
    }

    public Usuario createMotoristaByGoogleAccount(String name, String email, String googleId){
        Usuario novoUsuario = new Usuario();
        if (email != null) email = email.trim().toLowerCase();
        novoUsuario.setAtivo(true);
        novoUsuario.setEmailHash(hashService.hash(email));
        novoUsuario.setEmailCripto(criptoService.encrypt(email));
        novoUsuario.setNome(StringUtils.formatarNome(name));
        novoUsuario.setGoogleId(googleId);
        novoUsuario.setProvider(UsuarioProviderEnum.GOOGLE);
        novoUsuario.setPermissao(PermissaoEnum.MOTORISTA);
        novoUsuario.setCriptoVersion(criptoService.getActiveKeyVersion());

        return usuarioRepository.save(novoUsuario);
    }

    @Transactional
    public Usuario completarCadastro(CompletarCadastroDTO request, UUID usuarioId){
        if (!request.getAceitarTermos()) throw new UsuarioExceptions.TermosNotAcceptedException();
        if (existsByCpfAndIdNot(request.getCpf().trim(), usuarioId)) throw new UsuarioExceptions.CpfAlreadyExistsException();
        Usuario usuarioCadastrado = findByIdAndAtivo(usuarioId, true);
        
        // Verifica se o usuário já completou o cadastro
        if (
            usuarioCadastrado.getAceitarTermos() &&
            usuarioCadastrado.getAceitouTermosEm() != null &&
            usuarioCadastrado.getVersaoTermos().equals(UsuarioUtils.VERSAO_ATUAL_TERMOS) &&
            usuarioCadastrado.getTelefoneHash() != null &&
            usuarioCadastrado.getTelefoneCripto() != null &&
            usuarioCadastrado.getTelefoneLast4() != null &&
            usuarioCadastrado.getCriptoVersion() != null
        ) return usuarioCadastrado;

        usuarioCadastrado.setAceitarTermos(request.getAceitarTermos());
        usuarioCadastrado.setAceitouTermosEm(DateUtils.agora());
        usuarioCadastrado.setVersaoTermos(UsuarioUtils.VERSAO_ATUAL_TERMOS);
        usuarioCadastrado.setTelefoneHash(hashService.hash(request.getTelefone()));
        usuarioCadastrado.setTelefoneCripto(criptoService.encrypt(request.getTelefone()));
        usuarioCadastrado.setTelefoneLast4(UsuarioUtils.gerarLastN(request.getTelefone(), 4));
        usuarioCadastrado.setCriptoVersion(criptoService.getActiveKeyVersion());
        usuarioCadastrado.setSenha((request.getSenha() != null ? passwordEncoder.encode(request.getSenha()) : null));

        return usuarioRepository.save(usuarioCadastrado);

    }  

    public void reativarAgenteOuGestorDeletado(UUID usuarioId){
        Usuario usuario = usuarioRepository.findByIdAndAtivoAndPermissaoInAndDesativadoEmNotNull(usuarioId, false, List.of(PermissaoEnum.AGENTE, PermissaoEnum.GESTOR))
            .orElseThrow(() -> new UsuarioExceptions.UsuarioNotFoundException());

        usuario.setDesativadoEm(null);
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }

    public Optional<Usuario> findByEmailOrCpfOrCnpjAndAtivo(String email, String cpf, String cnpj, Boolean ativo) {
        if ((email == null && cpf == null && cnpj == null) || (email != null && cpf != null && cnpj != null)) {
            throw new UsuarioExceptions.EmailOrCpfOrCnpjRequiredException();
        }
        for (int version = hashService.getPeppers().keySet().size(); version > 0; version--) {
            if (email != null) {
                String emailHash = hashService.hash(email.trim().toLowerCase(), version);
                Optional<Usuario> userByEmailOpt = usuarioRepository.findByEmailHashAndAtivo(emailHash, ativo);
                if (userByEmailOpt.isPresent()) return userByEmailOpt;
            }

            if (cnpj != null) {
                Optional<Empresa> empresaOpt = empresaRepository.findByCnpjAndUsuarioAtivo(cnpj.trim(), ativo);
                if (empresaOpt.isPresent()) return Optional.of(empresaOpt.get().getUsuario());
            }

            if (cpf != null) {
                String cpfHash = hashService.hash(cpf.trim(), version);
                
                Optional<Motorista> motoristaOpt = motoristaRepository.findByCpfHashAndUsuarioAtivo(cpfHash, ativo);
                if (motoristaOpt.isPresent()) return Optional.of(motoristaOpt.get().getUsuario());

                Optional<Agente> agenteOpt = agenteRepository.findByCpfHashAndUsuarioAtivo(cpfHash, ativo);
                if (agenteOpt.isPresent()) return Optional.of(agenteOpt.get().getUsuario());
                
                Optional<Gestor> gestorOpt = gestorRepository.findByCpfHashAndUsuarioAtivo(cpfHash, ativo);
                if (gestorOpt.isPresent()) return Optional.of(gestorOpt.get().getUsuario());
            }
        };
        return Optional.empty();
    }

    @Transactional
    public Usuario createMotoristaEmpresa(MotoristaEmpresaRequestDTO request, String email) {
        if (existsByCpf(request.getCpf())) throw new UsuarioExceptions.CpfAlreadyExistsException();
        email = email.trim().toLowerCase();
        Usuario novoUsuario = createUsuarioInativo(
            request.getNome(),
            email,
            request.getTelefone(),
            request.getSenha(),
            PermissaoEnum.MOTORISTA
        );
        Usuario novoUsuarioSalvo = usuarioRepository.save(novoUsuario);
        eventPublisher.publish(new UsuarioCriadoEvent(email, novoUsuarioSalvo.getVerificationCode(), null));
        return novoUsuarioSalvo;
    }

    public boolean existsByEmailAndPermissaoNot(@NotBlank String email, @NotBlank PermissaoEnum permissao) {
        return usuarioRepository.existsByEmailHashAndPermissaoNot(email.trim().toLowerCase(), permissao);
    }

    private String gerarCodigoAleatorio(){
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(1_000_000);
        return String.format("%06d", code);
    }

    private Boolean existsByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) return false;
        String cpfHash = hashService.hash(cpf);
        Boolean existeMotoristaByCpfHash = motoristaRepository.existsByCpfHash(cpfHash);
        Boolean existeAgenteByCpfHash = agenteRepository.existsByCpfHash(cpfHash);
        Boolean existeGestorByCpfHash = gestorRepository.existsByCpfHash(cpfHash);
        return existeMotoristaByCpfHash || existeAgenteByCpfHash || existeGestorByCpfHash;
    }

    private Boolean existsByCpfAndIdNot(String cpf, UUID id) {
        if (cpf == null || cpf.trim().isEmpty()) return false;
        String cpfHash = hashService.hash(cpf);
        Boolean existeMotoristaByCpfHash = motoristaRepository.existsByCpfHashAndIdNot(cpfHash, id);
        Boolean existeAgenteByCpfHash = agenteRepository.existsByCpfHashAndIdNot(cpfHash, id);
        Boolean existeGestorByCpfHash = gestorRepository.existsByCpfHashAndIdNot(cpfHash, id);
        return existeMotoristaByCpfHash || existeAgenteByCpfHash || existeGestorByCpfHash;
    }

    //caso as chaves de criptografia e hash do usuario sejam diferentes das chaves ativas, atualiza os dados criptografados/hasheados
    @Transactional
    public Usuario atualizarCriptografiaDosDadosSeNecessario(Usuario usuario){
        Integer criptoVersion = usuario.getCriptoVersion();
        Integer activeKeyVersion = criptoService.getActiveKeyVersion();
        Integer hashVersion = usuario.getHashVersion();
        Integer activeHashVersion = hashService.getActivePepperVersion();

        if (criptoVersion.equals(activeKeyVersion) && hashVersion.equals(activeHashVersion)) return usuario;
        
        String email = criptoService.decrypt(usuario.getEmailCripto(), criptoVersion);
        String novoEmailCripto = criptoService.encrypt(email);
        String novoEmailHash = hashService.hash(email);
        String telefone = criptoService.decrypt(usuario.getTelefoneCripto(), criptoVersion);
        String novoTelefoneCripto = criptoService.encrypt(telefone);
        String novoTelefoneHash = hashService.hash(telefone);
        usuario.setEmailCripto(novoEmailCripto);
        usuario.setEmailHash(novoEmailHash);
        usuario.setTelefoneCripto(novoTelefoneCripto);
        usuario.setTelefoneHash(novoTelefoneHash);
        usuario.setCriptoVersion(activeKeyVersion);
        usuario.setHashVersion(activeHashVersion);
        
        switch (usuario.getPermissao()) {
            case GESTOR:
                Gestor gestor = gestorRepository.findById(usuario.getId()).orElseThrow(() -> new GestorExceptions.GestorNotFoundException());
                String cpfGestor = criptoService.decrypt(gestor.getCpfCripto(), criptoVersion);
                gestor.setCpfCripto(criptoService.encrypt(cpfGestor));
                gestor.setCpfHash(hashService.hash(cpfGestor));
                gestorRepository.save(gestor);
                break;
            case MOTORISTA:
                Motorista motorista = motoristaRepository.findById(usuario.getId()).orElseThrow(() -> new MotoristaExceptions.MotoristaNotFoundException());
                String cpfMotorista = criptoService.decrypt(motorista.getCpfCripto(), criptoVersion);
                motorista.setCpfCripto(criptoService.encrypt(cpfMotorista));
                motorista.setCpfHash(hashService.hash(cpfMotorista));
                motoristaRepository.save(motorista);
                break;
            case AGENTE:
                Agente agente = agenteRepository.findById(usuario.getId()).orElseThrow(() -> new AgenteExceptions.AgenteNotFoundException());
                String cpfAgente = criptoService.decrypt(agente.getCpfCripto(), criptoVersion);
                agente.setCpfCripto(criptoService.encrypt(cpfAgente));
                agente.setCpfHash(hashService.hash(cpfAgente));
                agenteRepository.save(agente);
                break;
            default:
                break;
        }

        if ((usuario.getPermissao().equals(PermissaoEnum.MOTORISTA) || usuario.getPermissao().equals(PermissaoEnum.EMPRESA) && (usuario.getVeiculos() != null && !usuario.getVeiculos().isEmpty()))){
            for (Veiculo veiculo : usuario.getVeiculos()){
                if (veiculo.getCpfProprietarioCripto() != null){
                    String cpfProprietario = criptoService.decrypt(veiculo.getCpfProprietarioCripto(), criptoVersion);
                    String novoCpfProprietarioCripto = criptoService.encrypt(cpfProprietario);
                    String novoCpfProprietarioHash = hashService.hash(cpfProprietario);
                    veiculo.setCpfProprietarioCripto(novoCpfProprietarioCripto);
                    veiculo.setCpfProprietarioHash(novoCpfProprietarioHash);
                    veiculoRepository.save(veiculo);
                }
            }
        }
        return usuarioRepository.save(usuario);
    }

    private void validarCodigoEExpiracao(String code, String usuarioVerificationCode, LocalDateTime verificationCodeExpiresAt) {
        if (
            code == null || 
            usuarioVerificationCode == null || 
            !usuarioVerificationCode.equals(code) ||
            verificationCodeExpiresAt == null ||
            verificationCodeExpiresAt.isBefore(LocalDateTime.now())
        ) throw new UsuarioExceptions.CodigoInvalidoOuExpiradoException();
    }

    private Usuario createUsuarioInativo(String nome, String email, String telefone, String senha, PermissaoEnum permissao) {
        nome = StringUtils.formatarNome(nome.trim());
        email = email.trim().toLowerCase();
        telefone = telefone.trim();
        String emailHash = hashService.hash(email);

        if (usuarioRepository.existsByEmailHash(emailHash)) throw new UsuarioExceptions.EmailAlreadyExistsException();

        String emailCripto = criptoService.encrypt(email);
        String telefoneHash = hashService.hash(telefone);
        String telefoneCripto = criptoService.encrypt(telefone);
        String telefoneLast4 = UsuarioUtils.gerarLastN(telefone, 4);

        Usuario novoUsuario = new Usuario();
        
        novoUsuario.setNome(nome);
        novoUsuario.setEmailHash(emailHash);
        novoUsuario.setEmailCripto(emailCripto);
        novoUsuario.setTelefoneHash(telefoneHash);
        novoUsuario.setTelefoneCripto(telefoneCripto);
        novoUsuario.setTelefoneLast4(telefoneLast4);
        novoUsuario.setCriptoVersion(criptoService.getActiveKeyVersion());
        novoUsuario.setSenha(senha != null ? passwordEncoder.encode(senha.trim()) : null);
        novoUsuario.setAtivo(false);
        novoUsuario.setVerificationCode(gerarCodigoAleatorio());
        novoUsuario.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));
        novoUsuario.setPermissao(permissao);
        return novoUsuario;
    }

}