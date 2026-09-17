package com.cptrans.petrocarga.modules.vaga.dto.mapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.TipoMapaVagasEnum;
import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper.EnderecoVagaMapper;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.mapper.OperacaoVagaMapper;
import com.cptrans.petrocarga.modules.operacaoVaga.entity.OperacaoVaga;
import com.cptrans.petrocarga.modules.vaga.dto.projection.ClusterMapaProjection;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaRequestDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaCoordenadaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagasClusterResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagasMapaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.vaga.service.VagaService;

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
        return vagas.stream().map(this::toResponse).filter(v -> v != null).toList();
    }

    public VagaCoordenadaResponseDTO toCoordenadaResponse(Vaga vaga, StatusVagaEnum status, Set<UUID> idsDisponiveisHoje){
        if (vaga == null) return null;
        VagaCoordenadaResponseDTO response = new VagaCoordenadaResponseDTO(
            vaga.getId(),
            vaga.getArea(),
            vaga.getStatus(),
            vaga.getLatitudeInicio(),
            vaga.getLongitudeInicio(),
            vaga.getLatitudeFim(),
            vaga.getLongitudeFim(),
            null
        );
        if (status != null && status.equals(StatusVagaEnum.DISPONIVEL)){
            if (idsDisponiveisHoje != null && !idsDisponiveisHoje.isEmpty()){
                response.setDisponivelHoje(idsDisponiveisHoje.contains(vaga.getId()));
            } else {
                response.setDisponivelHoje(false);
            }
        }
        return response;
    }

    public List<VagaCoordenadaResponseDTO> toCoordenadaResponseList(List<Vaga> vagas, StatusVagaEnum status, Set<UUID> idsDisponiveisHoje){
        if (vagas == null || vagas.isEmpty()) return List.of();
        return vagas.stream().map(v -> toCoordenadaResponse(v, status, idsDisponiveisHoje)).filter(v -> v != null).toList();
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
            vaga.getStatus(),
            vaga.getLatitudeInicio(),
            vaga.getLongitudeInicio(),
            vaga.getLatitudeFim(),
            vaga.getLongitudeFim()
        );
    }

    public List<VagaSimplificadoResponseDTO> toResponseSimplificadoList(List<Vaga> vagas){
        if (vagas == null || vagas.isEmpty()) return List.of();
        return vagas.stream().map(this::toResponseSimplificado).toList();
    }

    public VagasClusterResponseDTO toClusterResponse(ClusterMapaProjection projection){
        return new VagasClusterResponseDTO(
            projection.getLatitude(),
            projection.getLongitude(),
            projection.getQuantidade()
        );
    }

    public List<VagasClusterResponseDTO> toClusterResponseList(List<ClusterMapaProjection> clusters){
        if (clusters == null || clusters.isEmpty()) return List.of();
        return clusters.stream().map(this::toClusterResponse).filter(c -> c != null).toList();
    }

    public VagasMapaResponseDTO toVagasMapaResponse(List<VagaCoordenadaResponseDTO> vagas, List<VagasClusterResponseDTO> clusters){
        boolean limiteAtingido = (vagas != null && vagas.size() == VagaService.LIMITE_VAGAS_MAPA);
        TipoMapaVagasEnum tipo = TipoMapaVagasEnum.VAGAS;
        if (clusters != null && !clusters.isEmpty()) tipo = TipoMapaVagasEnum.CLUSTERS;
        return new VagasMapaResponseDTO(tipo, vagas, clusters, limiteAtingido);
    }
}