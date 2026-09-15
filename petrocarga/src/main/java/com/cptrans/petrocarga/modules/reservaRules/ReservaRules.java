package com.cptrans.petrocarga.modules.reservaRules;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;

public interface ReservaRules {
    public void validacoesDaVaga(Vaga vaga, OffsetDateTime inicio, OffsetDateTime fim);
    public void validacoesDeAutorizacao(UUID usuarioLogadoId , Motorista motorista, Veiculo veiculo);
    public void validacoesDeConflito(Vaga vaga, ReservaDTO reserva);
    public void validacoesTemporais(OffsetDateTime inicio, OffsetDateTime fim);
    public void validarLimiteIntrodutorioReservasRapidas(String placa);
}