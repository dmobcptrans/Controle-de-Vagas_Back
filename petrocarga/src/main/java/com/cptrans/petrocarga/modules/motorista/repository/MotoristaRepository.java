package com.cptrans.petrocarga.modules.motorista.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.motorista.entity.Motorista;

@Repository
public interface MotoristaRepository extends JpaRepository<Motorista, UUID>, JpaSpecificationExecutor<Motorista> {
    public Optional<Motorista> findByUsuarioIdAndUsuarioAtivo(UUID usuarioId, Boolean ativo);
    public Optional<Motorista> findByIdAndUsuarioAtivoTrueAndEmpresaId(UUID id, UUID empresaId);
    public Optional<Motorista> findByIdAndUsuarioAtivoTrue(UUID id);
    public Optional<Motorista> findByUsuarioId(UUID usuarioId);
    public Optional<Motorista> findByCnhHash(String cnhHash);
    public Optional<Motorista> findByUsuarioCpfHashAndUsuarioAtivoTrue(String cpfHash);
    public Boolean existsByUsuarioCpfHash(String cpfHash);
    public Boolean existsByCnhHash(String cnhHash);
}
