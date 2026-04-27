package com.cptrans.petrocarga.domain.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.domain.entities.Vaga;
import com.cptrans.petrocarga.domain.enums.StatusVagaEnum;


@Repository
public interface  VagaRepository extends JpaRepository<Vaga, UUID> {
    List<Vaga> findByStatus(StatusVagaEnum status);
    Page<Vaga> findByStatus(StatusVagaEnum status, Pageable pageable);
    Page<Vaga> findByEnderecoLogradouroContainingIgnoreCase(String logradouro, Pageable pageable);
    Page<Vaga> findByStatusAndEnderecoLogradouroContainingIgnoreCase(StatusVagaEnum status, String logradouro, Pageable pageable);

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
}
