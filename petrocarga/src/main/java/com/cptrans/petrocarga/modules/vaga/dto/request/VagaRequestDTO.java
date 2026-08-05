package com.cptrans.petrocarga.modules.vaga.dto.request;

import java.util.Set;

import org.hibernate.validator.constraints.Range;

import com.cptrans.petrocarga.enums.AreaVagaEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.request.EnderecoVagaRequestDTO;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.request.OperacaoVagaRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class VagaRequestDTO {

    @Valid
    @NotNull(message = "O endereço é obrigatório.")
    private EnderecoVagaRequestDTO endereco;
    
    @NotNull(message = "O campo 'area' é obrigatório.")
    @Schema(description = "Área da vaga (Ex: AMARELA, VERMELHA)", example = "AMARELA")
    private AreaVagaEnum area;
    
    @NotNull(message = "O campo 'numeroEndereco' é obrigatório.")
    @Schema(description = "Número de endereço dereferência da vaga", example = "07 ao 35")
    private String numeroEndereco;

    @NotNull(message = "O campo 'referenciaEndereco' é obrigatório.")
    @Schema(description = "Ponto de referência para a vaga", example = "Em frente ao portão principal")
    private String referenciaEndereco;

    @NotNull(message = "A latitude inicial é obrigatória.")
    @Schema(description = "Latitude inicial da vaga", example = "-22.509135")
    private Double latitudeInicio;

    @NotNull(message = "A longitude inicial é obrigatória.")
    @Schema(description = "Longitude inicial da vaga", example = "-43.171351")
    private Double longitudeInicio;

    @NotNull(message = "A latitude final é obrigatória.")
    @Schema(description = "Latitude final da vaga", example = "-22.509140")
    private Double latitudeFim;

    @NotNull(message = "A longitude final é obrigatória.")
    @Schema(description = "Longitude final da vaga", example = "-43.171355")
    private Double longitudeFim;

    @NotNull(message = "O campo 'tipoVaga' é obrigatório.")
    @Schema(description = "Tipo de vaga (Ex: PARALELA, PERPENDICULAR)", example="PARALELA")
    private TipoVagaEnum tipoVaga;

    @NotNull(message = "O comprimento é obrigatório.")
    @Schema(description = "Comprimento máximo em metros permitido para a vaga", example = "12")
    @Range(min = 1, max = 100, message = "O comprimento deve ser um número inteiro positivo entre 1 e 100.")
    private Integer comprimento;
    
    @Valid
    private Set<OperacaoVagaRequestDTO> operacoesVaga;

    @Valid
    @Range(min = 1, max = 15, message = "A quantidade deve ser um número inteiro positivo entre 1 e 15.")
    private Integer quantidade;

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}