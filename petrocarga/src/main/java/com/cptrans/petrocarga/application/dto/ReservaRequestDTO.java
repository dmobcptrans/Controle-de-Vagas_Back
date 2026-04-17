package com.cptrans.petrocarga.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.domain.entities.Motorista;
import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.entities.Veiculo;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReservaRequestDTO {
    @NotNull(message = "O campo 'vagaId' é obrigatório.")
    private UUID vagaId;
    @NotNull(message = "O campo 'motoristaId' é obrigatório.")
    private UUID motoristaId;
    @NotNull(message = "O campo 'veiculoId' é obrigatório.")
    private UUID veiculoId;
    @NotNull(message = "O campo 'cidadeOrigem' é obrigatório.")
    @NotBlank(message = "O campo 'cidadeOrigem' não pode estar em branco.")
    private String cidadeOrigem;
    @Size(min = 3, max = 100, message = "O campo 'entradaCidade' deve ter entre 3 e 100 caracteres.")
    private String entradaCidade;
    @NotNull(message = "O campo 'inicio' é obrigatório.")
    private OffsetDateTime inicio;
    @NotNull(message = "O campo 'fim' é obrigatório.")
    private OffsetDateTime fim;
    private Integer posicaoPerpendicular;

    public Reserva toEntity(Vaga vaga, Motorista motorista, Veiculo veiculo) {
        Reserva reserva = new Reserva();
        reserva.setVaga(vaga);
        reserva.setMotorista(motorista);
        reserva.setVeiculo(veiculo);
        reserva.setCidadeOrigem(this.cidadeOrigem);
        reserva.setEntradaCidade(this.entradaCidade);
        reserva.setInicio(this.inicio);
        reserva.setFim(this.fim);
        reserva.setPosicaoPerpendicular(this.posicaoPerpendicular);
        return reserva;
    }

    public Reserva toEntity() {
        Reserva reserva = new Reserva();
        reserva.setCidadeOrigem(this.cidadeOrigem);
        reserva.setEntradaCidade(this.entradaCidade);
        reserva.setInicio(this.inicio);
        reserva.setFim(this.fim);
        reserva.setStatus(StatusReservaEnum.RESERVADA);
        reserva.setPosicaoPerpendicular(this.posicaoPerpendicular);
        return reserva;
    }

    // Getters and Setters
    public UUID getVagaId() {
        return vagaId;
    }
    public UUID getMotoristaId() {
        return motoristaId;
    }
    public UUID getVeiculoId() {
        return veiculoId;
    }
    public String getCidadeOrigem() {
        return cidadeOrigem;
    }
    public String getEntradaCidade() {
        return entradaCidade;
    }
    public OffsetDateTime getInicio() {
        return inicio;
    }
    public OffsetDateTime getFim() {
        return fim;
    }
    public Integer getPosicaoPerpendicular() {
        return posicaoPerpendicular;
    }
}
