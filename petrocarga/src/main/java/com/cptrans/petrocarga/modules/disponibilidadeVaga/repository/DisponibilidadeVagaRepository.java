package com.cptrans.petrocarga.modules.disponibilidadeVaga.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.disponibilidadeVaga.entity.DisponibilidadeVaga;

@Repository
public interface DisponibilidadeVagaRepository extends JpaRepository<DisponibilidadeVaga, UUID> {
    public List<DisponibilidadeVaga> findByIdIn(List<UUID> listaIds);
    public List<DisponibilidadeVaga> findByVagaId(UUID vagaId);
    public Page<DisponibilidadeVaga> findByVagaId(UUID vagaId, Pageable pageable);
    public List<DisponibilidadeVaga> findByVagaEnderecoCodigoPmp(String codigoPMP);
    public List<DisponibilidadeVaga> findByFimGreaterThanAndInicioLessThan(OffsetDateTime inicio, OffsetDateTime fim);
    public List<DisponibilidadeVaga> findByVagaIdAndFimGreaterThanAndInicioLessThan(UUID vagaId, OffsetDateTime inicio, OffsetDateTime fim);
    public boolean existsByVagaIdAndFimGreaterThanAndInicioLessThan(UUID vagaId, OffsetDateTime inicio, OffsetDateTime fim);
    public boolean existsByIdNotAndVagaIdAndFimGreaterThanAndInicioLessThan(UUID id,UUID vagaId, OffsetDateTime inicio, OffsetDateTime fim);
}