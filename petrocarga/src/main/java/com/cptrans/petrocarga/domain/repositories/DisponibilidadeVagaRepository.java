package com.cptrans.petrocarga.domain.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.domain.entities.DisponibilidadeVaga;

@Repository
public interface DisponibilidadeVagaRepository extends JpaRepository<DisponibilidadeVaga, UUID> {
    public List<DisponibilidadeVaga> findByVagaId(UUID vagaId);
    public List<DisponibilidadeVaga> findByVagaEnderecoCodigoPmp(String codigoPMP);
    public List<DisponibilidadeVaga> findByFimGreaterThanAndInicioLessThan(OffsetDateTime inicio, OffsetDateTime fim);
    public boolean existsByVagaIdAndFimGreaterThanAndInicioLessThan(UUID vagaId, OffsetDateTime inicio, OffsetDateTime fim);
}
