package com.cptrans.petrocarga.domain.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cptrans.petrocarga.domain.entities.VeiculoEmpresaMotorista;
import com.cptrans.petrocarga.domain.entities.VeiculoEmpresaMotoristaId;

@Repository
public interface VeiculoMotoristaAssociacaoRepository extends JpaRepository<VeiculoEmpresaMotorista, VeiculoEmpresaMotoristaId> {
}
