package com.cptrans.petrocarga.application.dto;

import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import com.cptrans.petrocarga.domain.entities.Veiculo;
import com.cptrans.petrocarga.domain.enums.TipoVeiculoEnum;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class VeiculoRequestDTO {

    @NotNull(message = "Placa deve ser informada.")
    @Size(min=7, max=7, message="Placa deve ter exatamente 7 caracteres.")
    private String placa;
    
    @Size(min=3, max=20, message="Marca deve ter entre 3 e 20 caracteres.")
    private String marca;

    @Size(min=3, max=20, message="Modelo deve ter entre 3 e 20 caracteres.")
    private String modelo;

    @NotNull(message = "Tipo do veículo deve ser informado.")
    private TipoVeiculoEnum tipo;

    @Valid
    @CPF(message = "CPF inválido.")
    private String cpfProprietario;

    @Valid
    @CNPJ(message = "CNPJ inválido.")
    private String cnpjProprietario;

    public Veiculo toEntity() {
        Veiculo veiculo = new Veiculo();
        // if(this.placa == null) throw new IllegalArgumentException("Placa deve ser informada.");
        veiculo.setPlaca(this.placa.toUpperCase());
        veiculo.setMarca(this.marca);
        veiculo.setModelo(this.modelo);
        veiculo.setTipo(this.tipo);
        veiculo.setComprimento(this.tipo.getComprimento());
        veiculo.setCpfProprietarioHash(this.cpfProprietario);
        veiculo.setCnpjProprietario(this.cnpjProprietario);
        return veiculo;
    }

    // Getters
    public String getPlaca() {
        return placa;
    }
    public String getMarca() {
        return marca;
    }
    public String getModelo() {
        return modelo;
    }
    public TipoVeiculoEnum getTipo() {
        return tipo;
    }
    public String getCpfProprietario() {
        return cpfProprietario;
    }
    public String getCnpjProprietario() {
        return cnpjProprietario;
    }
}
