package com.cptrans.petrocarga.application.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.cptrans.petrocarga.domain.entities.OperacaoVaga;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.AreaVagaEnum;
import com.cptrans.petrocarga.domain.enums.StatusVagaEnum;
import com.cptrans.petrocarga.domain.enums.TipoVagaEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class VagaPatchDTO {

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
    
    @Min(value = 1, message = "O comprimento deve ser um número inteiro positivo entre 1 e 100.")
    @Max(value = 100, message = "O comprimento deve ser um número inteiro positivo entre 1 e 100.")
    private Integer comprimento;

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
        vaga.setLatitudeFim(this.latitudeFim);
        vaga.setLatitudeInicio(this.latitudeInicio);
        vaga.setLongitudeFim(this.longitudeFim);
        vaga.setLongitudeInicio(this.longitudeInicio);
        vaga.setStatus(this.status);
        vaga.setTipoVaga(this.tipoVaga);
        vaga.setEndereco(this.endereco != null ? this.endereco.toEntity() : null);
        vaga.setQuantidade(this.quantidade);

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

    public Double getLatitudeInicio() {
        return latitudeInicio;
    }

    public void setLatitudeInicio(Double latitudeInicio) {
        this.latitudeInicio = latitudeInicio;
    }

    public Double getLatitudeFim() {
        return latitudeFim;
    }

    public void setLatitudeFim(Double latitudeFim) {
        this.latitudeFim = latitudeFim;
    }

    public Double getLongitudeInicio() {
        return longitudeInicio;
    }

    public void setLongitudeInicio(Double longitudeInicio) {
        this.longitudeInicio = longitudeInicio;
    }

    public Double getLongitudeFim() {
        return longitudeFim;
    }

    public void setLongitudeFim(Double longitudeFim) {
        this.longitudeFim = longitudeFim;
    }

}