package com.cptrans.petrocarga.modules.reserva.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.dto.response.VeiculoSimplificadoResponseDTO;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ReservaResponseDTO {
    private UUID id;
    private VagaSimplificadoResponseDTO vaga;
    private MotoristaSimplificadoResponseDTO motorista;
    private VeiculoSimplificadoResponseDTO veiculo;
    private UsuarioSimplificadoResponseDTO criadoPor;
    private String cidadeOrigem;
    private String entradaCidade;
    private OffsetDateTime criadoEm;
    private OffsetDateTime inicio;
    private OffsetDateTime fim;
    private StatusReservaEnum status;
    private Boolean checkedIn;
    private OffsetDateTime checkInEm;
    private OffsetDateTime checkOutEm;
    private Integer posicaoPerpendicular;

    public void formatarDados(){
        criadoEm = DateUtils.fusoHorarioBrasilia(criadoEm);
        inicio = DateUtils.fusoHorarioBrasilia(inicio);
        fim = DateUtils.fusoHorarioBrasilia(fim);
        checkInEm = DateUtils.fusoHorarioBrasilia(checkInEm);
        checkOutEm = DateUtils.fusoHorarioBrasilia(checkOutEm);
    }
}