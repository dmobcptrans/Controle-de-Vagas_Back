package com.cptrans.petrocarga.modules.usuario.dto.mapper;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.empresa.exceptions.EmpresaExceptions;
import com.cptrans.petrocarga.modules.empresa.repository.EmpresaRepository;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.motorista.exceptions.MotoristaExceptions;
import com.cptrans.petrocarga.modules.motorista.repository.MotoristaRepository;
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

    private Boolean resolvePossuiVeiculoAtivo(PermissaoEnum permissao, UUID id, List<Veiculo> veiculosAtivos){
        Boolean possuiVeiculos = false;
        
        switch (permissao) {
            case MOTORISTA:
                Motorista motorista = motoristaRepository.findById(id).orElseThrow(() -> new MotoristaExceptions.MotoristaNotFoundException());
                if (motorista.getVeiculoEmpresaMotoristaAtivos() != null && !motorista.getVeiculoEmpresaMotoristaAtivos().isEmpty()) possuiVeiculos = true;
                if (veiculosAtivos != null && !veiculosAtivos.isEmpty()) possuiVeiculos = true;
                break;
            case EMPRESA:
                Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new EmpresaExceptions.EmpresaNotFoundException());
                if (empresa.getVeiculoEmpresaMotoristaAtivos() != null && !empresa.getVeiculoEmpresaMotoristaAtivos().isEmpty()) possuiVeiculos = true;
                if (veiculosAtivos != null && !veiculosAtivos.isEmpty()) possuiVeiculos = true;
                break;
            case ADMIN, GESTOR, AGENTE:
                possuiVeiculos = null;
                break;
            default:
                break;
        }
        return possuiVeiculos;
    }

    public UsuarioResponseDTO toResponse(Usuario usuario, String cpfOrCnpj) {
        if (usuario == null) return null;
        cpfOrCnpj = cpfOrCnpj != null && cpfOrCnpj.length() > 14 ? criptoUtils.decrypt(cpfOrCnpj, usuario.getCriptoVersion()) : cpfOrCnpj;
        String cpf = cpfOrCnpj != null && cpfOrCnpj.length() == 11 ? cpfOrCnpj : null;
        String cnpj = cpfOrCnpj != null && cpfOrCnpj.length() == 14 ? cpfOrCnpj : null;
        UsuarioResponseDTO response = criptoUtils.decrypt(
            new UsuarioResponseDTO(
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
                resolvePossuiVeiculoAtivo(usuario.getPermissao(), usuario.getId(), usuario.getVeiculosAtivos())
            ), usuario.getCriptoVersion());    
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
}