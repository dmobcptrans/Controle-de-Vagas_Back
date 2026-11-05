package com.cptrans.petrocarga.modules.reservaRules;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.reserva.dto.response.ReservaDTO;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;

public interface ReservaRules {
    public static final int LIMITE_DE_RESERVAS_ATIVAS_POR_PLACA = 3;
    public static final int LIMITE_DE_RESERVAS_ATIVAS_POR_VEICULO = 3;
    public static final int LIMITE_DE_RESERVAS_ATIVAS_POR_MOTORISTA = 3;
    public static final int LIMITE_DE_RESERVAS_RAPIDAS_POR_PLACA = 3;
    public static final int LIMITE_DE_DIAS_PARA_RESERVA = 2;
    
    public void validacoesDaVaga(Vaga vaga, OffsetDateTime inicio, OffsetDateTime fim);
    public void validacoesDeAutorizacao(UUID usuarioLogadoId , Motorista motorista, Veiculo veiculo);
    public void validacoesDeConflito(Vaga vaga, ReservaDTO reserva);
    public void validacoesTemporais(OffsetDateTime inicio, OffsetDateTime fim);
    public void validarLimiteIntrodutorioReservasRapidas(String placa);
}