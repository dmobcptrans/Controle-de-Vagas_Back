package com.cptrans.petrocarga.domain.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.domain.entities.Usuario;
import com.cptrans.petrocarga.domain.enums.PermissaoEnum;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID>, JpaSpecificationExecutor<Usuario> {
    public Optional<Usuario> findByEmailHash(String emailHash);
    public Optional<Usuario> findByEmailHashOrGoogleId(String emailHash, String google_id);
    public Optional<Usuario> findByCpfHash(String cpfHash);
    public Boolean existsByEmailHash(String emailHash);
    public Boolean existsByCpfHash(String cpfHash);
    public List<Usuario> findByPermissao(PermissaoEnum permissao);
    public List<Usuario> findByPermissaoAndAtivo(PermissaoEnum permissao, Boolean ativo);
    public Optional<Usuario> findByIdAndAtivo(UUID id, Boolean ativo);
    public Optional<Usuario> findByIdAndAtivoAndPermissaoInAndDesativadoEmNotNull(UUID id, Boolean ativo, List<PermissaoEnum> permissoes);
    @Query("SELECT u FROM Usuario u WHERE (u.emailHash = :emailHash OR u.cpfHash = :cpfHash) AND u.ativo = :ativo")
    public Optional<Usuario> findByEmailHashOrCpfHashAndAtivo(@Param("emailHash") String emailHash, @Param("cpfHash") String cpfHash, @Param("ativo") Boolean ativo);
}