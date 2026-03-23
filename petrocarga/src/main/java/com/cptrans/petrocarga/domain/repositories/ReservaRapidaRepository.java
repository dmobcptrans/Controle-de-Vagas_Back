package com.cptrans.petrocarga.domain.repositories;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.domain.entities.Agente;
import com.cptrans.petrocarga.domain.entities.ReservaRapida;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;
import com.cptrans.petrocarga.domain.repositories.projections.StayDurationAggProjection;

@Repository
public interface ReservaRapidaRepository extends JpaRepository<ReservaRapida, UUID>, JpaSpecificationExecutor<ReservaRapida> {
    public List<ReservaRapida> findByStatusIn(List<StatusReservaEnum> status);
    public List<ReservaRapida> findByVaga(Vaga vaga);
    public List<ReservaRapida> findByVagaAndStatus(Vaga vaga, StatusReservaEnum status);
    public List<ReservaRapida> findByVagaAndStatusIn(Vaga vaga, List<StatusReservaEnum> status);
    public List<ReservaRapida> findByPlacaIgnoringCaseAndStatus(String placa, StatusReservaEnum status);
    public List<ReservaRapida> findByAgente(Agente agente);
    public List<ReservaRapida> findByAgenteAndVagaId(Agente agente, UUID vagaId);
    public List<ReservaRapida> findByAgenteAndPlacaIgnoringCase(Agente agente, String placaVeiculo);
    public List<ReservaRapida> findByAgenteAndStatusIn(Agente agente, List<StatusReservaEnum> listaStatus);
    public List<ReservaRapida> findByFimGreaterThanAndInicioLessThanAndStatusIn(OffsetDateTime novoInicio, OffsetDateTime novoFim, List<StatusReservaEnum> status);
    public List<ReservaRapida> findByAgenteAndVagaIdAndPlacaIgnoringCaseAndStatusIn(Agente agente, UUID vagaId, String placaVeiculo, List<StatusReservaEnum> status);
    public Integer countByPlacaIgnoringCase(String placa);
    
    @Query("SELECT COUNT(rr) FROM ReservaRapida rr WHERE rr.status = 'ATIVA' AND rr.inicio BETWEEN :startDate AND :endDate")
    Long countActiveReservations(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

        /**
         * "Ativas no período" (overlap) != "iniciadas no período".
         *
         * <p>Ativas no período significa que houve interseção com o intervalo consultado:
         * {@code rr.inicio <= endDate AND (rr.fim IS NULL OR rr.fim >= startDate)}.</p>
         */
        @Query("SELECT COUNT(rr) FROM ReservaRapida rr " +
            "WHERE rr.status = 'ATIVA' " +
            "AND rr.inicio <= :endDate " +
            "AND (rr.fim IS NULL OR rr.fim >= :startDate)")
        Long countActiveReservationsDuringPeriod(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

        /**
         * Soma de (comprimento do TipoVeiculoEnum em metros × minutos de overlap) para reservas rápidas ATIVAS.
         */
        @Query(
            value = """
                SELECT
                    COALESCE(
                        SUM(
                            (
                                CASE rr.tipo_veiculo
                                    WHEN 'AUTOMOVEL' THEN 5
                                    WHEN 'VUC' THEN 7
                                    WHEN 'CAMINHONETA' THEN 8
                                    WHEN 'CAMINHAO_MEDIO' THEN 12
                                    WHEN 'CAMINHAO_LONGO' THEN 19
                                    ELSE 0
                                END
                            )
                            * GREATEST(
                                    EXTRACT(EPOCH FROM (LEAST(rr.fim, :endDate) - GREATEST(rr.inicio, :startDate))) / 60.0,
                                    0
                                )
                        ),
                        0
                    ) AS occupiedLengthMinute
                FROM reserva_rapida rr
                WHERE rr.status = 'ATIVA'
                    AND rr.inicio <= :endDate
                    AND rr.fim >= :startDate
            """,
            nativeQuery = true
        )
        BigDecimal sumOccupiedLengthMinutesActiveDuringPeriod(
                @Param("startDate") OffsetDateTime startDate,
                @Param("endDate") OffsetDateTime endDate
        );

    @Query("SELECT COUNT(rr) FROM ReservaRapida rr WHERE rr.status = 'CONCLUIDA' AND rr.inicio BETWEEN :startDate AND :endDate")
    Long countCompletedReservations(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT COUNT(rr) FROM ReservaRapida rr WHERE rr.status = 'REMOVIDA' AND rr.inicio BETWEEN :startDate AND :endDate")
    Long countRemovedReservations(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT COUNT(rr) FROM ReservaRapida rr WHERE rr.inicio BETWEEN :startDate AND :endDate")
    Long countTotalReservationsInPeriod(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    // @Query("SELECT COUNT(rr) FROM ReservaRapida rr WHERE rr.inicio BETWEEN :startDate AND :endDate " +
    //        "AND (rr.tipo_veiculo = 'CAMINHAO_MEDIO' OR rr.tipo_veiculo = 'CAMINHAO_LONGO')")
    // Long countMultipleSlotReservations(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);


    // @Query("SELECT new map(CAST(rr.tipoVeiculo AS string) as tipo, COUNT(DISTINCT rr.id) as count, COUNT(DISTINCT rr.placaVeiculo) as uniqueVehicles) " +
    //        "FROM ReservaRapida rr " +
    //        "WHERE rr.tipoVeiculo IS NOT NULL " +
    //        "AND rr.inicio BETWEEN :startDate AND :endDate " +
    //        "GROUP BY rr.tipoVeiculo " +
    //        "ORDER BY count DESC")
    // List<java.util.Map<String, Object>> getVehicleTypeStats(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT new map(ev.bairro as name, COUNT(rr.id) as count) " +
           "FROM ReservaRapida rr " +
           "JOIN rr.vaga v " +
           "JOIN v.endereco ev " +
           "WHERE ev.bairro IS NOT NULL " +
           "AND rr.inicio BETWEEN :startDate AND :endDate " +
           "GROUP BY ev.bairro " +
           "ORDER BY count DESC")
    List<java.util.Map<String, Object>> getDistrictStats(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

        @Query(
            value = """
                SELECT
                    COUNT(1) AS totalCount,
                    COALESCE(SUM(EXTRACT(EPOCH FROM (rr.fim - rr.inicio)) / 60.0), 0) AS sumMinutes,
                    MIN(EXTRACT(EPOCH FROM (rr.fim - rr.inicio)) / 60.0) AS minMinutes,
                    MAX(EXTRACT(EPOCH FROM (rr.fim - rr.inicio)) / 60.0) AS maxMinutes
                FROM reserva_rapida rr
                WHERE rr.status = 'CONCLUIDA'
                    AND rr.inicio BETWEEN :startDate AND :endDate
                    AND rr.inicio IS NOT NULL
                    AND rr.fim IS NOT NULL
                    AND rr.fim >= rr.inicio
            """,
            nativeQuery = true
        )
        StayDurationAggProjection getStayDurationAggCompletedReservations(
                @Param("startDate") OffsetDateTime startDate,
                @Param("endDate") OffsetDateTime endDate
        );

}
