package com.cptrans.petrocarga.application.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.cptrans.petrocarga.domain.entities.OperacaoVaga;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.AreaVagaEnum;
import com.cptrans.petrocarga.domain.enums.StatusVagaEnum;
import com.cptrans.petrocarga.domain.enums.TipoVagaEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class VagaRequestDTO {

    @Valid
    @NotNull(message = "O endereço é obrigatório.")
    private EnderecoVagaRequestDTO endereco;
    
    @Schema(description = "Área da vaga (Ex: AMARELA, VERMELHA)", example = "AMARELA")
    private AreaVagaEnum area;
    
    @Schema(description = "Número de endereço dereferência da vaga", example = "07 ao 35")
    private String numeroEndereco;

    @Schema(description = "Ponto de referência para a vaga", example = "Em frente ao portão principal")
    private String referenciaEndereco;

    @Schema(description = "Tipo de vaga (Ex: PARALELA, PERPENDICULAR)", example="PARALELA")
    private TipoVagaEnum tipoVaga;

    @Schema(description = "Coordenada geográfica inicial da vaga", example = "-22.509135, -43.171351")
    private String referenciaGeoInicio;
    
    @Schema(description = "Coordenada geográfica final da vaga (se aplicável)", example = "-22.509140, -43.171355")
    private String referenciaGeoFim;

    @Valid
    @NotNull(message = "O comprimento é obrigatório.")
    @Schema(description = "Comprimento máximo em metros permitido para a vaga", example = "12")
    private Integer comprimento;
    
    @Valid
    private Set<OperacaoVagaRequestDTO> operacoesVaga;

    @Valid
    private StatusVagaEnum status;


    public Vaga toEntity() {
        Vaga vaga = new Vaga();
        vaga.setArea(this.area);
        vaga.setComprimento(this.comprimento);
        vaga.setNumeroEndereco(this.numeroEndereco);
        vaga.setReferenciaEndereco(this.referenciaEndereco);
        vaga.setReferenciaGeoFim(this.referenciaGeoFim);
        vaga.setReferenciaGeoInicio(this.referenciaGeoInicio);
        vaga.setStatus(this.status != null ? this.status : StatusVagaEnum.INDISPONIVEL );
        vaga.setTipoVaga(this.tipoVaga);
        vaga.setEndereco(this.endereco.toEntity());

        if (this.operacoesVaga != null && !this.operacoesVaga.isEmpty()) {
            Set<OperacaoVaga> operacoes = this.operacoesVaga.stream()
                    .map(dto -> {
                        return dto.toEntity(vaga);
                    })
                    .collect(Collectors.toSet());
            vaga.setOperacoesVaga(operacoes);
        }
        return vaga;
    }

    // Getters e Setters
    
    public EnderecoVagaRequestDTO getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoVagaRequestDTO endereco) {
        this.endereco = endereco;
    }

    public AreaVagaEnum getArea() {
        return area;
    }

    public void setArea(AreaVagaEnum area) {
        this.area = area;
    }

    public String getNumeroEndereco() {
        return numeroEndereco;
    }

    public void setNumeroEndereco(String numeroEndereco) {
        this.numeroEndereco = numeroEndereco;
    }

    public String getReferenciaEndereco() {
        return referenciaEndereco;
    }

    public void setReferenciaEndereco(String referenciaEndereco) {
        this.referenciaEndereco = referenciaEndereco;
    }

    public TipoVagaEnum getTipoVaga() {
        return tipoVaga;
    }

    public void setTipoVaga(TipoVagaEnum tipoVaga) {
        this.tipoVaga = tipoVaga;
    }

    public String getReferenciaGeoInicio() {
        return referenciaGeoInicio;
    }

    public void setReferenciaGeoInicio(String referenciaGeoInicio) {
        this.referenciaGeoInicio = referenciaGeoInicio;
    }

    public String getReferenciaGeoFim() {
        return referenciaGeoFim;
    }

    public void setReferenciaGeoFim(String referenciaGeoFim) {
        this.referenciaGeoFim = referenciaGeoFim;
    }

    public Integer getComprimento() {
        return comprimento;
    }

    public void setComprimento(Integer comprimento) {
        this.comprimento = comprimento;
    }

    public Set<OperacaoVagaRequestDTO> getOperacoesVaga() {
        return operacoesVaga;
    }

    public void setOperacoesVaga(Set<OperacaoVagaRequestDTO> operacoesVaga) {
        this.operacoesVaga = operacoesVaga;
    }

    public StatusVagaEnum getStatus(){
        return status;
    }

    public void setStatus(StatusVagaEnum status){
        this.status = status;
    }
}