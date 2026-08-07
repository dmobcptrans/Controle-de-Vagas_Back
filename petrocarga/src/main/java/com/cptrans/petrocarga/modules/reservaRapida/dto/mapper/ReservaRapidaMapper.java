package com.cptrans.petrocarga.modules.reservaRapida.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper.EnderecoVagaMapper;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.reservaRapida.dto.request.ReservaRapidaRequestDTO;
import com.cptrans.petrocarga.modules.reservaRapida.dto.response.ReservaRapidaResponseDTO;
import com.cptrans.petrocarga.modules.reservaRapida.entity.ReservaRapida;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.veiculo.utils.VeiculoUtils;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservaRapidaMapper {
    private final UsuarioMapper usuarioMapper;
    private final EnderecoVagaMapper enderecoVagaMapper;

    public ReservaRapida toEntity(ReservaRapidaRequestDTO request, Vaga vaga) {
        return new ReservaRapida(
            vaga,
            request.getTipoVeiculo(),
            request.getPlaca() != null ? VeiculoUtils.normalizarEValidar(request.getPlaca()) : null,
            DateUtils.fusoHorarioBrasilia(request.getInicio()),
            DateUtils.fusoHorarioBrasilia(request.getFim()),
            request.getPosicaoPerpendicular(),
            request.getCidadeOrigem(),
            request.getEntradaCidade()
        );
    }

    public ReservaRapidaResponseDTO toResponse(ReservaRapida reservaRapida) {
        if (reservaRapida == null) return null;
        Vaga vaga = reservaRapida.getVaga();
        EnderecoVaga enderecoVaga = vaga != null ? vaga.getEndereco() : null;
        Agente agente = reservaRapida.getAgente();
        ReservaRapidaResponseDTO response = new ReservaRapidaResponseDTO(
            reservaRapida.getId(),
            vaga != null ? vaga.getId() : null,
            agente != null ? agente.getId() : null,
            enderecoVaga != null ? enderecoVaga.getLogradouro() : null,
            enderecoVaga != null ? enderecoVaga.getBairro() : null,
            reservaRapida.getTipoVeiculo(),
            reservaRapida.getPlaca(),
            reservaRapida.getInicio(),
            reservaRapida.getFim(),
            reservaRapida.getCriadoEm(),
            reservaRapida.getStatus(),
            reservaRapida.getPosicaoPerpendicular(),
            reservaRapida.getCidadeOrigem(),
            reservaRapida.getEntradaCidade()
        );
        response.formatarDados();
        return response;
    }  

    public List<ReservaRapidaResponseDTO> toResponseList(List<ReservaRapida> reservaRapidas) {
        if (reservaRapidas == null || reservaRapidas.isEmpty()) return null;
        return reservaRapidas.stream().map(this::toResponse).toList();
    }

    public ReservaDTO toReservaDTO(ReservaRapida reserva, String cpfOrCnpjCriador) {
        if (reserva == null) return null;
        Vaga vaga = reserva.getVaga();
        EnderecoVaga enderecoVaga = vaga != null ? vaga.getEndereco() : null;
        Usuario criadoPor = reserva.getAgente() != null ? reserva.getAgente().getUsuario() : null;
        ReservaDTO response = new ReservaDTO(
            reserva.getId(),
            vaga != null ? vaga.getId() : null,
            null,
            null,
            null,
            vaga != null ? vaga.getNumeroEndereco() : null,
            vaga != null ? vaga.getReferenciaEndereco() : null,
            enderecoVagaMapper.toResponse(enderecoVaga),
            reserva.getInicio(),
            reserva.getFim(),
            reserva.getTipoVeiculo().getComprimento(),
            reserva.getPlaca(),
            null,
            null,
            null,
            null,
            reserva.getCidadeOrigem(),
            reserva.getEntradaCidade(),
            reserva.getStatus(),
            null,
            null,
            null,
            usuarioMapper.toResponseSimplificado(criadoPor, cpfOrCnpjCriador),
            reserva.getCriadoEm(),
            reserva.getPosicaoPerpendicular()
        );
        response.formatarDados();
        return response;
    }

}