package com.cptrans.petrocarga.modules.reservaRapida.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.enums.TipoVeiculoEnum;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ReservaRapidaResponseDTO {

    private UUID id;
    private UUID vagaId;
    private UUID agenteId;
    private String logradouro;
    private String bairro;
    private TipoVeiculoEnum tipoVeiculo;
    private String placa;
    private OffsetDateTime inicio;
    private OffsetDateTime fim;
    private OffsetDateTime criadoEm;
    private StatusReservaEnum status;
    private Integer posicaoPerpendicular;
    private String cidadeOrigem;
    private String entradaCidade;

    public void formatarDados() {
        inicio = DateUtils.fusoHorarioBrasilia(inicio);
        fim = DateUtils.fusoHorarioBrasilia(fim);
        criadoEm = DateUtils.fusoHorarioBrasilia(criadoEm);
    }
}