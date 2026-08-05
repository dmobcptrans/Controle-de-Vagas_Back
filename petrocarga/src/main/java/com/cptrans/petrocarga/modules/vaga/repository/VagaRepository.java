package com.cptrans.petrocarga.modules.vaga.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.enums.StatusVagaEnum;
import com.cptrans.petrocarga.modules.vaga.entity.Vaga;


@Repository
public interface  VagaRepository extends JpaRepository<Vaga, UUID>, JpaSpecificationExecutor<Vaga> {
    List<Vaga> findByIdIn(List<UUID> ids);
    List<Vaga> findByStatus(StatusVagaEnum status);
    Page<Vaga> findByStatus(StatusVagaEnum status, Pageable pageable);
    Page<Vaga> findByEnderecoLogradouroContainingIgnoreCase(String logradouro, Pageable pageable);
    Page<Vaga> findByStatusAndEnderecoLogradouroContainingIgnoreCase(StatusVagaEnum status, String logradouro, Pageable pageable);

    @Query("""
        SELECT v FROM Vaga v 
        INNER JOIN DisponibilidadeVaga dv ON dv.vaga.id = v.id
        WHERE dv.fim > CURRENT_TIMESTAMP AND dv.inicio <= CURRENT_TIMESTAMP
        AND v.status = 'DISPONIVEL'
    """)
    List<Vaga> buscarDisponiveis();

    @Query("SELECT COALESCE(SUM(v.comprimento), 0) FROM Vaga v")
    Long sumTotalAvailableLengthMeters();

    @Query("""
        SELECT v FROM Vaga v
        WHERE v.latitudeFim >= :south
        AND v.latitudeInicio <= :north
        AND v.longitudeFim >= :west
        AND v.longitudeInicio <= :east
        AND v.status = :status
    """)
    List<Vaga> buscarPorArea(
            Double south,
            Double north,
            Double west,
            Double east,
            StatusVagaEnum status
    );

    @Query("""
        SELECT v FROM Vaga v
        INNER JOIN DisponibilidadeVaga dv on dv.vaga.id = v.id
        WHERE v.latitudeFim >= :south
        AND v.latitudeInicio <= :north
        AND v.longitudeFim >= :west
        AND v.longitudeInicio <= :east
        AND v.status = 'DISPONIVEL'
        AND dv.fim > :agora
        AND dv.inicio <= :agora
    """)
    List<Vaga> buscarDisponiveisPorArea(
            Double south,
            Double north,
            Double west,
            Double east,
            OffsetDateTime agora
        );
}
