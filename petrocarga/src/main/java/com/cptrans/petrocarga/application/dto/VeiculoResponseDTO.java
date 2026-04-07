package com.cptrans.petrocarga.application.dto;

import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.Veiculo;
import com.cptrans.petrocarga.domain.enums.TipoVeiculoEnum;

public class VeiculoResponseDTO {

    private UUID id;
    private String placa;
    private String marca;
    private String modelo;
    private TipoVeiculoEnum tipo;
    private Integer comprimento;
    private UUID usuarioId;
    private String cpfProprietario;
    private String cnpjProprietario;

    public VeiculoResponseDTO() {
    }

    public VeiculoResponseDTO(Veiculo veiculo) {
        this.id = veiculo.getId();
        this.placa = veiculo.getPlaca();
        this.marca = veiculo.getMarca();
        this.modelo = veiculo.getModelo();
        this.tipo = veiculo.getTipo();
        this.comprimento = veiculo.getTipo().getComprimento();
        this.usuarioId = veiculo.getUsuario().getId();
        this.cpfProprietario = veiculo.getCpfProprietarioCripto();
        this.cnpjProprietario = veiculo.getCnpjProprietario();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public TipoVeiculoEnum getTipo() {
        return tipo;
    }

    public void setTipo(TipoVeiculoEnum tipo) {
        this.tipo = tipo;
    }

    public Integer getComprimento() {
        return comprimento;
    }

    public void setComprimento(Integer comprimento) {
        this.comprimento = comprimento;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCpfProprietario() {
        return cpfProprietario;
    }

    public void setCpfProprietario(String cpfProprietario) {
        this.cpfProprietario = cpfProprietario;
    }

    public String getCnpjProprietario() {
        return cnpjProprietario;
    }

    public void setCnpjProprietario(String cnpjProprietario) {
        this.cnpjProprietario = cnpjProprietario;
    }
}