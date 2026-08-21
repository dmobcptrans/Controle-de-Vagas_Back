package com.cptrans.petrocarga.modules.vaga.dto.mapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.DiaSemanaEnum;
import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.modules.disponibilidadeVaga.repository.DisponibilidadeVagaRepository;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper.EnderecoVagaMapper;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.mapper.OperacaoVagaMapper;
import com.cptrans.petrocarga.modules.operacaoVaga.entity.OperacaoVaga;
import com.cptrans.petrocarga.modules.vaga.dto.request.VagaRequestDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaCoordenadaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VagaMapper {
    private final OperacaoVagaMapper operacaoVagaMapper;
    private final EnderecoVagaMapper enderecoVagaMapper;
    private final DisponibilidadeVagaRepository disponibilidadeVagaRepository;
    
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

    public VagaCoordenadaResponseDTO toCoordenadaResponse(Vaga vaga, StatusVagaEnum status){
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
            response = possuiOperacaoNosProximosDoisDias(vaga, DateUtils.agora(), response) ? response :  null;
        }
        return response;
    }

    public List<VagaCoordenadaResponseDTO> toCoordenadaResponseList(List<Vaga> vagas, StatusVagaEnum status){
        if (vagas == null || vagas.isEmpty()) return List.of();
        return vagas.stream().map(v -> toCoordenadaResponse(v, status)).toList();
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

    private boolean possuiOperacaoNosProximosDoisDias(Vaga vaga, OffsetDateTime agora, VagaCoordenadaResponseDTO response) {
        LocalDate hoje = agora.toLocalDate();

        for (int i = 0; i <= 2; i++) {
            LocalDate data = hoje.plusDays(i);

            DiaSemanaEnum diaSemana = DiaSemanaEnum.fromDayOfWeek(data.getDayOfWeek());

            OperacaoVaga operacao = vaga.getOperacoesVaga()
                .stream()
                .filter(op -> op.getDiaSemana() == diaSemana)
                .findFirst()
                .orElse(null);

            if (operacao == null) continue;

            OffsetDateTime inicioOperacao = operacao.getHoraInicio().atDate(data).atOffset(agora.getOffset());
            OffsetDateTime fimOperacao = operacao.getHoraFim().atDate(data).atOffset(agora.getOffset());

            OffsetDateTime inicioBusca = i == 0 ? agora : inicioOperacao;

            if (i == 0 && !fimOperacao.isAfter(agora)) continue;

            if (disponibilidadeVagaRepository.existsByVagaIdAndFimGreaterThanAndInicioLessThan(vaga.getId(), inicioBusca, fimOperacao)) {
                if (i == 0) response.setDisponivelAgora(true);
                return true;
            }
        }

        return false;
    }
}