package com.cptrans.petrocarga.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.Denuncia;
import com.cptrans.petrocarga.domain.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.domain.enums.TipoDenunciaEnum;

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

    public DenunciaResponseDTO() {}

    public DenunciaResponseDTO(Denuncia denuncia){
        this.id = denuncia.getId();
        this.criadoPorId = denuncia.getCriadoPor().getId();
        this.telefoneMotorista = denuncia.getReserva().getMotorista().getUsuario().getTelefoneLast4();
        this.vagaId = denuncia.getVaga().getId();
        this.reservaId = denuncia.getReserva().getId();
        this.descricao = denuncia.getDescricao();
        this.nomeMotorista = denuncia.getReserva().getMotorista().getUsuario().getNome();
        this.enderecoVaga = denuncia.getVaga().getEndereco().toResponseDTO();
        this.numeroEndereco = denuncia.getVaga().getNumeroEndereco();
        this.referenciaEndereco = denuncia.getVaga().getReferenciaEndereco();
        this.veiculoId = denuncia.getReserva().getVeiculo().getId();
        this.marcaVeiculo = denuncia.getReserva().getVeiculo().getMarca();
        this.modeloVeiculo = denuncia.getReserva().getVeiculo().getModelo();
        this.placaVeiculo = denuncia.getReserva().getVeiculo().getPlaca();
        this.tamanhoVeiculo = denuncia.getReserva().getVeiculo().getTipo().getComprimento();
        this.status = denuncia.getStatus();
        this.tipo = denuncia.getTipo();
        this.resposta = denuncia.getResposta();
        this.criadoEm = denuncia.getCriadoEm();
        this.atualizadoEm = denuncia.getAtualizadoEm();
        this.atualizadoPorId = denuncia.getAtualizadoPor() != null ? denuncia.getAtualizadoPor().getId() : null;
        this.encerradoEm = denuncia.getEncerradoEm();
    }

    public UUID getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public UUID getCriadoPorId() {
        return criadoPorId;
    }

    public UUID getVagaId() {
        return vagaId;
    }

    public UUID getReservaId() {
        return reservaId;
    }

    public EnderecoVagaResponseDTO getEnderecoVaga() {
        return enderecoVaga;
    }

    public String getNumeroEndereco() {
        return numeroEndereco;
    }

    public String getReferenciaEndereco() {
        return referenciaEndereco;
    }

    public StatusDenunciaEnum getStatus() {
        return status;
    }

    public TipoDenunciaEnum getTipo() {
        return tipo;
    }

    public String getResposta() {
        return resposta;
    }

    public UUID getAtualizadoPorId() {
        return atualizadoPorId;
    }

    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public OffsetDateTime getEncerradoEm() {
        return encerradoEm;
    }

    public String getNomeMotorista() {
        return nomeMotorista;
    }

    public String getTelefoneMotorista() {
        return telefoneMotorista;
    }

    public UUID getVeiculoId() {
        return veiculoId;
    }

    public String getMarcaVeiculo() {
        return marcaVeiculo;
    }

    public String getModeloVeiculo() {
        return modeloVeiculo;
    }

    public String getPlacaVeiculo() {
        return placaVeiculo;
    }

    public Integer getTamanhoVeiculo() {
        return tamanhoVeiculo;
    }

}
