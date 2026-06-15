package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity.VeiculoEmpresaMotorista;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity.VeiculoEmpresaMotoristaId;

@Repository
public interface VeiculoEmpresaMotoristaRepository extends JpaRepository<VeiculoEmpresaMotorista, VeiculoEmpresaMotoristaId> {
}
