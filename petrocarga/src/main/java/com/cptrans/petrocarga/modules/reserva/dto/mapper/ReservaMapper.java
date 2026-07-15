package com.cptrans.petrocarga.modules.reserva.dto.mapper;


import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper.EnderecoVagaMapper;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.motorista.dto.mapper.MotoristaMapper;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.reserva.dto.request.ReservaRequestDTO;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDetailedResponseDTO;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaResponseDTO;
import com.cptrans.petrocarga.modules.reserva.entity.Reserva;
import com.cptrans.petrocarga.modules.usuario.dto.mapper.UsuarioMapper;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.vaga.dto.mapper.VagaMapper;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.veiculo.dto.mapper.VeiculoMapper;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservaMapper {
    private final MotoristaMapper motoristaMapper;
    private final UsuarioMapper usuarioMapper;
    private final VeiculoMapper veiculoMapper;

    public Reserva toEntity (ReservaRequestDTO request, Vaga vaga, Motorista motorista, Veiculo veiculo, Usuario criadoPor){ 
        return new Reserva(
            vaga,
            motorista,
            veiculo,
            criadoPor,
            request.getCidadeOrigem(),
            request.getEntradaCidade(),
            request.getInicio(),
            request.getFim(),
            request.getPosicaoPerpendicular()
        );
    }

    public ReservaResponseDTO toResponse(Reserva reserva, String cpfOrCnpjCriador) {
        if (reserva == null) return null;
        Vaga vaga = reserva.getVaga();
        Veiculo veiculo = reserva.getVeiculo();
        Motorista motorista = reserva.getMotorista();
        Usuario criadoPor = reserva.getCriadoPor();
        return new ReservaResponseDTO(
            reserva.getId(),
            VagaMapper.toResponse(vaga),
            motoristaMapper.toResponseSimplificado(motorista),
            veiculoMapper.toResponse(veiculo),
            usuarioMapper.toResponse(criadoPor, cpfOrCnpjCriador),
            reserva.getCidadeOrigem(),
            reserva.getEntradaCidade(),
            reserva.getCriadoEm(),
            reserva.getInicio(),
            reserva.getFim(),
            reserva.getStatus(),
            reserva.getCheckedIn(),
            reserva.getCheckInEm(),
            reserva.getCheckOutEm(),
            reserva.getPosicaoPerpendicular()
        );
    }

    public ReservaDetailedResponseDTO toDetailedResponse(Reserva reserva) {
        if (reserva == null) return null;
        Vaga vaga = reserva.getVaga();
        EnderecoVaga enderecoVaga = vaga != null ? vaga.getEndereco() : null;
        Motorista motorista = reserva.getMotorista();
        Usuario usuarioMotorista = motorista != null ? motorista.getUsuario() : null;
        Veiculo veiculo = reserva.getVeiculo();
        Usuario criadoPor = reserva.getCriadoPor();
        return new ReservaDetailedResponseDTO(
            reserva.getId(),
            vaga != null ? vaga.getId() : null,
            vaga != null ? vaga.getNumeroEndereco() : null,
            vaga != null ? vaga.getReferenciaEndereco() : null,
            enderecoVaga != null ? enderecoVaga.getLogradouro() : null,
            enderecoVaga != null ? enderecoVaga.getBairro() : null,
            motorista != null ? motorista.getId() : null,
            usuarioMotorista != null ? usuarioMotorista.getNome() : null,
            veiculo != null ? veiculo.getId() : null,
            veiculo != null ? veiculo.getPlaca() : null,
            veiculo != null ? veiculo.getModelo() : null,
            veiculo != null ? veiculo.getMarca() : null,
            criadoPor != null ? criadoPor.getId() : null,
            criadoPor != null ? criadoPor.getNome() : null,
            reserva.getCidadeOrigem(),
            reserva.getEntradaCidade(),
            reserva.getCriadoEm(),
            reserva.getInicio(),
            reserva.getFim(),
            reserva.getStatus()
        );
    }

    public ReservaDTO toReservaDTO(Reserva reserva, String cpfOrCnpjCriador) {
        if (reserva == null) return null;
        Vaga vaga = reserva.getVaga();
        EnderecoVaga enderecoVaga = vaga != null ? vaga.getEndereco() : null;
        Motorista motorista = reserva.getMotorista();
        Usuario usuarioMotorista = motorista != null ? motorista.getUsuario() : null;
        Veiculo veiculo = reserva.getVeiculo();
        Usuario criadoPor = reserva.getCriadoPor();
        return new ReservaDTO(
            reserva.getId(),
            vaga != null ? vaga.getId() : null,
            motorista != null ? motorista.getId() : null,
            usuarioMotorista != null ? usuarioMotorista.getNome() : null,
            motorista != null ? motorista.getCpfCripto() : null,
            vaga != null ? vaga.getNumeroEndereco() : null,
            vaga != null ? vaga.getReferenciaEndereco() : null,
            EnderecoVagaMapper.toResponse(enderecoVaga),
            reserva.getInicio(),
            reserva.getFim(),
            veiculo != null ? veiculo.getTipo().getComprimento() : null,
            veiculo != null ? veiculo.getPlaca() : null,
            veiculo != null ? veiculo.getModelo() : null,
            veiculo != null ? veiculo.getMarca() : null,
            veiculo != null ? veiculo.getCpfProprietarioCripto() : null,
            veiculo != null ? veiculo.getCnpjProprietario() : null,
            reserva.getCidadeOrigem(),
            reserva.getEntradaCidade(),
            reserva.getStatus(),
            reserva.getCheckedIn(),
            reserva.getCheckInEm(),
            reserva.getCheckOutEm(),
            usuarioMapper.toResponse(criadoPor, cpfOrCnpjCriador),
            reserva.getCriadoEm(),
            reserva.getPosicaoPerpendicular()
        );
    }
}