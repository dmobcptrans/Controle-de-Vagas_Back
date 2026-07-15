package com.cptrans.petrocarga.modules.gestor.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.cptrans.petrocarga.modules.gestor.entity.Gestor;

public interface GestorRepository extends JpaRepository<Gestor, UUID>, JpaSpecificationExecutor<Gestor> {
    public Boolean existsByCpfHash(String cpfHash);
    public Boolean existsByCpfHashAndIdNot(String cpfHash, UUID id);
    public Optional<Gestor> findByCpfHash(String cpfHash);
    public Optional<Gestor> findByCpfHashAndUsuarioAtivo(String cpfHash, Boolean ativo);

    @Query("SELECT g.cpfCripto FROM Gestor g WHERE g.id = :id")
    public Optional<String> findCpfCriptoById(UUID id);
}