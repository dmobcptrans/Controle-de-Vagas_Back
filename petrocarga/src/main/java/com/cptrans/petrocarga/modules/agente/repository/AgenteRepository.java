package com.cptrans.petrocarga.modules.agente.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.agente.entity.Agente;
import com.cptrans.petrocarga.modules.usuario.entity.Usuario;

@Repository
public interface AgenteRepository extends JpaRepository<Agente, UUID>, JpaSpecificationExecutor<Agente> {
    public Optional<Agente> findByUsuario(Usuario usuario);
    public Optional<Agente> findByUsuarioAndUsuarioAtivo(Usuario usuario, Boolean ativo);
    public Optional<Agente> findByIdAndUsuarioAtivo(UUID agenteId, Boolean ativo);
    public Optional<Agente> findByMatricula(String matricula);
    public Boolean existsByMatricula(String matricula);
}
