package com.cptrans.petrocarga.modules.vaga.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper.EnderecoVagaMapper;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.mapper.OperacaoVagaMapper;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;

@Component
public class VagaMapper {
    public static VagaResponseDTO toResponse(Vaga vaga){
        if (vaga == null) return null;
        return new VagaResponseDTO(
            vaga.getId(),
            EnderecoVagaMapper.toResponse(vaga.getEndereco()),
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
            OperacaoVagaMapper.toResponseSet(vaga.getOperacoesVaga())
        );
    } 

    public static List<VagaResponseDTO> toResponseList(List<Vaga> vagas){
        if (vagas == null || vagas.isEmpty()) return List.of();
        return vagas.stream().map(VagaMapper::toResponse).toList();
    }
}