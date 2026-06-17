package com.cptrans.petrocarga.modules.veiculo.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, UUID> {
    public List<Veiculo> findByPlaca(String placa);
    public List<Veiculo> findByPlacaAndAtivo(String placa, Boolean ativo);
    public Optional<Veiculo> findByPlacaAndUsuario(String placa, Usuario usuario);
    public List<Veiculo> findByUsuario(Usuario usuario);
    public List<Veiculo> findByUsuarioAndAtivo(Usuario usuario, Boolean ativo);
    public Optional<Veiculo> findByIdAndAtivo(UUID id, Boolean ativo);
    public Optional<Veiculo> findByIdAndAtivoTrue(UUID id);
    public Optional<Veiculo> findByIdAndAtivoTrueAndUsuarioAtivoTrue(UUID id);
    public Optional<Veiculo> findByIdAndAtivoTrueAndUsuarioIdAndUsuarioAtivoTrueAndUsuarioPermissao(UUID id, UUID usuarioId, PermissaoEnum permissao);
    public Boolean existsByPlaca(String placa);
    public Boolean existsByUsuarioIdAndAtivo(UUID usuarioId, boolean ativo);
}
