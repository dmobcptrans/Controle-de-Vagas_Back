package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class VeiculoEmpresaMotoristaId implements Serializable {

    @Column(name = "veiculo_id")
    private UUID veiculoId;
    
    @Column(name = "empresa_id")
    private UUID empresaId;

    @Column(name = "motorista_id")
    private UUID motoristaId;

}