package com.cptrans.petrocarga.modules.denuncia.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.modules.denuncia.dto.request.DenunciaFiltrosRequestDTO;
import com.cptrans.petrocarga.modules.denuncia.entity.Denuncia;
import com.cptrans.petrocarga.shared.utils.StringUtils;
import com.cptrans.petrocarga.shared.utils.Utils;

import jakarta.persistence.criteria.Predicate;

public class DenunciaSpecification {
    public static Specification<Denuncia> filtrar(DenunciaFiltrosRequestDTO filtros){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filtros.getId() != null) {
                predicates.add(cb.equal(root.get("id"), filtros.getId()));
            }

            if (filtros.getVagaId() != null) {
                predicates.add(cb.equal(root.get("vaga").get("id"), filtros.getVagaId()));
            }

            if (filtros.getReservaId() != null){
                predicates.add(cb.equal(root.get("reserva").get("id"), filtros.getReservaId()));
            }

            if (filtros.getCriadoPorId() != null){
                predicates.add(cb.equal(root.get("criadoPor").get("id"), filtros.getCriadoPorId()));
            }

            if (filtros.getCriadoPorNome() != null && !filtros.getCriadoPorNome().trim().isEmpty()){
                predicates.add(cb.like(Utils.createUnaccentExpression(cb, cb.upper(root.get("criadoPor").get("nome"))), "%" + StringUtils.normalize(filtros.getCriadoPorNome().trim().toUpperCase()) + "%"));
            }

            if (filtros.getListaStatus() != null && !filtros.getListaStatus().isEmpty()){
                predicates.add(root.get("status").in(filtros.getListaStatus()));
            }

            if (filtros.getListaTipos() != null && !filtros.getListaTipos().isEmpty()){
                predicates.add(root.get("tipo").in(filtros.getListaTipos()));
            }
           
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}