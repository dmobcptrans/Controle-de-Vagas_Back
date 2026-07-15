package com.cptrans.petrocarga.modules.motorista.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.modules.motorista.dto.request.MotoristaFiltrosDTO;
import com.cptrans.petrocarga.modules.motorista.entity.Motorista;
import com.cptrans.petrocarga.shared.utils.StringUtils;
import com.cptrans.petrocarga.shared.utils.Utils;

import jakarta.persistence.criteria.Predicate;

public class MotoristaSpecification {
    /**
     * Cria uma Specification para filtrar motoristas com base nos filtros passados.
     *
     * Os filtros são: nome, telefone, cpf, cnh e ativo.
     * Se nenhum filtro for passado, então retorna uma Specification que não filtra nada.
     *
     * @param filtros o objeto com os filtros para a busca
     * @return a Specification que filtra os motoristas com base nos filtros passados
     */
     public static Specification<Motorista> filtrar(
        MotoristaFiltrosDTO filtros
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (filtros.getId() != null) {
                predicates.add(
                    cb.equal(root.get("id"), filtros.getId())
                );
            }

            if (filtros.getNome() != null) {
                predicates.add(
                    cb.like(cb.lower(Utils.createUnaccentExpression(cb, root.get("usuario").get("nome"))), "%" + StringUtils.normalize(filtros.getNome().trim().toLowerCase()) + "%")
                );
            }

            if (filtros.getTelefone() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("telefoneHash"), filtros.getTelefone())
                );
            }

            if (filtros.getEmail() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("emailHash"), filtros.getEmail())
                );
            }

            if (filtros.getCpf() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("cpfHash"), filtros.getCpf())
                );
            }

            if (filtros.getCnh() != null) {
                predicates.add(
                    cb.equal(root.get("cnhHash"), filtros.getCnh())
                );
            }

            if (filtros.getEmpresaId() != null) {
                predicates.add(
                    cb.equal(root.get("empresa").get("id"), filtros.getEmpresaId())
                );
            }

            if (filtros.getEmpresaCnpj() != null) {
                predicates.add(
                    cb.like(root.get("empresa").get("cnpj"), "%" + filtros.getEmpresaCnpj().trim() + "%")
                );
            }

            if (filtros.getEmpresaRazaoSocial() != null) {
                predicates.add(
                    cb.like(cb.lower(Utils.createUnaccentExpression(cb, root.get("empresa").get("usuario").get("nome"))), "%" + StringUtils.normalize(filtros.getEmpresaRazaoSocial().trim().toLowerCase()) + "%")
                );
            }

            if (filtros.getAtivo() != null) {
                predicates.add(
                    cb.equal(root.get("usuario").get("ativo") , filtros.getAtivo())
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}