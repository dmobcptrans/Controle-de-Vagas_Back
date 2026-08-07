package com.cptrans.petrocarga.modules.vaga.dto.mapper;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper.EnderecoVagaMapper;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.mapper.OperacaoVagaMapper;
import com.cptrans.petrocarga.modules.operacaoVaga.entity.OperacaoVaga;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaRequestDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaCoordenadaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VagaMapper {
    private final OperacaoVagaMapper operacaoVagaMapper;
    private final EnderecoVagaMapper enderecoVagaMapper;
    
    public Vaga toEntity(VagaRequestDTO request, EnderecoVaga endereco){
        if (request == null) return null;
        Set<OperacaoVaga> operacoesVaga = null;
        
        Vaga vaga = new Vaga(
            endereco,
            request.getArea(),
            request.getNumeroEndereco().trim(),
            request.getReferenciaEndereco().trim(),
            request.getTipoVaga(),
            request.getLatitudeInicio(),
            request.getLongitudeInicio(),
            request.getLatitudeFim(),
            request.getLongitudeFim(),
            request.getComprimento(),
            request.getQuantidade(),
            operacoesVaga
            
        );
        
        operacoesVaga = operacaoVagaMapper.toEntitySet(request.getOperacoesVaga(), vaga);
        vaga.setOperacoesVaga(operacoesVaga);
        
        return vaga;
    }

    public VagaResponseDTO toResponse(Vaga vaga){
        if (vaga == null) return null;
        return new VagaResponseDTO(
            vaga.getId(),
            enderecoVagaMapper.toResponse(vaga.getEndereco()),
            vaga.getArea(),
            vaga.getNumeroEndereco(),
            vaga.getReferenciaEndereco(),
            vaga.getTipoVaga(),
            vaga.getLatitudeInicio(),
            vaga.getLongitudeInicio(),
            vaga.getLatitudeFim(),
            vaga.getLongitudeFim(),
            vaga.getComprimento(),
            vaga.getQuantidade(),
            vaga.getStatus(),
            operacaoVagaMapper.toResponseSet(vaga.getOperacoesVaga())
        );
    } 

    public List<VagaResponseDTO> toResponseList(List<Vaga> vagas){
        if (vagas == null || vagas.isEmpty()) return List.of();
        return vagas.stream().map(this::toResponse).toList();
    }

    public VagaCoordenadaResponseDTO toCoordenadaResponse(Vaga vaga){
        if (vaga == null) return null;
        return new VagaCoordenadaResponseDTO(
            vaga.getId(),
            vaga.getArea(),
            vaga.getStatus(),
            vaga.getLatitudeInicio(),
            vaga.getLongitudeInicio(),
            vaga.getLatitudeFim(),
            vaga.getLongitudeFim()
        );
    }

    public List<VagaCoordenadaResponseDTO> toCoordenadaResponseList(List<Vaga> vagas){
        if (vagas == null || vagas.isEmpty()) return List.of();
        return vagas.stream().map(this::toCoordenadaResponse).toList();
    }

    public VagaSimplificadoResponseDTO toResponseSimplificado(Vaga vaga){
        if (vaga == null) return null;
        EnderecoVaga enderecoVaga = vaga.getEndereco();
        return new VagaSimplificadoResponseDTO(
            vaga.getId(),
            enderecoVaga != null ? enderecoVaga.getId() : null,
            enderecoVaga != null ? enderecoVaga.getLogradouro() : null,
            enderecoVaga != null ? enderecoVaga.getBairro() : null,
            vaga.getNumeroEndereco(),
            vaga.getReferenciaEndereco(),
            vaga.getArea(),
            vaga.getTipoVaga(),
            vaga.getComprimento(),
            vaga.getQuantidade(),
            vaga.getStatus()
        );
    }

    public List<VagaSimplificadoResponseDTO> toResponseSimplificadoList(List<Vaga> vagas){
        if (vagas == null || vagas.isEmpty()) return List.of();
        return vagas.stream().map(this::toResponseSimplificado).toList();
    }
}