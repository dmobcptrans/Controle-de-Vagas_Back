package com.cptrans.petrocarga.modules.usuario.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.enums.UsuarioProviderEnum;
import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.modules.agente.exceptions.AgenteExceptions;
import com.cptrans.petrocarga.modules.agente.repository.AgenteRepository;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.empresa.exceptions.EmpresaExceptions;
import com.cptrans.petrocarga.modules.empresa.repository.EmpresaRepository;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.motorista.exceptions.MotoristaExceptions;
import com.cptrans.petrocarga.modules.motorista.repository.MotoristaRepository;
import com.cptrans.petrocarga.modules.usuario.dto.response.DadosExtras;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;

import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class UsuarioMapper {
    private final CriptoUtils criptoUtils;
    private final MotoristaRepository motoristaRepository;
    private final EmpresaRepository empresaRepository;
    private final AgenteRepository agenteRepository;

    public UsuarioResponseDTO toResponse(Usuario usuario, String cpfOrCnpj) {
        if (usuario == null) return null;
        cpfOrCnpj = cpfOrCnpj != null && cpfOrCnpj.length() > 14 ? criptoUtils.decrypt(cpfOrCnpj, usuario.getCriptoVersion()) : cpfOrCnpj;
        String cpf = cpfOrCnpj != null && cpfOrCnpj.length() == 11 ? cpfOrCnpj : null;
        String cnpj = cpfOrCnpj != null && cpfOrCnpj.length() == 14 ? cpfOrCnpj : null;
        
        UsuarioResponseDTO response = new UsuarioResponseDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getTelefoneCripto(),
            usuario.getEmailCripto(),
            cpf,
            cnpj,
            usuario.getPermissao(),
            usuario.getCriadoEm(),
            usuario.getAtivo(),
            usuario.getDesativadoEm(),
            resolveDadosExtras(usuario, cpf)
        );
        
        response = criptoUtils.decrypt(response, usuario.getCriptoVersion());    
        response.formatarDados();
        return response;
    }

    public List<UsuarioResponseDTO> toResponseList(List<Usuario> usuarios, String cpfOrCnpj) {
        if (usuarios == null || usuarios.isEmpty()) return List.of();
        return usuarios.stream().map(u -> toResponse(u, cpfOrCnpj)).toList();
    }

    public UsuarioSimplificadoResponseDTO toResponseSimplificado(Usuario usuario, String cnpj) {
        if (usuario == null) return null;
        if (cnpj == null || cnpj.length() != 14) cnpj = null;

        UsuarioSimplificadoResponseDTO response = criptoUtils.decrypt(
            new UsuarioSimplificadoResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getTelefoneCripto(),
                usuario.getEmailCripto(),
                cnpj,
                usuario.getPermissao(),
                usuario.getAtivo()
            ), usuario.getCriptoVersion());
        response.formatarDados();
        return response;
    }

    private boolean resolvePossuiVeiculoAtivo(List<Veiculo> veiculosAtivos, Motorista motorista, Empresa empresa){
        boolean possuiVeiculos = false;

        if (motorista != null ){
            if (motorista.getVeiculoEmpresaMotoristaAtivos() != null && !motorista.getVeiculoEmpresaMotoristaAtivos().isEmpty()) possuiVeiculos = true;
            if (veiculosAtivos != null && !veiculosAtivos.isEmpty()) possuiVeiculos = true;
        } else if (empresa != null) {
            if (empresa.getVeiculoEmpresaMotoristaAtivos() != null && !empresa.getVeiculoEmpresaMotoristaAtivos().isEmpty()) possuiVeiculos = true;
            if (veiculosAtivos != null && !veiculosAtivos.isEmpty()) possuiVeiculos = true;
        }
       
        return possuiVeiculos;
    }

    private DadosExtras resolveDadosExtras(Usuario usuario, String cpf) {
        PermissaoEnum permissao = usuario.getPermissao();
        switch (permissao) {
            case MOTORISTA:
                Motorista motorista = motoristaRepository.findById(
                        usuario.getId()
                    ).orElseThrow(
                        () -> new MotoristaExceptions.MotoristaNotFoundException()
                    );
                Empresa empresa = motorista.getEmpresa();
                return new DadosExtras(
                        empresa != null ? empresa.getId() : null,
                        empresa != null ? empresa.getCnpj() : null,
                        empresa != null ? empresa.getUsuario().getNome() : null,
                        isNovoMotoristaCreatedByGoogle(usuario, cpf) ? 
                        false : resolvePossuiVeiculoAtivo(usuario.getVeiculosAtivos(), motorista, null)
                    );

            case AGENTE:
                Agente agente = agenteRepository.findById(
                        usuario.getId()
                    ).orElseThrow(
                        () -> new AgenteExceptions.AgenteNotFoundException()
                    );

                return new DadosExtras(
                    agente.getMatricula()
                );

            case EMPRESA:
                Empresa empresaUsuario = empresaRepository.findById(
                        usuario.getId()
                    ).orElseThrow(
                        () -> new EmpresaExceptions.EmpresaNotFoundException()
                    );

                return new DadosExtras(
                    resolvePossuiVeiculoAtivo(usuario.getVeiculosAtivos(), null, empresaUsuario)
                );
            default:
                return null;
        }
    }

    private boolean isNovoMotoristaCreatedByGoogle(Usuario usuario, String cpf) {
        if (usuario == null) return false;
        return (usuario.getPermissao().equals(PermissaoEnum.MOTORISTA) && usuario.getProvider().equals(UsuarioProviderEnum.GOOGLE) && !usuario.getAceitarTermos() && usuario.getGoogleId() != null && cpf == null);
    }
}