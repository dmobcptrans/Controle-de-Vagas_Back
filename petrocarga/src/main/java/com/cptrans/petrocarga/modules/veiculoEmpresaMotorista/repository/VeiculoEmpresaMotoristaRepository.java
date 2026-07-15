package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.modules.veiculo.entity.Veiculo;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity.VeiculoEmpresaMotorista;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity.VeiculoEmpresaMotoristaId;

@Repository
public interface VeiculoEmpresaMotoristaRepository extends JpaRepository<VeiculoEmpresaMotorista, VeiculoEmpresaMotoristaId>, JpaSpecificationExecutor<VeiculoEmpresaMotorista> {
    public Boolean existsByEmpresaIdAndMotoristaId(UUID empresaId, UUID motoristaId);
    public Boolean existsByVeiculoIdAndMotoristaId(UUID veiculoId, UUID motoristaId);
    public Boolean existsByVeiculoIdAndMotoristaIdAndEmpresaId(UUID veiculoId, UUID motoristaId, UUID empresaId);
    public Optional<VeiculoEmpresaMotorista> findByVeiculoIdAndMotoristaId(UUID veiculoId, UUID motoristaId);
    public Optional<VeiculoEmpresaMotorista> findByVeiculoIdAndMotoristaIdAndEmpresaId(UUID veiculoId, UUID motoristaId, UUID empresaId);
    public Page<VeiculoEmpresaMotorista> findByEmpresaId(UUID empresaId, Pageable pageable);
    public Page<VeiculoEmpresaMotorista> findByEmpresaIdAndMotoristaId(UUID empresaId, UUID motoristaId, Pageable pageable);
    
    @Modifying
    @Query("DELETE FROM VeiculoEmpresaMotorista vem WHERE vem.motorista.id = :motoristaId")
    public void deleteAllByMotoristaId(UUID motoristaId);

    @Query("SELECT vem.veiculo FROM VeiculoEmpresaMotorista vem WHERE vem.empresa.id = :empresaId AND vem.motorista.id = :motoristaId")
    public Page<Veiculo> findVeiculoByEmpresaIdAndMotoristaId(UUID empresaId, UUID motoristaId, Pageable pageable, Specification<VeiculoEmpresaMotorista> specification);

    @Query("SELECT vem.motorista FROM VeiculoEmpresaMotorista vem WHERE vem.veiculo.id = :veiculoId AND vem.empresa.id = :empresaId")
    public Page<Motorista> findMotoristaByVeiculoIdAndEmpresaId(UUID veiculoId, UUID empresaId, Pageable pageable);
}
