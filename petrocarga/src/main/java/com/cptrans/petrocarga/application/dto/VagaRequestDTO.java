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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class VagaRequestDTO {

    @Valid
    @NotNull(message = "O endereço é obrigatório.")
    private EnderecoVagaRequestDTO endereco;
    
    @Schema(description = "Área da vaga (Ex: AMARELA, VERMELHA)", example = "AMARELA")
    private AreaVagaEnum area;
    
    @Schema(description = "Número de endereço dereferência da vaga", example = "07 ao 35")
    @NotNull(message = "O campo 'numeroEndereco' é obrigatório.")
    private String numeroEndereco;

    @Schema(description = "Ponto de referência para a vaga", example = "Em frente ao portão principal")
    @NotNull(message = "O campo 'referenciaEndereco' é obrigatório.")
    private String referenciaEndereco;

    @Schema(description = "Latitude inicial da vaga", example = "-22.509135")
    @NotNull(message = "A latitude inicial é obrigatória.")
    private Double latitudeInicio;

    @Schema(description = "Longitude inicial da vaga", example = "-43.171351")
    @NotNull(message = "A longitude inicial é obrigatória.")
    private Double longitudeInicio;

    @Schema(description = "Latitude final da vaga", example = "-22.509140")
    @NotNull(message = "A latitude final é obrigatória.")
    private Double latitudeFim;

    @Schema(description = "Longitude final da vaga", example = "-43.171355")
    @NotNull(message = "A longitude final é obrigatória.")
    private Double longitudeFim;

    @Schema(description = "Tipo de vaga (Ex: PARALELA, PERPENDICULAR)", example="PARALELA")
    private TipoVagaEnum tipoVaga;

    @Valid
    @NotNull(message = "O comprimento é obrigatório.")
    @Schema(description = "Comprimento máximo em metros permitido para a vaga", example = "12")
    private Integer comprimento;
    
    @Valid
    private Set<OperacaoVagaRequestDTO> operacoesVaga;

    @Valid
    @Min(value = 1, message = "A quantidade deve ser um número inteiro positivo entre 1 e 15.")
    @Max(value = 15, message = "A quantidade deve ser um número inteiro positivo entre 1 e 15.")
    private Integer quantidade;

    @Valid
    private StatusVagaEnum status;


    public Vaga toEntity() {
        Vaga vaga = new Vaga();

        vaga.setArea(this.area);
        vaga.setComprimento(this.comprimento);
        vaga.setNumeroEndereco(this.numeroEndereco);
        vaga.setReferenciaEndereco(this.referenciaEndereco);
        vaga.setTipoVaga(this.tipoVaga);
        vaga.setEndereco(this.endereco.toEntity());
        vaga.setQuantidade(this.quantidade);
        vaga.setStatus(this.status != null ? this.status : StatusVagaEnum.INDISPONIVEL);

        vaga.setLatitudeInicio(this.latitudeInicio);
        vaga.setLongitudeInicio(this.longitudeInicio);
        vaga.setLatitudeFim(this.latitudeFim);
        vaga.setLongitudeFim(this.longitudeFim);

        if (this.operacoesVaga != null && !this.operacoesVaga.isEmpty()) {
            Set<OperacaoVaga> operacoes = this.operacoesVaga.stream()
                    .map(dto -> dto.toEntity(vaga))
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

    public Double getLatitudeInicio() {
        return latitudeInicio;
    }

    public void setLatitudeInicio(Double latitudeInicio) {
        this.latitudeInicio = latitudeInicio;
    }

    public Double getLongitudeInicio() {
        return longitudeInicio;
    }

    public void setLongitudeInicio(Double longitudeInicio) {
        this.longitudeInicio = longitudeInicio;
    }

    public Double getLatitudeFim() {
        return latitudeFim;
    }

    public void setLatitudeFim(Double latitudeFim) {
        this.latitudeFim = latitudeFim;
    }

    public Double getLongitudeFim() {
        return longitudeFim;
    }

    public void setLongitudeFim(Double longitudeFim) {
        this.longitudeFim = longitudeFim;
    }

    public Integer getComprimento() {
        return comprimento;
    }

    public void setComprimento(Integer comprimento) {
        this.comprimento = comprimento;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
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