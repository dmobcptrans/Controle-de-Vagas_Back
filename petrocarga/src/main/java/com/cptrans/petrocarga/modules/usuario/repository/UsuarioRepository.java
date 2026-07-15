package com.cptrans.petrocarga.modules.usuario.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.enums.PermissaoEnum;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID>, JpaSpecificationExecutor<Usuario> {
    public Optional<Usuario> findByEmailHash(String emailHash);
    public Optional<Usuario> findByEmailHashAndAtivo(String emailHash, Boolean ativo);
    public Optional<Usuario> findByEmailHashOrGoogleId(String emailHash, String google_id);
    public Boolean existsByEmailHash(String emailHash);
    public Boolean existsByEmailHashAndIdNot(String emailHash, UUID id);
    public List<Usuario> findByPermissao(PermissaoEnum permissao);
    public List<Usuario> findByPermissaoAndAtivo(PermissaoEnum permissao, Boolean ativo);
    public Optional<Usuario> findByIdAndAtivo(UUID id, Boolean ativo);
    public Optional<Usuario> findByIdAndAtivoAndPermissaoInAndDesativadoEmNotNull(UUID id, Boolean ativo, List<PermissaoEnum> permissoes);
}