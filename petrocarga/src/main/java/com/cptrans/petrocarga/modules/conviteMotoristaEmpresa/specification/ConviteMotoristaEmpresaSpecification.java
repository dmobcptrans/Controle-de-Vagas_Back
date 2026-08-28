package com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.dto.request.ConviteMotoristaEmpresaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.conviteMotoristaEmpresa.entity.ConviteMotoristaEmpresa;
import com.cptrans.petrocarga.shared.utils.StringUtils;
import com.cptrans.petrocarga.shared.utils.Utils;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConviteMotoristaEmpresaSpecification {

    public static Specification<ConviteMotoristaEmpresa> filtrar(ConviteMotoristaEmpresaFiltrosRequestDTO filtros) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtros.getConviteId() != null) {
                predicates.add(cb.equal(root.get("id"), filtros.getConviteId()));
            }

            if (filtros.getEmpresaId() != null) {
                predicates.add(cb.equal(root.get("empresa").get("id"), filtros.getEmpresaId()));
            }

            if (filtros.getMotoristaId() != null) {
                predicates.add(cb.equal(root.get("motorista").get("id"), filtros.getMotoristaId()));
            }

            if (filtros.getListStatus() != null && !filtros.getListStatus().isEmpty()) {
                predicates.add(root.get("status").in(filtros.getListStatus()));
            }

            if (filtros.getCnpj() != null && !filtros.getCnpj().trim().isEmpty()) {
                predicates.add(cb.like(root.get("empresa").get("cnpj"), "%" + filtros.getCnpj().trim() + "%"));
            }

            if (filtros.getRazaoSocial() != null && !filtros.getRazaoSocial().trim().isEmpty()) {
                predicates.add(cb.like(Utils.createUnaccentExpression(cb, cb.lower(root.get("empresa").get("usuario").get("nome"))), "%" + StringUtils.removerAcentos(filtros.getRazaoSocial().trim().toLowerCase())));
            }

            if (filtros.getMotoristaNome() != null && !filtros.getMotoristaNome().trim().isEmpty()) {
                predicates.add(cb.like(Utils.createUnaccentExpression(cb, cb.lower(root.get("motorista").get("usuario").get("nome"))), "%" + StringUtils.removerAcentos(filtros.getMotoristaNome().trim().toLowerCase())));
            }

            if (filtros.getMotoristaEmail() != null && !filtros.getMotoristaEmail().trim().isEmpty()) {
                predicates.add(cb.like(root.get("motorista").get("usuario").get("emailHash"), "%" + filtros.getMotoristaEmail().trim() + "%"));
            }

            return cb.and(predicates.toArray(Predicate[]::new));

        };
    }
}
