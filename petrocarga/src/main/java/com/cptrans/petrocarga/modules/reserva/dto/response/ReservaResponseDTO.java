package com.cptrans.petrocarga.modules.reserva.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.modules.motorista.dto.response.MotoristaSimplificadoResponseDTO;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;
import com.cptrans.petrocarga.modules.vaga.dto.response.VagaResponseDTO;
import com.cptrans.petrocarga.modules.veiculo.dto.response.VeiculoResponseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class ReservaResponseDTO {
    private UUID id;
    private VagaResponseDTO vaga;
    private MotoristaSimplificadoResponseDTO motorista;
    private VeiculoResponseDTO veiculo;
    private UsuarioResponseDTO criadoPor;
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
}