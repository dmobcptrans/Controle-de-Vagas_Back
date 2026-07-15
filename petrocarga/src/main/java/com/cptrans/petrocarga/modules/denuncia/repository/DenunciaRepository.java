package com.cptrans.petrocarga.modules.denuncia.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.enums.StatusDenunciaEnum;
import com.cptrans.petrocarga.enums.TipoDenunciaEnum;
import com.cptrans.petrocarga.modules.denuncia.entity.Denuncia;

@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, UUID>, JpaSpecificationExecutor<Denuncia> {
    public boolean existsByReservaId(UUID reservaId);
    public List<Denuncia> findByCriadoPorIdAndStatus(UUID usuarioId, StatusDenunciaEnum status);
    public List<Denuncia> findByCriadoPorId(UUID usuarioId);
    public Page<Denuncia> findByCriadoPorId(UUID usuarioId, Pageable pageable);
    public Page<Denuncia> findByCriadoPorIdAndStatusIn(UUID usuarioId, List<StatusDenunciaEnum> listaStatus, Pageable pageable);
    public List<Denuncia> findByVagaId(UUID vagaId);
    public List<Denuncia> findByStatusIn(List<StatusDenunciaEnum> status);
    public List<Denuncia> findByTipoIn(List<TipoDenunciaEnum> tipo);
    public List<Denuncia> findByVagaIdAndStatusInAndTipoIn(UUID vagaId, List<StatusDenunciaEnum> status, List<TipoDenunciaEnum> tipo);

}
