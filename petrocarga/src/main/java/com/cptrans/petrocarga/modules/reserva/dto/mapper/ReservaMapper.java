package com.cptrans.petrocarga.modules.reserva.dto.mapper;


import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;
import com.cptrans.petrocarga.modules.empresa.exceptions.EmpresaExceptions;
import com.cptrans.petrocarga.modules.empresa.repository.EmpresaRepository;
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
import com.cptrans.petrocarga.shared.utils.DateUtils;
import com.cptrans.petrocarga.shared.utils.StringUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReservaMapper {
    private final MotoristaMapper motoristaMapper;
    private final UsuarioMapper usuarioMapper;
    private final VeiculoMapper veiculoMapper;
    private final VagaMapper vagaMapper;
    private final EnderecoVagaMapper enderecoVagaMapper;
    private final EmpresaRepository empresaRepository;

    public Reserva toEntity (ReservaRequestDTO request, Vaga vaga, Motorista motorista, Veiculo veiculo, Usuario criadoPor){ 
        return new Reserva(
            vaga,
            motorista,
            veiculo,
            criadoPor,
            request.getCidadeOrigem(),
            request.getEntradaCidade(),
            DateUtils.fusoHorarioBrasilia(request.getInicio()),
            DateUtils.fusoHorarioBrasilia(request.getFim()),
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
            vagaMapper.toResponse(vaga),
            motoristaMapper.toResponseSimplificado(motorista),
            veiculoMapper.toResponse(veiculo),
            usuarioMapper.toResponse(criadoPor, cpfOrCnpjCriador),
            reserva.getCidadeOrigem(),
            reserva.getEntradaCidade(),
            reserva.getCriadoEm(),
            DateUtils.fusoHorarioBrasilia(reserva.getInicio()),
            DateUtils.fusoHorarioBrasilia(reserva.getFim()),
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
        Empresa empresa = criadoPor != null && criadoPor.getPermissao().equals(PermissaoEnum.EMPRESA) ? findEmpresaById(criadoPor.getId()) : null;
        UUID empresaId = empresa != null ? empresa.getId() : null;
        String empresaNome = empresa != null ? criadoPor.getNome() : null;
        String empresaCnpj = empresaId != null ? empresa.getCnpj() : null;

        return new ReservaDetailedResponseDTO(
            reserva.getId(),
            vaga != null ? vaga.getId() : null,
            vaga != null ? vaga.getNumeroEndereco() : null,
            vaga != null ? vaga.getReferenciaEndereco() : null,
            enderecoVaga != null ? enderecoVaga.getLogradouro() : null,
            enderecoVaga != null ? enderecoVaga.getBairro() : null,
            motorista != null ? motorista.getId() : null,
            usuarioMotorista != null ? usuarioMotorista.getNome() : null,
            motorista != null ? StringUtils.aplicarMascaraCpf(motorista.getCpfLast5()): null,
            veiculo != null ? veiculo.getId() : null,
            veiculo != null ? veiculo.getPlaca() : null,
            veiculo != null ? veiculo.getModelo() : null,
            veiculo != null ? veiculo.getMarca() : null,
            empresaId != null ? empresaId : null,
            empresaNome != null ? empresaNome : null,
            empresaCnpj != null ? StringUtils.formatarCnpj(empresaCnpj) : null,
            reserva.getCidadeOrigem(),
            reserva.getEntradaCidade(),
            reserva.getCriadoEm(),
            DateUtils.fusoHorarioBrasilia(reserva.getInicio()),
            DateUtils.fusoHorarioBrasilia(reserva.getFim()),
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
            enderecoVagaMapper.toResponse(enderecoVaga),
            DateUtils.fusoHorarioBrasilia(reserva.getInicio()),
            DateUtils.fusoHorarioBrasilia(reserva.getFim()),
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
            DateUtils.fusoHorarioBrasilia(reserva.getCriadoEm()),
            reserva.getPosicaoPerpendicular()
        );
    }

    private Empresa findEmpresaById(UUID empresaId) {
        if (empresaId == null) return null;
        return empresaRepository.findByIdAndUsuarioAtivoTrue(empresaId).orElseThrow(() -> new EmpresaExceptions.EmpresaNotFoundException());
    }
}