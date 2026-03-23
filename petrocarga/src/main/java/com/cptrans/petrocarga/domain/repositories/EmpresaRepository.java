package com.cptrans.petrocarga.domain.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.domain.entities.Empresa;

import java.util.UUID;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {
    public Optional<Empresa> findByUsuarioId(UUID id);
}
