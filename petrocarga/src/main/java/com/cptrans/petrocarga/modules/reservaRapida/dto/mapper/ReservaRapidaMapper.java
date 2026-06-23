package com.cptrans.petrocarga.modules.reservaRapida.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper.EnderecoVagaMapper;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.reservaRapida.dto.response.ReservaRapidaResponseDTO;
import com.cptrans.petrocarga.modules.reservaRapida.entity.ReservaRapida;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;

@Component
public class ReservaRapidaMapper {
    public static ReservaRapidaResponseDTO toResponse(ReservaRapida reservaRapida) {
        if (reservaRapida == null) return null;
        return new ReservaRapidaResponseDTO(reservaRapida);
    }  

    public static List<ReservaRapidaResponseDTO> toResponseList(List<ReservaRapida> reservaRapidas) {
        if (reservaRapidas == null || reservaRapidas.isEmpty()) return null;
        return reservaRapidas.stream().map(ReservaRapidaMapper::toResponse).toList();
    }

    public static ReservaDTO toReservaDTO(ReservaRapida reserva) {
        if (reserva == null) return null;
        Vaga vaga = reserva.getVaga();
        EnderecoVaga enderecoVaga = vaga != null ? vaga.getEndereco() : null;
        Usuario criadoPor = reserva.getAgente() != null ? reserva.getAgente().getUsuario() : null;
        return new ReservaDTO(
            reserva.getId(),
            vaga != null ? vaga.getId() : null,
            null,
            null,
            null,
            vaga != null ? vaga.getNumeroEndereco() : null,
            vaga != null ? vaga.getReferenciaEndereco() : null,
            EnderecoVagaMapper.toResponse(enderecoVaga),
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
            UsuarioMapper.toResponse(criadoPor),
            reserva.getCriadoEm(),
            reserva.getPosicaoPerpendicular()
        );
    }

}
