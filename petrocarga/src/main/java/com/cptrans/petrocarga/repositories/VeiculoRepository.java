package com.cptrans.petrocarga.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.models.Usuario;
import com.cptrans.petrocarga.models.Veiculo;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {
    public List<Veiculo> findByPlaca(String placa);
    public List<Veiculo> findByPlacaAndAtivo(String placa, Boolean ativo);
    public Optional<Veiculo> findByPlacaAndUsuario(String placa, Usuario usuario);
    public List<Veiculo> findByUsuario(Usuario usuario);
    public List<Veiculo> findByUsuarioAndAtivo(Usuario usuario, Boolean ativo);
    public Optional<Veiculo> findByIdAndAtivo(UUID id, Boolean ativo);
    public Boolean existsByPlaca(String placa);
    public Boolean existsByUsuarioId(UUID usuarioId);
}
