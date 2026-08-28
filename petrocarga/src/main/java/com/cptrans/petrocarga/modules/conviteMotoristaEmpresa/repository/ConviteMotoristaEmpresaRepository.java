package com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.entity.ConviteMotoristaEmpresa;

@Repository
public interface ConviteMotoristaEmpresaRepository extends JpaRepository<ConviteMotoristaEmpresa, UUID>, JpaSpecificationExecutor<ConviteMotoristaEmpresa> {

    @Query("""
        SELECT c FROM ConviteMotoristaEmpresa c 
        WHERE c.tokenHash = :tokenHash 
        AND c.validoAte > CURRENT_TIMESTAMP
    """)
    public Optional<ConviteMotoristaEmpresa> findValidoByTokenHash(String tokenHash);

    @Query("""
        SELECT c FROM ConviteMotoristaEmpresa c 
        WHERE c.id = :id 
        AND c.empresa.id = :empresaId
        AND c.status = 'PENDENTE'
        AND c.validoAte > CURRENT_TIMESTAMP
    """)
    public Optional<ConviteMotoristaEmpresa> findPendenteValidoByIdAndEmpresaId(UUID id, UUID empresaId);

    @Query("""
        SELECT c FROM ConviteMotoristaEmpresa c 
        WHERE c.motoristaEmailHash = :motoristaEmailHash 
        AND c.empresa.id = :empresaId 
        AND c.validoAte > CURRENT_TIMESTAMP
    """)
    public Optional<ConviteMotoristaEmpresa> findConviteValidoByMotoristaEmailAndEmpresaId(String motoristaEmailHash, UUID empresaId);

    @Query("""
        SELECT c FROM ConviteMotoristaEmpresa c 
        WHERE c.motorista.id = :motoristaId 
        AND c.empresa.id = :empresaId 
        AND c.validoAte > CURRENT_TIMESTAMP
    """)
    public Optional<ConviteMotoristaEmpresa> findConviteValidoByMotoristaIdAndEmpresaId(UUID motoristaId, UUID empresaId);

    @Query("""
        SELECT c FROM ConviteMotoristaEmpresa c 
        WHERE c.id = :id
        AND c.motorista.id = :motoristaId 
        AND c.validoAte > CURRENT_TIMESTAMP
    """)
    public Optional<ConviteMotoristaEmpresa> findConviteValidoByIdAndMotoristaId(UUID id, UUID motoristaId);

    @Query("""
        SELECT CASE WHEN COUNT(c) > 0 
        THEN true 
        ELSE false END
        FROM ConviteMotoristaEmpresa c
        WHERE c.motoristaEmailHash = :motoristaEmailHash
        AND c.empresa.id = :empresaId
        AND c.validoAte > CURRENT_TIMESTAMP
    """)
    public boolean existsValidoByMotoristaEmailHashAndEmpresaId(String motoristaEmailHash, UUID empresaId);
}