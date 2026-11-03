package com.cptrans.petrocarga.modules.vaga.dto.request;

import java.util.Set;

import org.hibernate.validator.constraints.Range;

import com.cptrans.petrocarga.enums.AreaVagaEnum;
import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.enums.TipoVagaEnum;
import com.cptrans.petrocarga.modules.enderecoVaga.dto.request.EnderecoVagaRequestDTO;
import com.cptrans.petrocarga.modules.operacaoVaga.dto.request.OperacaoVagaRequestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class VagaPatchDTO {

    @Valid
    private EnderecoVagaRequestDTO endereco;
    
    private AreaVagaEnum area;
    private TipoVagaEnum tipoVaga;
    private Set<OperacaoVagaRequestDTO> operacoesVaga;
    
    private Double latitudeInicio;
    private Double latitudeFim;
    private Double longitudeInicio;
    private Double longitudeFim;
    
    private String numeroEndereco;

    private String referenciaEndereco;
    
    @Range(min = 1, max = 100, message = "O comprimento deve ser um número inteiro positivo entre 1 e 100.")
    private Integer comprimento;

    @Range(min = 1, max = 15, message = "A quantidade deve ser um número inteiro positivo entre 1 e 15.")
    private Integer quantidade;

    private StatusVagaEnum status;

    @Size(min = 6, max = 6, message="O campo 'codigoPMP' deve ter 6 caracteres.")
    @Schema(
        description = "Código PMP do endereço da vaga",
        example = "Pb-123"
    )
    private String codigoPmp;

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

}