package com.cptrans.petrocarga.modules.denuncia.dto.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.denuncia.dto.response.DenunciaResponseDTO;
import com.cptrans.petrocarga.modules.denuncia.entity.Denuncia;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.mapper.EnderecoVagaMapper;
import com.cptrans.petrocarga.modules.enderecoVaga.entity.EnderecoVaga;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.reserva.entity.Reserva;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.shared.utils.CriptoUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DenunciaMapper {

    private final CriptoUtils criptoUtils;

    public DenunciaResponseDTO toResponse(Denuncia denuncia){
        if (denuncia == null) return null;
        Vaga vaga = denuncia.getVaga();
        Usuario criadoPor = denuncia.getCriadoPor();
        Reserva reserva = denuncia.getReserva();
        Veiculo veiculo = reserva != null ? reserva.getVeiculo() : null;
        Motorista motorista = reserva != null ? reserva.getMotorista() : null;
        Usuario usuarioMotorista = motorista != null ? motorista.getUsuario() : null;
        EnderecoVaga enderecoVaga = vaga != null ? vaga.getEndereco() : null;
        return criptoUtils.decrypt(
            new DenunciaResponseDTO(
            denuncia.getId(),
            criadoPor != null ? criadoPor.getId() : null,
            vaga != null ? vaga.getId() : null,
            reserva != null ? reserva.getId() : null,
            veiculo != null ? veiculo.getId() : null,
            usuarioMotorista != null ? usuarioMotorista.getNome() : null,
            usuarioMotorista != null ?  usuarioMotorista.getTelefoneCripto() : null,
            denuncia.getDescricao(),
            EnderecoVagaMapper.toResponse(enderecoVaga),
            vaga != null ? vaga.getNumeroEndereco() : null,
            vaga != null ? vaga.getReferenciaEndereco() : null,
            veiculo != null ? veiculo.getMarca() : null,
            veiculo != null ? veiculo.getModelo() : null,
            veiculo != null ? veiculo.getPlaca() : null,
            veiculo != null ? veiculo.getTipo().getComprimento() : null,
            denuncia.getStatus(),
            denuncia.getTipo(),
            denuncia.getResposta(),
            denuncia.getAtualizadoPor() != null ? denuncia.getAtualizadoPor().getId() : null,
            denuncia.getCriadoEm(),
            denuncia.getAtualizadoEm(),
            denuncia.getEncerradoEm()
                
        ), usuarioMotorista.getPersonalDataKeyVersion());
    }

    public List<DenunciaResponseDTO> toResponseList(List<Denuncia> denuncias){
        if (denuncias == null || denuncias.isEmpty()) return List.of();
        return denuncias.stream().map(this::toResponse).toList();
    }
}