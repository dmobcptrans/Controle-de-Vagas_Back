package com.cptrans.petrocarga.modules.reserva.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.StatusReservaEnum;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.response.EnderecoVagaResponseDTO;
import com.cptrans.petrocarga.modules.usuario.dto.response.UsuarioResponseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Setter
public class ReservaDTO {
    private UUID id;
    private UUID vagaId;
    private UUID motoristaId;
    private String motoristaNome;
    private String motoristaCpf;
    private String numeroEndereco;
    private String referenciaEndereco;
    private EnderecoVagaResponseDTO enderecoVaga;
    private OffsetDateTime inicio;
    private OffsetDateTime fim;
    private Integer tamanhoVeiculo;
    private String placaVeiculo;
    private String modeloVeiculo;
    private String marcaVeiculo;
    private String cpfProprietarioVeiculo;
    private String cnpjProprietarioVeiculo;
    private String cidadeOrigem;
    private String entradaCidade;
    private StatusReservaEnum status;
    private Boolean checkedIn;
    private OffsetDateTime checkInEm;
    private OffsetDateTime checkOutEm;
    private UsuarioResponseDTO criadoPor;
    private OffsetDateTime criadoEm;
    private Integer posicaoPerpendicular;
}