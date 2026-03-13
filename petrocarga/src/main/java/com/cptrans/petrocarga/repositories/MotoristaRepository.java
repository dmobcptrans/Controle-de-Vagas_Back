package com.cptrans.petrocarga.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.models.Motorista;

@Repository
public interface MotoristaRepository extends JpaRepository<Motorista, UUID>, JpaSpecificationExecutor<Motorista> {
    public Optional<Motorista> findByUsuarioIdAndUsuarioAtivo(UUID usuarioId, Boolean ativo);
    public Optional<Motorista> findByUsuarioId(UUID usuarioId);
    public Optional<Motorista> findByCnhHash(String cnhHash);
    public Boolean existsByCnhHash(String cnhHash);
}
