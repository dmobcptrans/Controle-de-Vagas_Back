package com.cptrans.petrocarga.application.dto;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.AreaVagaEnum;
import com.cptrans.petrocarga.domain.enums.StatusVagaEnum;
import com.cptrans.petrocarga.domain.enums.TipoVagaEnum;

public class VagaResponseDTO {

    private UUID id;
    private EnderecoVagaResponseDTO endereco;
    private AreaVagaEnum area;
    private String numeroEndereco;
    private String referenciaEndereco;
    private TipoVagaEnum tipoVaga;
    private String referenciaGeoInicio;
    private String referenciaGeoFim;
    private Integer comprimento;
    private Integer quantidade;
    private StatusVagaEnum status;

    Comparator<OperacaoVagaResponseDTO> compararPorCodigoEnum = Comparator.comparingInt(op -> op.getDiaSemanaAsEnum().getCodigo());
    private Set<OperacaoVagaResponseDTO> operacoesVaga = new TreeSet<>(compararPorCodigoEnum);

    public VagaResponseDTO() {
    }

    public VagaResponseDTO(Vaga vaga) {
        this.id = vaga.getId();
        this.endereco = vaga.getEndereco().toResponseDTO();
        this.area = vaga.getArea();
        this.numeroEndereco = vaga.getNumeroEndereco();
        this.referenciaEndereco = vaga.getReferenciaEndereco();
        this.tipoVaga = vaga.getTipoVaga();
        this.referenciaGeoInicio = vaga.getReferenciaGeoInicio();
        this.referenciaGeoFim = vaga.getReferenciaGeoFim();
        this.comprimento = vaga.getComprimento();
        this.quantidade = vaga.getQuantidade();
        this.status = vaga.getStatus();
        this.operacoesVaga = vaga.getOperacoesVaga().stream()
                .map(operacaoVaga -> new OperacaoVagaResponseDTO(operacaoVaga))
                .collect(Collectors.toCollection(() -> new TreeSet<>(compararPorCodigoEnum)));
    }

    // Getters e Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EnderecoVagaResponseDTO getEndereco() {
        return endereco;
    }

    public void setEndereco(EnderecoVagaResponseDTO endereco) {
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
    
    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public StatusVagaEnum getStatus() {
        return status;
    }

    public void setStatus(StatusVagaEnum status) {
        this.status = status;
    }

    public Set<OperacaoVagaResponseDTO> getOperacoesVaga() {
        return operacoesVaga;
    }

    public void setOperacoesVaga(Set<OperacaoVagaResponseDTO> operacoesVaga){
        this.operacoesVaga = operacoesVaga.stream()
            .sorted(compararPorCodigoEnum)
            .collect(Collectors.toCollection(() -> new TreeSet<>(compararPorCodigoEnum)));
    }
}