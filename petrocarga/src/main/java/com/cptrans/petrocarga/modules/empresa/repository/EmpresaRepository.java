package com.cptrans.petrocarga.modules.empresa.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.empresa.entity.Empresa;

import java.util.UUID;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, UUID>, JpaSpecificationExecutor<Empresa> {
    public Optional<Empresa> findByIdAndUsuarioAtivoTrue(UUID id);
    public Optional<Empresa> findByIdAndUsuarioAtivoFalse(UUID id);
    public Optional<Empresa> findByCnpj(String cnpj);
    public Optional<Empresa> findByCnpjAndUsuarioAtivo(String cnpj, Boolean ativo);
    public boolean existsByCnpj(String cnpj);
    public boolean existsByCnpjAndIdNot(String cnpj, UUID id);
    @Query("SELECT e.cnpj FROM Empresa e WHERE e.id = :id")
    public Optional<String> findCnpjById(UUID id);
}
