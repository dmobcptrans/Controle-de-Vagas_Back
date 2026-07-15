package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.enums.OrdemEnum;
import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.cripto.HashService;
import com.cptrans.petrocarga.modules.motorista.dto.mapper.MotoristaMapper;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.motorista.exceptions.MotoristaExceptions;
import com.cptrans.petrocarga.modules.motorista.repository.MotoristaRepository;
import com.cptrans.petrocarga.modules.veiculo.dto.mapper.VeiculoMapper;
import com.cptrans.petrocarga.modules.veiculo.dto.response.VeiculoResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.modules.veiculo.exceptions.VeiculoExceptions;
import com.cptrans.petrocarga.modules.veiculo.repository.VeiculoRepository;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.mapper.VeiculoEmpresaMotoristaMapper;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.request.VeiculoEmpresaMotoristaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.response.VeiculoEmpresaMotoristaResponseDTO;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity.VeiculoEmpresaMotorista;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.exceptions.VeiculoEmpresaMotoristaExceptions;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.repository.VeiculoEmpresaMotoristaRepository;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.specification.VeiculoEmpresaMotoristaSpecification;
import com.cptrans.petrocarga.shared.dto.response.PageResponseDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VeiculoEmpresaMotoristaService {
    private final VeiculoEmpresaMotoristaRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final MotoristaRepository motoristaRepository;
    private final VeiculoEmpresaMotoristaMapper veiculoEmpresaMotoristaMapper;
    private final VeiculoMapper veiculoMapper;
    private final MotoristaMapper motoristaMapper;
    private final HashService hashService;

    
    public PageResponseDTO findMotoristasByEmpresaIdAndVeiculoId(VeiculoEmpresaMotoristaFiltrosRequestDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem) {
        final Sort SORT_MOTORISTA_ASC = Sort.by("motorista.usuario.nome").ascending();
        final Sort SORT_MOTORISTA_DESC = Sort.by("motorista.usuario.nome").descending();
        
        if (filtros.getMotoristaCpf() != null) filtros.setMotoristaCpf(hashService.hash(filtros.getMotoristaCpf().trim()));
        if (filtros.getMotoristaTelefone() != null) filtros.setMotoristaTelefone(hashService.hash(filtros.getMotoristaTelefone().trim()));
        if (filtros.getMotoristaEmail() != null) filtros.setMotoristaEmail(hashService.hash(filtros.getMotoristaEmail().trim())); 
        
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, !ordem.equals(OrdemEnum.ASC) ? SORT_MOTORISTA_DESC : SORT_MOTORISTA_ASC);
        Page<VeiculoEmpresaMotorista> page = repository.findAll(VeiculoEmpresaMotoristaSpecification.filtrar(filtros), pageable);
        
        if (page == null || page.isEmpty()) return new PageResponseDTO(page);
        
        Page<MotoristaSimplificadoResponseDTO> pageResponse = page.map((vem) -> motoristaMapper.toResponseSimplificado(vem.getMotorista()));
        
        return new PageResponseDTO(pageResponse);
    }

    public PageResponseDTO findVeiculosByMotoristaId(VeiculoEmpresaMotoristaFiltrosRequestDTO filtros, int pagina, int tamanhoPagina, OrdemEnum ordem){
        final Sort SORT_VEICULO_ASC = Sort.by("veiculo.marca").and(Sort.by("veiculo.modelo").and(Sort.by("veiculo.placa").ascending()));
        final Sort SORT_VEICULO_DESC = Sort.by("veiculo.marca").and(Sort.by("veiculo.modelo").and(Sort.by("veiculo.placa").descending()));
        
        Pageable pageable = PageRequest.of(pagina, tamanhoPagina, !ordem.equals(OrdemEnum.ASC) ? SORT_VEICULO_DESC : SORT_VEICULO_ASC);
        Page<VeiculoEmpresaMotorista> page = repository.findAll(VeiculoEmpresaMotoristaSpecification.filtrar(filtros), pageable);
        
        if (page == null || page.isEmpty()) return new PageResponseDTO(page);
        
        Page<VeiculoResponseDTO> pageResponse = page.map((vem) -> veiculoMapper.toResponse(vem.getVeiculo()));
        
        return new PageResponseDTO(pageResponse);
    }

    public VeiculoEmpresaMotoristaResponseDTO vincularMotoristaAoVeiculo(UUID empresaId, UUID veiculoId, UUID motoristaId) {
        if (repository.existsByVeiculoIdAndMotoristaIdAndEmpresaId(veiculoId, motoristaId, empresaId)) throw new VeiculoEmpresaMotoristaExceptions.VeiculoEmpresaMotoristaJaVinculadoException();

        Veiculo veiculo = veiculoRepository.findByIdAndAtivoTrueAndUsuarioIdAndUsuarioAtivoTrue(veiculoId, empresaId).orElseThrow(() -> new VeiculoExceptions.VeiculoNotFoundException());
        if (veiculo.getUsuario().getPermissao() != PermissaoEnum.EMPRESA) throw new VeiculoExceptions.VeiculoNaoPertenceEmpresaException();
        
        Motorista motorista = motoristaRepository.findByIdAndUsuarioAtivoTrue(motoristaId).orElseThrow(() -> new MotoristaExceptions.MotoristaNotFoundException());
        if (motorista.getEmpresa() == null) throw new MotoristaExceptions.MotoristaNaoPossuiEmpresaException();
        if (!motorista.getEmpresa().getId().equals(empresaId)) throw new MotoristaExceptions.MotoristaJaPossuiEmpresaException();
        
        VeiculoEmpresaMotorista veiculoEmpresaMotorista = repository.save(new VeiculoEmpresaMotorista(veiculo, motorista.getEmpresa(), motorista));
        
        return veiculoEmpresaMotoristaMapper.toResponse(veiculoEmpresaMotorista);
    }

    public void desvincularMotoristaDoVeiculo(UUID empresaId, UUID veiculoId, UUID motoristaId) {
        VeiculoEmpresaMotorista veiculoEmpresaMotorista = repository.findByVeiculoIdAndMotoristaIdAndEmpresaId(veiculoId, motoristaId, empresaId).orElseThrow(() -> new VeiculoEmpresaMotoristaExceptions.VeiculoEmpresaMotoristaNotFoundException());
        repository.delete(veiculoEmpresaMotorista);
    }   

    @Transactional
    public void desvincularTodosByMotoristaId(UUID motoristaId) {
        repository.deleteAllByMotoristaId(motoristaId);
    }

    public boolean existsByEmpresaIdAndMotoristaId(UUID empresaId, UUID motoristaId) {
        return repository.existsByEmpresaIdAndMotoristaId(empresaId, motoristaId);
    }
}