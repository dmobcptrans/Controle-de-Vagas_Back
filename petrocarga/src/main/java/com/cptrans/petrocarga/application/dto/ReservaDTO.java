package com.cptrans.petrocarga.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.Motorista;
import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.entities.ReservaRapida;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.entities.Veiculo;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;

import jakarta.persistence.criteria.CriteriaBuilder.In;


public class ReservaDTO {
    private UUID id;
    private UUID vagaId;
    private UUID motoristaId;
    private String motoristaNome;
    private String motoristaCpf;
    private String numeroEndereco;
    private String referenciaEndereco;
    private EnderecoVagaResponseDTO enderecoVaga;
    private OffsetDateTime inicio;
    private OffsetDateTime fim;
    private Integer tamanhoVeiculo;
    private String placaVeiculo;
    private String modeloVeiculo;
    private String marcaVeiculo;
    private String cpfProprietarioVeiculo;
    private String cnpjProprietarioVeiculo;
    private String cidadeOrigem;
    private String entradaCidade;
    private StatusReservaEnum status;
    private Boolean checkedIn;
    private OffsetDateTime checkInEm;
    private OffsetDateTime checkOutEm;
    private UsuarioResponseDTO criadoPor;
    private OffsetDateTime criadoEm;
    private Integer posicaoPerpendicular;

    public ReservaDTO() {
    }
    public ReservaDTO(UUID reservaId, UUID vagaId, String numeroEndereco,
        String referenciaEndereco, EnderecoVagaResponseDTO enderecoVaga, OffsetDateTime inicio,
        OffsetDateTime fim, Integer tamanhoVeiculo, String placaVeiculo, StatusReservaEnum status,
        Usuario criadoPor, OffsetDateTime criadoEm, Integer posicaoPerpendicular, String cidadeOrigem, String entradaCidade) {
        this.id = reservaId;
        this.vagaId = vagaId;
        this.numeroEndereco = numeroEndereco;
        this.referenciaEndereco = referenciaEndereco;
        this.enderecoVaga = enderecoVaga;
        this.inicio = inicio;
        this.fim = fim;
        this.tamanhoVeiculo = tamanhoVeiculo;
        this.placaVeiculo = placaVeiculo;
        this.status = status;
        this.criadoPor = criadoPor.toResponseDTO();
        this.criadoEm = criadoEm;
        this.posicaoPerpendicular = posicaoPerpendicular;
        this.cidadeOrigem = cidadeOrigem;
        this.entradaCidade = entradaCidade;
    }

    public ReservaDTO(UUID reservaId, String cidadeOrigem, String entradaCidade, Boolean checkedIn, OffsetDateTime checkInEm, OffsetDateTime checkOutEm,  Vaga vaga, OffsetDateTime inicio, OffsetDateTime fim, Veiculo veiculo, StatusReservaEnum status, Usuario criadoPor, OffsetDateTime criadoEm, Motorista motorista, Integer posicaoPerpendicular) {
        this.id = reservaId;
        this.vagaId = vaga.getId();
        this.motoristaId = motorista.getId();
        this.motoristaNome = motorista.getUsuario().getNome();
        this.motoristaCpf = motorista.getUsuario().getCpfLast5();
        this.numeroEndereco = vaga.getNumeroEndereco();
        this.referenciaEndereco = vaga.getReferenciaEndereco();
        this.enderecoVaga = vaga.getEndereco().toResponseDTO();
        this.inicio = inicio;
        this.fim = fim;
        this.tamanhoVeiculo = veiculo.getTipo().getComprimento();
        this.placaVeiculo = veiculo.getPlaca();
        this.modeloVeiculo = veiculo.getModelo();
        this.marcaVeiculo = veiculo.getMarca();
        this.cpfProprietarioVeiculo = veiculo.getCpfProprietarioLast5();
        this.cnpjProprietarioVeiculo = veiculo.getCnpjProprietario();
        this.cidadeOrigem = cidadeOrigem;
        this.entradaCidade = entradaCidade;
        this.status = status;
        this.checkedIn = checkedIn;
        this.checkInEm = checkInEm;
        this.checkOutEm = checkOutEm;
        this.criadoPor = criadoPor.toResponseDTO();
        this.criadoEm = criadoEm;
        this.posicaoPerpendicular = posicaoPerpendicular;
    }

    public ReservaDTO(Reserva reserva){
        this.id = reserva.getId();
        this.vagaId = reserva.getVaga().getId();
        this.motoristaId = reserva.getMotorista().getId();
        this.motoristaNome = reserva.getMotorista().getUsuario().getNome();        
        this.motoristaCpf = reserva.getMotorista().getUsuario().getCpfLast5();
        this.numeroEndereco = reserva.getVaga().getNumeroEndereco();
        this.referenciaEndereco = reserva.getVaga().getReferenciaEndereco();
        this.enderecoVaga = reserva.getVaga().getEndereco().toResponseDTO();
        this.inicio = reserva.getInicio();
        this.fim = reserva.getFim();
        this.tamanhoVeiculo = reserva.getVeiculo().getTipo().getComprimento();
        this.placaVeiculo = reserva.getVeiculo().getPlaca();
        this.modeloVeiculo = reserva.getVeiculo().getModelo();
        this.marcaVeiculo = reserva.getVeiculo().getMarca();
        this.cpfProprietarioVeiculo = reserva.getVeiculo().getCpfProprietarioLast5();
        this.cnpjProprietarioVeiculo = reserva.getVeiculo().getCnpjProprietario();
        this.cidadeOrigem = reserva.getCidadeOrigem();
        this.entradaCidade = reserva.getEntradaCidade();
        this.status = reserva.getStatus();
        this.checkedIn = reserva.isCheckedIn();
        this.checkInEm = reserva.getCheckInEm();
        this.checkOutEm = reserva.getCheckOutEm();
        this.criadoPor = reserva.getCriadoPor().toResponseDTO();
        this.criadoEm = reserva.getCriadoEm();
        this.posicaoPerpendicular = reserva.getPosicaoPerpendicular();
    }

    public ReservaDTO (ReservaRapida reservaRapida) {
        this.id = reservaRapida.getId();
        this.vagaId = reservaRapida.getVaga().getId();
        this.numeroEndereco = reservaRapida.getVaga().getNumeroEndereco();
        this.referenciaEndereco = reservaRapida.getVaga().getReferenciaEndereco();
        this.enderecoVaga = reservaRapida.getVaga().getEndereco().toResponseDTO();
        this.inicio = reservaRapida.getInicio();
        this.fim = reservaRapida.getFim();
        this.tamanhoVeiculo = reservaRapida.getTipoVeiculo().getComprimento();
        this.placaVeiculo = reservaRapida.getPlaca();
        this.status = reservaRapida.getStatus();
        this.criadoPor = reservaRapida.getAgente().getUsuario().toResponseDTO();
        this.criadoEm = reservaRapida.getCriadoEm();
        this.posicaoPerpendicular = reservaRapida.getPosicaoPerpendicular();
        this.cidadeOrigem = reservaRapida.getCidadeOrigem();
        this.entradaCidade = reservaRapida.getEntradaCidade();
    }
    public UUID getId() {
        return id;
    }
    public void setId(UUID reservaId) {
        this.id = reservaId;
    }
    public UUID getVagaId() {
        return vagaId;
    }
    public void setVagaId(Vaga vaga) {
        this.vagaId = vaga.getId();
    }
    public String getNumeroEndereco() {
        return numeroEndereco;
    }
    public void setNumeroEndereco(Vaga vaga) {
        this.numeroEndereco = vaga.getNumeroEndereco();
    }
    public String getReferenciaEndereco() {
        return referenciaEndereco;
    }
    public void setReferenciaEndereco(Vaga vaga) {
        this.referenciaEndereco = vaga.getReferenciaEndereco();
    }
    public EnderecoVagaResponseDTO getEnderecoVaga() {
        return enderecoVaga;
    }
    public OffsetDateTime getInicio() {
        return inicio;
    }    
    public void setInicio(OffsetDateTime inicio) {
        this.inicio = inicio;
    }
    public OffsetDateTime getFim() {
        return fim;
    }
    public void setFim(OffsetDateTime fim) {
        this.fim = fim;
    }
    public Integer getTamanhoVeiculo() {
        return tamanhoVeiculo;
    }
    public void setTamanhoVeiculo(Integer tamanhoVeiculo) {
        this.tamanhoVeiculo = tamanhoVeiculo;
    }
    public String getPlacaVeiculo() {
        return placaVeiculo;
    }
    public void setPlacaVeiculo(String placaVeiculo) {
        this.placaVeiculo = placaVeiculo;
    }
    public String getCidadeOrigem() {
        return cidadeOrigem;
    }
    public void setCidadeOrigem(String cidadeOrigem) {
        this.cidadeOrigem = cidadeOrigem;
    }
    public String getEntradaCidade() {
        return entradaCidade;
    }
    public void setEntradaCidade(String entradaCidade) {
        this.entradaCidade = entradaCidade;
    }
    public StatusReservaEnum getStatus() {
        return status;
    }
    public void setStatus(StatusReservaEnum status) {
        this.status = status;
    }
    public Boolean isCheckedIn() {
        return checkedIn;
    }
    public void setCheckedIn(Boolean checkedIn) {
        this.checkedIn = checkedIn;
    }
    public OffsetDateTime getCheckInEm() {
        return checkInEm;
    }
    public void setCheckInEm(OffsetDateTime checkInEm) {
        this.checkInEm = checkInEm;
    }
    public OffsetDateTime getCheckOutEm() {
        return checkOutEm;
    }
    public void setCheckOutEm(OffsetDateTime checkOutEm) {
        this.checkOutEm = checkOutEm;
    }
    public UsuarioResponseDTO getCriadoPor() {
        return criadoPor;
    }
    public void setCriadoPor(UsuarioResponseDTO criadoPor) {
        this.criadoPor = criadoPor;
    }
    public OffsetDateTime getCriadoEm() {
        return criadoEm;
    }
    public void setCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    public UUID getMotoristaId() {
        return motoristaId;
    }
    public void setMotoristaId(UUID motoristaId) {
        this.motoristaId = motoristaId;
    }
    public String getModeloVeiculo() {
        return modeloVeiculo;
    }
    public void setModeloVeiculo(String modeloVeiculo) {
        this.modeloVeiculo = modeloVeiculo;
    }
    public String getMarcaVeiculo() {
        return marcaVeiculo;
    }
    public void setMarcaVeiculo(String marcaVeiculo) {
        this.marcaVeiculo = marcaVeiculo;
    }
    public String getCpfProprietarioVeiculo() {
        return cpfProprietarioVeiculo;
    }
    public void setCpfProprietarioVeiculo(String cpfProprietarioVeiculo) {
        this.cpfProprietarioVeiculo = cpfProprietarioVeiculo;
    }
    public String getCnpjProprietarioVeiculo() {
        return cnpjProprietarioVeiculo;
    }
    public void setCnpjProprietarioVeiculo(String cnpjProprietarioVeiculo) {
        this.cnpjProprietarioVeiculo = cnpjProprietarioVeiculo;
    }
    public String getMotoristaNome() {
        return motoristaNome;
    }
    public void setMotoristaNome(String motoristaNome) {
        this.motoristaNome = motoristaNome;
    }
    public String getMotoristaCpf() {
        return motoristaCpf;
    }
    public void setMotoristaCpf(String motoristaCpf) {
        this.motoristaCpf = motoristaCpf;
    }
    public void setNumeroEndereco(String numeroEndereco) {
        this.numeroEndereco = numeroEndereco;
    }
    public void setReferenciaEndereco(String referenciaEndereco) {
        this.referenciaEndereco = referenciaEndereco;
    }
    public void setEnderecoVaga(EnderecoVagaResponseDTO enderecoVaga) {
        this.enderecoVaga = enderecoVaga;
    }
    public Integer getPosicaoPerpendicular() {
        return posicaoPerpendicular;
    }
    public void setPosicaoPerpendicular(Integer posicaoPerpendicular) {
        this.posicaoPerpendicular = posicaoPerpendicular;
    }
}
