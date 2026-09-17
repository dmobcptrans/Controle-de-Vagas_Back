package com.cptrans.petrocarga.modules.vaga.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.modules.vaga.dto.projection.ClusterMapaProjection;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;


@Repository
public interface  VagaRepository extends JpaRepository<Vaga, UUID>, JpaSpecificationExecutor<Vaga> {
    public List<Vaga> findByIdIn(List<UUID> ids);
    public List<Vaga> findByStatus(StatusVagaEnum status);
    public Page<Vaga> findByStatus(StatusVagaEnum status, Pageable pageable);
    public Page<Vaga> findByEnderecoLogradouroContainingIgnoreCase(String logradouro, Pageable pageable);
    public Page<Vaga> findByStatusAndEnderecoLogradouroContainingIgnoreCase(StatusVagaEnum status, String logradouro, Pageable pageable);

    @Query("""
        SELECT v FROM Vaga v 
        INNER JOIN DisponibilidadeVaga dv ON dv.vaga.id = v.id
        WHERE dv.fim > CURRENT_TIMESTAMP AND dv.inicio <= CURRENT_TIMESTAMP
        AND v.status = 'DISPONIVEL'
    """)
    public List<Vaga> buscarDisponiveis();

    @Query("SELECT COALESCE(SUM(v.comprimento), 0) FROM Vaga v")
    Long sumTotalAvailableLengthMeters();

    @Query("""
        SELECT DISTINCT v FROM Vaga v
        WHERE v.latitudeFim >= :south
        AND v.latitudeInicio <= :north
        AND v.longitudeFim >= :west
        AND v.longitudeInicio <= :east
        AND v.status = :status
    """)
    public List<Vaga> buscarPorArea(
        Double north,
        Double south,
        Double east,
        Double west,
        StatusVagaEnum status,
        Pageable pageable
    );

    @Query(value = """
        SELECT v.*
        FROM vaga v
        WHERE v.latitude_fim >= :south
        AND v.latitude_inicio <= :north
        AND v.longitude_fim >= :west
        AND v.longitude_inicio <= :east
        AND v.status = 'DISPONIVEL'

        AND EXISTS (
            SELECT 1
            FROM operacao_vaga ov

            CROSS JOIN LATERAL (
                SELECT gs::date AS data_operacao
                FROM generate_series(
                    CAST(:dataHoje AS date),
                    CAST(:dataDepoisAmanha AS date),
                    INTERVAL '1 day'
                ) AS gs
            ) d

            CROSS JOIN LATERAL (
                SELECT
                    CASE EXTRACT(DOW FROM d.data_operacao)
                        WHEN 0 THEN 'DOMINGO'
                        WHEN 1 THEN 'SEGUNDA'
                        WHEN 2 THEN 'TERCA'
                        WHEN 3 THEN 'QUARTA'
                        WHEN 4 THEN 'QUINTA'
                        WHEN 5 THEN 'SEXTA'
                        WHEN 6 THEN 'SABADO'
                    END AS dia_semana,

                    (
                        d.data_operacao + ov.hora_inicio
                    ) AT TIME ZONE make_interval(secs => :offsetSegundos)
                        AS inicio_operacao,

                    (
                        d.data_operacao + ov.hora_fim
                    ) AT TIME ZONE make_interval(secs => :offsetSegundos)
                        AS fim_operacao
            ) horario

            WHERE ov.vaga_id = v.id
            AND ov.dia_semana = horario.dia_semana

            AND (
                d.data_operacao <> CAST(:dataHoje AS date)
                OR horario.fim_operacao > :agora
            )

            AND EXISTS (
                SELECT 1
                FROM disponibilidade_vaga dv
                WHERE dv.vaga_id = v.id

                AND dv.fim >
                    CASE
                        WHEN d.data_operacao = CAST(:dataHoje AS date)
                            THEN :agora
                        ELSE horario.inicio_operacao
                    END

                AND dv.inicio < horario.fim_operacao
            )
        )
        """,
        nativeQuery = true)
    public List<Vaga> buscarDisponiveisPorArea(
        Double north,
        Double south,
        Double east,
        Double west,
        OffsetDateTime agora,
        LocalDate dataHoje,
        LocalDate dataDepoisAmanha,
        Integer offsetSegundos,
        Pageable limite
    );

    @Query(value = """
        SELECT
            FLOOR(((v.latitude_inicio + v.latitude_fim) / 2) / :tamanhoCelula) AS grid_lat,
            FLOOR(((v.longitude_inicio + v.longitude_fim) / 2) / :tamanhoCelula) AS grid_lng,
            AVG((v.latitude_inicio + v.latitude_fim) / 2) AS latitude,
            AVG((v.longitude_inicio + v.longitude_fim) / 2) AS longitude,
            COUNT(*) AS quantidade
        FROM vaga v
        WHERE v.latitude_fim >= :south
        AND v.latitude_inicio <= :north
        AND v.longitude_fim >= :west
        AND v.longitude_inicio <= :east
        AND v.status = :status
        GROUP BY
            FLOOR(((v.latitude_inicio + v.latitude_fim) / 2) / :tamanhoCelula),
            FLOOR(((v.longitude_inicio + v.longitude_fim) / 2) / :tamanhoCelula)
        """,
        nativeQuery = true)
    public List<ClusterMapaProjection> buscarClustersPorArea(
        Double north,
        Double south,
        Double east,
        Double west,
        StatusVagaEnum status,
        Double tamanhoCelula
    );

    @Query(value = """
        WITH vagas_elegiveis AS (

            SELECT
                v.id,

                (v.latitude_inicio + v.latitude_fim) / 2.0
                    AS latitude,

                (v.longitude_inicio + v.longitude_fim) / 2.0
                    AS longitude,

                FLOOR(
                    (
                        (v.latitude_inicio + v.latitude_fim) / 2.0
                    ) / :tamanhoCelula
                ) AS grid_lat,

                FLOOR(
                    (
                        (v.longitude_inicio + v.longitude_fim) / 2.0
                    ) / :tamanhoCelula
                ) AS grid_lng

            FROM vaga v

            WHERE v.latitude_fim >= :south
            AND v.latitude_inicio <= :north
            AND v.longitude_fim >= :west
            AND v.longitude_inicio <= :east
            AND v.status = 'DISPONIVEL'

            AND EXISTS (
                SELECT 1
                FROM operacao_vaga ov

                CROSS JOIN LATERAL (
                    SELECT gs::date AS data_operacao
                    FROM generate_series(
                        CAST(:dataHoje AS date),
                        CAST(:dataDepoisAmanha AS date),
                        INTERVAL '1 day'
                    ) AS gs
                ) d

                CROSS JOIN LATERAL (
                    SELECT
                        CASE EXTRACT(DOW FROM d.data_operacao)
                            WHEN 0 THEN 'DOMINGO'
                            WHEN 1 THEN 'SEGUNDA'
                            WHEN 2 THEN 'TERCA'
                            WHEN 3 THEN 'QUARTA'
                            WHEN 4 THEN 'QUINTA'
                            WHEN 5 THEN 'SEXTA'
                            WHEN 6 THEN 'SABADO'
                        END AS dia_semana,

                        (
                            d.data_operacao + ov.hora_inicio
                        ) AT TIME ZONE make_interval(
                            secs => :offsetSegundos
                        ) AS inicio_operacao,

                        (
                            d.data_operacao + ov.hora_fim
                        ) AT TIME ZONE make_interval(
                            secs => :offsetSegundos
                        ) AS fim_operacao

                ) horario

                WHERE ov.vaga_id = v.id
                AND ov.dia_semana = horario.dia_semana

                AND (
                    d.data_operacao <> CAST(:dataHoje AS date)
                    OR horario.fim_operacao > :agora
                )

                AND EXISTS (
                    SELECT 1
                    FROM disponibilidade_vaga dv

                    WHERE dv.vaga_id = v.id

                    AND dv.fim >
                        CASE
                            WHEN d.data_operacao = CAST(:dataHoje AS date)
                                THEN :agora
                            ELSE horario.inicio_operacao
                        END

                    AND dv.inicio < horario.fim_operacao
                )
            )
        )

        SELECT
            grid_lat,
            grid_lng,

            AVG(latitude) AS latitude,
            AVG(longitude) AS longitude,

            COUNT(*) AS quantidade

        FROM vagas_elegiveis

        GROUP BY
            grid_lat,
            grid_lng
        """,
        nativeQuery = true)
    public List<ClusterMapaProjection> buscarClustersDisponiveisPorArea(
        @Param("north") Double north,
        @Param("south") Double south,
        @Param("east") Double east,
        @Param("west") Double west,
        @Param("agora") OffsetDateTime agora,
        @Param("dataHoje") LocalDate dataHoje,
        @Param("dataDepoisAmanha") LocalDate dataDepoisAmanha,
        @Param("offsetSegundos") Integer offsetSegundos,
        @Param("tamanhoCelula") Double tamanhoCelula
    );

    @Query(value = """
        SELECT DISTINCT v.id
        FROM vaga v
        INNER JOIN operacao_vaga ov
            ON ov.vaga_id = v.id

        WHERE v.id IN (:vagaIds)
        AND v.status = 'DISPONIVEL'
        AND ov.dia_semana = :diaSemana

        AND CAST(:agora AS time) >= ov.hora_inicio
        AND CAST(:agora AS time) < ov.hora_fim

        AND EXISTS (
            SELECT 1
            FROM disponibilidade_vaga dv
            WHERE dv.vaga_id = v.id

            AND dv.fim > :agora

            AND dv.inicio <
                (
                    CAST(CAST(:agora AS date) AS timestamp)
                    + ov.hora_fim
                ) AT TIME ZONE make_interval(
                    secs => :offsetSegundos
                )
        )
        """,
        nativeQuery = true)
    public Set<UUID> buscarIdsDisponiveisHoje(
        Set<UUID> vagaIds,
        String diaSemana,
        OffsetDateTime agora,
        Integer offsetSegundos
    );
}