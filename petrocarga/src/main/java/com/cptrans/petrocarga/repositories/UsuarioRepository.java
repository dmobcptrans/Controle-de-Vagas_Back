package com.cptrans.petrocarga.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.models.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID>, JpaSpecificationExecutor<Usuario> {
    public Optional<Usuario> findByEmail(String email);
    public Optional<Usuario> findByEmailOrGoogleId(String email, String google_id);
    public Optional<Usuario> findByEmailOrCpfHash(String email, String cpfHash);
    public Optional<Usuario> findByCpfHash(String cpfHash);
    public Boolean existsByEmail(String email);
    public Boolean existsByCpfHash(String cpfHash);
    public List<Usuario> findByPermissao(PermissaoEnum permissao);
    public List<Usuario> findByPermissaoAndAtivo(PermissaoEnum permissao, Boolean ativo);
    public Optional<Usuario> findByIdAndAtivo(UUID id, Boolean ativo);
    @Query("SELECT u FROM Usuario u WHERE (u.email = :email OR u.cpfHash = :cpfHash) AND u.ativo = :ativo")
    public Optional<Usuario> findByEmailOrCpfHashAndAtivo(@Param("email") String email, @Param("cpfHash") String cpfHash, @Param("ativo") Boolean ativo);
}