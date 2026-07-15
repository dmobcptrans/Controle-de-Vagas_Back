package com.cptrans.petrocarga.modules.motorista.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.motorista.entity.Motorista;

@Repository
public interface MotoristaRepository extends JpaRepository<Motorista, UUID>, JpaSpecificationExecutor<Motorista> {
    public Optional<Motorista> findByIdAndUsuarioAtivo(UUID id, Boolean ativo);
    public Optional<Motorista> findByIdAndUsuarioAtivoTrueAndEmpresaId(UUID id, UUID empresaId);
    public Optional<Motorista> findByIdAndUsuarioAtivoTrue(UUID id);
    public Optional<Motorista> findByCnhHash(String cnhHash);
    public Optional<Motorista> findByCpfHashAndUsuarioAtivo(String cpfHash, Boolean ativo);
    public Optional<Motorista> findByCpfHash(String cpfHash);
    public Optional<Motorista> findByUsuarioEmailHash(String emailHash);
    public Optional<Motorista> findByIdAndEmpresaId(UUID id, UUID empresaId);
    public Page<Motorista> findByEmpresaId(UUID empresaId, Pageable pageable);
    public Boolean existsByCpfHash(String cpfHash);
    public Boolean existsByCpfHashAndIdNot(String cpfHash, UUID id);
    public Boolean existsByCnhHash(String cnhHash);
    public Boolean existsByCnhHashAndIdNot(String cnhHash, UUID id);
    @Query("SELECT m.cpfCripto FROM Motorista m WHERE m.id = :id")
    public Optional<String> findCpfCriptoById(UUID id);
}
