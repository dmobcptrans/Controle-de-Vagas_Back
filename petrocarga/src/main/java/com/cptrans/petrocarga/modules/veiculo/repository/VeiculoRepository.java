package com.cptrans.petrocarga.modules.veiculo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, UUID>, JpaSpecificationExecutor<Veiculo> {
    public List<Veiculo> findByPlaca(String placa);
    public List<Veiculo> findByPlacaAndAtivo(String placa, Boolean ativo);
    public Optional<Veiculo> findByPlacaAndUsuarioId(String placa, UUID usuarioId);
    public Optional<Veiculo> findByIdNotAndPlacaAndUsuarioId(UUID id, String placa, UUID usuarioId);
    public List<Veiculo> findByUsuarioId(UUID usuarioId);
    public List<Veiculo> findByUsuarioIdAndAtivo(UUID usuarioId, Boolean ativo);
    public List<Veiculo> findByUsuarioIdAndAtivoTrueAndUsuarioAtivoTrue(UUID usuarioId);
    public Optional<Veiculo> findByIdAndAtivo(UUID id, Boolean ativo);
    public Optional<Veiculo> findByIdAndAtivoTrueAndUsuarioIdAndUsuarioAtivoTrue(UUID id, UUID usuarioId);
    public Optional<Veiculo> findByIdAndAtivoTrue(UUID id);
    public Optional<Veiculo> findByIdAndAtivoTrueAndUsuarioAtivoTrue(UUID id);
    public Boolean existsByPlaca(String placa);
    public Boolean existsByUsuarioIdAndAtivo(UUID usuarioId, boolean ativo);
}
