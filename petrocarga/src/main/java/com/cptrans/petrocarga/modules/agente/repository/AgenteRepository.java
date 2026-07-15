package com.cptrans.petrocarga.modules.agente.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.agente.entity.Agente;

@Repository
public interface AgenteRepository extends JpaRepository<Agente, UUID>, JpaSpecificationExecutor<Agente> {
    public Optional<Agente> findByIdAndUsuarioAtivo(UUID agenteId, Boolean ativo);
    public Optional<Agente> findByMatricula(String matricula);
    public Optional<Agente> findByCpfHash(String cpfHash);
    public Optional<Agente> findByCpfHashAndUsuarioAtivo(String cpfHash, Boolean ativo);
    public Boolean existsByMatricula(String matricula);
    public Boolean existsByMatriculaAndIdNot(String matricula, UUID id);
    public Boolean existsByCpfHash(String cpfHash);
    public Boolean existsByCpfHashAndIdNot(String cpfHash, UUID id);
    @Query("SELECT a.cpfCripto FROM Agente a WHERE a.id = :id")
    public Optional<String> findCpfCriptoById(UUID id);
}