package com.cptrans.petrocarga.modules.empresa.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import com.cptrans.petrocarga.modules.empresa.dto.request.EmpresaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.empresa.entity.Empresa;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class EmpresaSpecification {

    public static Specification<Empresa> filtrar(EmpresaFiltrosRequestDTO filtros) {
        return (root, query, cb) -> {  
            List<Predicate> predicates = new ArrayList<>();

            if (filtros.getEmpresaId() != null) {
                predicates.add(cb.equal(root.get("id"), filtros.getEmpresaId()));
            }

            if (filtros.getUsuarioId() != null) {
                predicates.add(cb.equal(root.get("usuario").get("id"), filtros.getUsuarioId()));
            }

            if (filtros.getCnpj() != null) {
                predicates.add(cb.equal(root.get("cnpj"), filtros.getCnpj()));
            }

            if (filtros.getNome() != null) {
                predicates.add(cb.like(cb.lower(root.get("usuario").get("nome")), "%" + filtros.getNome().trim().toLowerCase() + "%"));
            }

            if (filtros.getRazaoSocial() != null) {
                predicates.add(cb.like(cb.lower(root.get("razaoSocial")), "%" + filtros.getRazaoSocial().trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
