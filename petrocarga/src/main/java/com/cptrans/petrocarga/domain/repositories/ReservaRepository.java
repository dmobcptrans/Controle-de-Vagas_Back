package com.cptrans.petrocarga.domain.repositories;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;
import com.cptrans.petrocarga.domain.repositories.projections.StayDurationAggProjection;
import com.cptrans.petrocarga.domain.repositories.projections.VehicleRouteEventProjection;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
    public List<Reserva> findByVaga(Vaga vaga);
    public List<Reserva> findByVagaAndStatus(Vaga vaga, StatusReservaEnum status);
    public List<Reserva> findByVagaAndStatusIn(Vaga vaga, List<StatusReservaEnum> status);
    public List<Reserva> findByCriadoPor(Usuario criadoPor);
    public List<Reserva> findByCriadoPorAndStatusIn(Usuario criadoPor, List<StatusReservaEnum> status);
    public List<Reserva> findByStatusIn(List<StatusReservaEnum> status);
    public List<Reserva> findByVagaAndStatusAndInicio(Vaga vaga, StatusReservaEnum status, OffsetDateTime data);
    public List<Reserva> findByVeiculoPlacaIgnoringCaseAndStatusIn(String placa, List<StatusReservaEnum> status);
    public Integer countByVeiculoPlacaIgnoringCaseAndStatusIn(String placa,List<StatusReservaEnum> status);
    public List<Reserva> findByFimGreaterThanAndInicioLessThanAndStatusIn(OffsetDateTime novoInicio, OffsetDateTime novoFim, List<StatusReservaEnum> status);
    public List<Reserva> findByFimGreaterThanAndInicioLessThanAndMotoristaUsuarioIdAndStatusIn(OffsetDateTime novoInicio, OffsetDateTime novoFim, UUID usuarioId, List<StatusReservaEnum> status);
    public Boolean existsByVeiculoIdAndStatusIn(UUID veiculoId, List<StatusReservaEnum> status);

    @Query("SELECT r FROM Reserva r " +
           "JOIN FETCH r.vaga " +
           "JOIN FETCH r.motorista " +
           "JOIN FETCH r.veiculo " +
           "JOIN FETCH r.criadoPor " +
           "WHERE r.id = :id")
    Reserva findByIdWithJoins(@Param("id") UUID id);

    @Query("SELECT r FROM Reserva r " +
           "WHERE r.status = :status " +
           "AND r.checkedIn = false " +
           "AND r.inicio < :agora " +
           "AND FUNCTION('TIMESTAMPADD', MINUTE, :graceMinutes, r.inicio) <= :agora " +
           "AND r.fim > :agora")
    List<Reserva> findNoShowCandidates(
        @Param("status") StatusReservaEnum status,
        @Param("graceMinutes") int graceMinutes,
        @Param("agora") OffsetDateTime agora
    );

    @Query("SELECT COUNT(DISTINCT v.id) FROM Vaga v")
    Long countTotalSlots();

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.status = 'RESERVADA' AND r.inicio BETWEEN :startDate AND :endDate")
    Long countPendingReservations(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.status = 'ATIVA' AND r.inicio BETWEEN :startDate AND :endDate")
    Long countActiveReservations(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    /**
     * "Ativas no período" (overlap) != "iniciadas no período".
     *
     * <p>Ativas no período significa que houve interseção com o intervalo consultado:
     * {@code r.inicio <= endDate AND (r.fim IS NULL OR r.fim >= startDate)}.</p>
     */
    @Query("SELECT COUNT(r) FROM Reserva r " +
           "WHERE r.status = 'ATIVA' " +
           "AND r.inicio <= :endDate " +
           "AND (r.fim IS NULL OR r.fim >= :startDate)")
    Long countActiveReservationsDuringPeriod(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

              /**
               * Soma de (comprimento do veículo em metros × minutos de overlap) para reservas ATIVAS.
               *
               * <p>Usado para métricas de ocupação por comprimento no dashboard. Não altera as validações
               * existentes; apenas agrega dados já persistidos.</p>
               */
              @Query(
                     value = """
                            SELECT
                                   COALESCE(
                                          SUM(
                                                 (
                                                        COALESCE(
                                                               ve.comprimento,
                                                               CASE ve.tipo
                                                                      WHEN 'AUTOMOVEL' THEN 5
                                                                      WHEN 'VUC' THEN 7
                                                                      WHEN 'CAMINHONETA' THEN 8
                                                                      WHEN 'CAMINHAO_MEDIO' THEN 12
                                                                      WHEN 'CAMINHAO_LONGO' THEN 19
                                                                      ELSE 0
                                                               END
                                                        )
                                                 )
                                                 * GREATEST(
                                                               EXTRACT(EPOCH FROM (LEAST(r.fim, :endDate) - GREATEST(r.inicio, :startDate))) / 60.0,
                                                               0
                                                        )
                                          ),
                                          0
                                   ) AS occupiedLengthMinute
                            FROM reserva r
                            JOIN veiculo ve ON ve.id = r.veiculo_id
                            WHERE r.status = 'ATIVA'
                                   AND r.inicio <= :endDate
                                   AND r.fim >= :startDate
                     """,
                     nativeQuery = true
              )
              BigDecimal sumOccupiedLengthMinutesActiveDuringPeriod(
                            @Param("startDate") OffsetDateTime startDate,
                            @Param("endDate") OffsetDateTime endDate
              );

                            /**
                             * Eventos de trajeto para reconstrução (origem -> vaga(s) -> destino), unificando Reserva e ReservaRapida.
                             *
                             * <p>Filtro por interseção do intervalo: {@code inicio <= endDate AND fim >= startDate}.
                             * Isso permite retornar eventos que começaram antes de {@code startDate} mas ainda estavam em vigor no período.</p>
                             */
                            @Query(
                                   value = """
                                          SELECT
                                                 x.placa AS placa,
                                                 x.source AS source,
                                                 x.inicio AS inicio,
                                                 x.fim AS fim,
                                                 x.cidade_origem AS cidadeOrigem,
                                                 x.entrada_cidade AS entradaCidade,
                                                 x.vaga_id AS vagaId,
                                                 x.vaga_label AS vagaLabel
                                          FROM (
                                                 SELECT
                                                        ve.placa AS placa,
                                                        'RESERVA' AS source,
                                                        r.inicio AS inicio,
                                                        r.fim AS fim,
                                                        r.cidade_origem AS cidade_origem,
                                                        r.entrada_cidade AS entrada_cidade,
                                                        r.vaga_id AS vaga_id,
                                                        COALESCE(CONCAT(ev.logradouro, ', ', v.numero_endereco), 'SEM ENDERECO') AS vaga_label
                                                 FROM reserva r
                                                 JOIN veiculo ve ON ve.id = r.veiculo_id
                                                 JOIN vaga v ON v.id = r.vaga_id
                                                 LEFT JOIN endereco_vaga ev ON ev.id = v.endereco_id
                                                 WHERE r.inicio <= :endDate
                                                        AND r.fim >= :startDate

                                                 UNION ALL

                                                 SELECT
                                                        rr.placa AS placa,
                                                        'RESERVA_RAPIDA' AS source,
                                                        rr.inicio AS inicio,
                                                        rr.fim AS fim,
                                                        NULL AS cidade_origem,
                                                        NULL AS entrada_cidade,
                                                        rr.vaga_id AS vaga_id,
                                                        COALESCE(CONCAT(ev2.logradouro, ', ', v2.numero_endereco), 'SEM ENDERECO') AS vaga_label
                                                 FROM reserva_rapida rr
                                                 JOIN vaga v2 ON v2.id = rr.vaga_id
                                                 LEFT JOIN endereco_vaga ev2 ON ev2.id = v2.endereco_id
                                                 WHERE rr.inicio <= :endDate
                                                        AND rr.fim >= :startDate
                                          ) x
                                          ORDER BY x.placa ASC, x.inicio ASC
                                   """,
                                   nativeQuery = true
                            )
                            List<VehicleRouteEventProjection> getVehicleRouteEventsDuringPeriod(
                                          @Param("startDate") OffsetDateTime startDate,
                                          @Param("endDate") OffsetDateTime endDate
                            );

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.status = 'CONCLUIDA' AND r.inicio BETWEEN :startDate AND :endDate")
    Long countCompletedReservations(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.status = 'CANCELADA' AND r.inicio BETWEEN :startDate AND :endDate")
    Long countCanceledReservations(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.status = 'REMOVIDA' AND r.inicio BETWEEN :startDate AND :endDate")
    Long countRemovedReservations(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.inicio BETWEEN :startDate AND :endDate")
    Long countTotalReservationsInPeriod(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT COUNT(r) FROM Reserva r WHERE r.inicio BETWEEN :startDate AND :endDate " +
           "AND (r.veiculo.tipo = 'CAMINHAO_MEDIO' OR r.veiculo.tipo = 'CAMINHAO_LONGO')")
    Long countMultipleSlotReservations(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT new map(CAST(r.veiculo.tipo AS string) as tipo, COUNT(DISTINCT r.id) as count, COUNT(DISTINCT r.veiculo.id) as uniqueVehicles) " +
           "FROM Reserva r " +
           "WHERE r.veiculo.tipo IS NOT NULL " +
           "AND r.inicio BETWEEN :startDate AND :endDate " +
           "GROUP BY r.veiculo.tipo " +
           "ORDER BY count DESC")
    List<java.util.Map<String, Object>> getVehicleTypeStats(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT new map(ev.bairro as name, COUNT(r.id) as count) " +
           "FROM Reserva r " +
           "JOIN r.vaga v " +
           "JOIN v.endereco ev " +
           "WHERE ev.bairro IS NOT NULL " +
           "AND r.inicio BETWEEN :startDate AND :endDate " +
           "GROUP BY ev.bairro " +
           "ORDER BY count DESC")
    List<java.util.Map<String, Object>> getDistrictStats(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT new map(r.cidadeOrigem as name, COUNT(r.id) as count) " +
           "FROM Reserva r " +
           "WHERE r.cidadeOrigem IS NOT NULL " +
           "AND r.inicio BETWEEN :startDate AND :endDate " +
           "GROUP BY r.cidadeOrigem " +
           "ORDER BY count DESC")
    List<java.util.Map<String, Object>> getOriginStats(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT new map(r.entradaCidade as name, COUNT(r.id) as count) " +
           "FROM Reserva r " +
           "WHERE r.entradaCidade IS NOT NULL " +
           "AND r.inicio BETWEEN :startDate AND :endDate " +
           "GROUP BY r.entradaCidade " +
           "ORDER BY count DESC")
    List<java.util.Map<String, Object>> getEntryOriginStats(@Param("startDate") OffsetDateTime startDate, @Param("endDate") OffsetDateTime endDate);

@Query(
  value = """
    SELECT
      COALESCE(CONCAT(ev.logradouro, ', ', v.numero_endereco), 'SEM ENDERECO') AS name,
      SUM(x.total_utilizacoes) AS count
    FROM (
       SELECT
         r.vaga_id,
         COUNT(1) AS total_utilizacoes
       FROM reserva r
       WHERE r.status = 'CONCLUIDA'
         AND r.inicio BETWEEN :startDate AND :endDate
       GROUP BY r.vaga_id

       UNION ALL

       SELECT
         rr.vaga_id,
         COUNT(1) AS total_utilizacoes
       FROM reserva_rapida rr
       WHERE rr.inicio BETWEEN :startDate AND :endDate
       GROUP BY rr.vaga_id
    ) x
    JOIN vaga v ON v.id = x.vaga_id
    LEFT JOIN endereco_vaga ev ON ev.id = v.endereco_id
    GROUP BY ev.logradouro, v.numero_endereco
    ORDER BY count DESC
  """,
  nativeQuery = true
)
List<Object[]> getMostUsedVagas(
    @Param("startDate") OffsetDateTime startDate,
    @Param("endDate") OffsetDateTime endDate
);

@Query(value ="""
              select EXISTS (
    SELECT 1
    FROM reserva r
    LEFT JOIN motorista m ON r.motorista_id = m.id
    WHERE
        (r.criado_por = :usuarioId OR m.usuario_id = :usuarioId)
        AND r.status IN ('RESERVADA', 'ATIVA')
);
       """,
       nativeQuery = true)
Boolean existsByCriadoPorIdOrMotoristaUsuarioId (@Param("usuarioId") UUID usuarioId);

@Query(
       value = """
              SELECT
                     COUNT(1) AS totalCount,
                     COALESCE(SUM(EXTRACT(EPOCH FROM (r.check_out_em - r.check_in_em)) / 60.0), 0) AS sumMinutes,
                     MIN(EXTRACT(EPOCH FROM (r.check_out_em - r.check_in_em)) / 60.0) AS minMinutes,
                     MAX(EXTRACT(EPOCH FROM (r.check_out_em - r.check_in_em)) / 60.0) AS maxMinutes
              FROM reserva r
              WHERE r.status = 'CONCLUIDA'
                     AND r.inicio BETWEEN :startDate AND :endDate
                     AND r.check_in_em IS NOT NULL
                     AND r.check_out_em IS NOT NULL
                     AND r.check_out_em >= r.check_in_em
       """,
       nativeQuery = true
)
StayDurationAggProjection getStayDurationAggCompletedReservations(
              @Param("startDate") OffsetDateTime startDate,
              @Param("endDate") OffsetDateTime endDate
);

}