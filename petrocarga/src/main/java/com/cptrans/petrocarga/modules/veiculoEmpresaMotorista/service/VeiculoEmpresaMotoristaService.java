package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.auth.exceptions.AuthExceptions;
import com.cptrans.petrocarga.modules.auth.utils.AuthUtils;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.empresa.exceptions.EmpresaExceptions;
import com.cptrans.petrocarga.modules.empresa.repository.EmpresaRepository;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.motorista.exceptions.MotoristaExceptions;
import com.cptrans.petrocarga.modules.motorista.repository.MotoristaRepository;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.modules.veiculo.exceptions.VeiculoExceptions;
import com.cptrans.petrocarga.modules.veiculo.repository.VeiculoRepository;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.mapper.VeiculoEmpresaMotoristaMapper;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.response.VeiculoEmpresaMotoristaResponseDTO;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity.VeiculoEmpresaMotorista;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.exceptions.VeiculoEmpresaMotoristaExceptions;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.repository.VeiculoEmpresaMotoristaRepository;
import com.cptrans.petrocarga.security.UserAuthenticated;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VeiculoEmpresaMotoristaService {
    private final VeiculoEmpresaMotoristaRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final EmpresaRepository empresaRepository;
    private final MotoristaRepository motoristaRepository;

    public VeiculoEmpresaMotoristaResponseDTO vincularMotoristaAoVeiculo(UserAuthenticated usuarioAutenticado, UUID veiculoId, UUID motoristaId) {
        List<String> roles = AuthUtils.getRoles(usuarioAutenticado);
        if (!roles.contains(PermissaoEnum.ADMIN.getRole()) && !roles.contains(PermissaoEnum.EMPRESA.getRole())) throw new AuthExceptions.UsuarioNaoAutorizadoException();
        
        if (repository.existsByVeiculoIdAndMotoristaId(veiculoId, motoristaId)) throw new VeiculoEmpresaMotoristaExceptions.VeiculoEmpresaMotoristaJaVinculadoException();
        
        if (roles.contains(PermissaoEnum.EMPRESA.getRole())){
            Veiculo veiculo = veiculoRepository.findByIdAndAtivoTrueAndUsuarioIdAndUsuarioAtivoTrueAndUsuarioPermissao(veiculoId, usuarioAutenticado.id(), PermissaoEnum.EMPRESA).orElseThrow(() -> new VeiculoExceptions.VeiculoNotFoundException());
            Empresa empresa = empresaRepository.findByUsuarioIdAndUsuarioAtivoTrue(usuarioAutenticado.id()).orElseThrow(() -> new EmpresaExceptions.EmpresaNotFoundException());
            Motorista motorista = motoristaRepository.findByIdAndUsuarioAtivoTrueAndEmpresaId(motoristaId, empresa.getId()).orElseThrow(() -> new MotoristaExceptions.MotoristaNotFoundException());
            VeiculoEmpresaMotorista veiculoEmpresaMotorista = repository.save(new VeiculoEmpresaMotorista(veiculo, motorista));
            return VeiculoEmpresaMotoristaMapper.toResponseDTO(veiculoEmpresaMotorista); 
        }

        Veiculo veiculo = veiculoRepository.findByIdAndAtivoTrueAndUsuarioAtivoTrue(veiculoId).orElseThrow(() -> new VeiculoExceptions.VeiculoNotFoundException());
        if (veiculo.getUsuario().getPermissao() != PermissaoEnum.EMPRESA) throw new VeiculoExceptions.VeiculoNaoPertenceEmpresaException();
        
        Motorista motorista = motoristaRepository.findByIdAndUsuarioAtivoTrue(motoristaId).orElseThrow(() -> new MotoristaExceptions.MotoristaNotFoundException());
        if (motorista.getEmpresa() == null) throw new MotoristaExceptions.MotoristaNaoPossuiEmpresaException();
        if (!motorista.getEmpresa().getUsuario().getId().equals(veiculo.getUsuario().getId())) throw new MotoristaExceptions.MotoristaJaPossuiEmpresaException();
        
        VeiculoEmpresaMotorista veiculoEmpresaMotorista = repository.save(new VeiculoEmpresaMotorista(veiculo, motorista));
        return VeiculoEmpresaMotoristaMapper.toResponseDTO(veiculoEmpresaMotorista);
    }

    public void desvincularMotoristaDoVeiculo(UserAuthenticated usuarioAutenticado, UUID veiculoId, UUID motoristaId) {
        List<String> roles = AuthUtils.getRoles(usuarioAutenticado);

        VeiculoEmpresaMotorista veiculoEmpresaMotorista = repository.findByVeiculoIdAndMotoristaId(veiculoId, motoristaId).orElseThrow(() -> new VeiculoEmpresaMotoristaExceptions.VeiculoEmpresaMotoristaNotFoundException());
        
        if (roles.contains(PermissaoEnum.EMPRESA.getRole()) && !usuarioAutenticado.id().equals(veiculoEmpresaMotorista.getMotorista().getEmpresa().getUsuario().getId())) throw new AuthExceptions.UsuarioNaoAutorizadoException();

        repository.delete(veiculoEmpresaMotorista);
    }   

}
