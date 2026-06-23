package com.cptrans.petrocarga.modules.denuncia.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.enums.TipoDenunciaEnum;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.response.EnderecoVagaResponseDTO;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class DenunciaResponseDTO {
    private UUID id;
    private UUID criadoPorId;
    private UUID vagaId;
    private UUID reservaId;
    private UUID veiculoId;
    private String nomeMotorista;
    private String telefoneMotorista;
    private String descricao;
    private EnderecoVagaResponseDTO enderecoVaga;
    private String numeroEndereco;
    private String referenciaEndereco;
    private String marcaVeiculo;
    private String modeloVeiculo;
    private String placaVeiculo;
    private Integer tamanhoVeiculo;
    private StatusDenunciaEnum status;
    private TipoDenunciaEnum tipo;
    private String resposta;
    private UUID atualizadoPorId;
    private OffsetDateTime criadoEm;
    private OffsetDateTime atualizadoEm;
    private OffsetDateTime encerradoEm;

    public void setTelefoneMotorista(String telefoneMotorista) {
        this.telefoneMotorista = telefoneMotorista;
    }
}