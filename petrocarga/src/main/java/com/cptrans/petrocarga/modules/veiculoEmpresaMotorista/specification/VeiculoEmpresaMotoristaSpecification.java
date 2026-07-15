package com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.dto.request.VeiculoEmpresaMotoristaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.veiculoEmpresaMotorista.entity.VeiculoEmpresaMotorista;
import com.cptrans.petrocarga.shared.utils.StringUtils;
import com.cptrans.petrocarga.shared.utils.Utils;

import jakarta.persistence.criteria.Predicate;

public class VeiculoEmpresaMotoristaSpecification {
    
    public static Specification<VeiculoEmpresaMotorista> filtrar(VeiculoEmpresaMotoristaFiltrosRequestDTO filtros) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtros.getVeiculoId() != null) {
                predicates.add(cb.equal(root.get("veiculo").get("id"), filtros.getVeiculoId()));
            }

            if (filtros.getPlaca() != null) {
                predicates.add(cb.like(cb.upper(root.get("veiculo").get("placa")), "%" + filtros.getPlaca().trim().toUpperCase() + "%"));
            }

            if (filtros.getMarca() != null) {
                predicates.add(cb.like(cb.upper(root.get("veiculo").get("marca")), "%" + filtros.getMarca().trim().toUpperCase() + "%"));
            }

            if (filtros.getModelo() != null) {
                predicates.add(cb.like(cb.upper(root.get("veiculo").get("modelo")), "%" + filtros.getModelo().trim().toUpperCase() + "%"));
            }

            if (filtros.getTipoVeiculo() != null) {
                predicates.add(cb.equal(root.get("veiculo").get("tipo"), filtros.getTipoVeiculo()));
            }

            if (filtros.getVeiculoAtivo() != null) {
                predicates.add(cb.equal(root.get("veiculo").get("ativo"), filtros.getVeiculoAtivo()));
            }

            if (filtros.getEmpresaId() != null) {
                predicates.add(cb.equal(root.get("empresa").get("id"), filtros.getEmpresaId()));
            }

            if (filtros.getEmpresaCnpj() != null) {
                predicates.add(cb.like(cb.upper(root.get("empresa").get("cnpj")), "%" + filtros.getEmpresaCnpj().trim().toUpperCase() + "%"));
            }

            if (filtros.getEmpresaRazaoSocial() != null) {
                predicates.add(cb.like(Utils.createUnaccentExpression(cb, cb.upper(root.get("empresa").get("usuario").get("nome"))), "%" + StringUtils.normalize(filtros.getEmpresaRazaoSocial().trim().toUpperCase()) + "%"));
            }

            if (filtros.getMotoristaId() != null) {
                predicates.add(cb.equal(root.get("motorista").get("id"), filtros.getMotoristaId()));
            }

            if (filtros.getMotoristaNome() != null) {
                predicates.add(cb.like(Utils.createUnaccentExpression(cb, cb.upper(root.get("motorista").get("usuario").get("nome"))), "%" + StringUtils.normalize(filtros.getMotoristaNome().trim().toUpperCase()) + "%"));
            }

            if (filtros.getMotoristaCpf() != null) {
                predicates.add(cb.equal(root.get("motorista").get("cpfHash"), filtros.getMotoristaCpf()));
            }

            if (filtros.getMotoristaTelefone() != null) {
                predicates.add(cb.equal(root.get("motorista").get("usuario").get("telefoneHash"), filtros.getMotoristaTelefone()));
            }

            if (filtros.getMotoristaEmail() != null) {
                predicates.add(cb.equal(root.get("motorista").get("usuario").get("emailHash"), filtros.getMotoristaEmail()));
            }

            if (filtros.getMotoristaAtivo() != null) {
                predicates.add(cb.equal(root.get("motorista").get("usuario").get("ativo"), filtros.getMotoristaAtivo()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}