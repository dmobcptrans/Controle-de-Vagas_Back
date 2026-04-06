package com.cptrans.petrocarga.domain.specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.cptrans.petrocarga.domain.entities.Reserva;
import com.cptrans.petrocarga.domain.enums.StatusReservaEnum;
import com.cptrans.petrocarga.shared.utils.DateUtils;

import jakarta.persistence.criteria.Predicate;

public class ReservaSpecification {
    public static Specification<Reserva> filtrar(
        UUID usuarioId,
        UUID vagaId,
        String placaVeiculo,
        LocalDate data,
        Integer mes,
        Integer ano,
        List<StatusReservaEnum> listaStatus
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if(usuarioId != null) {
                predicates.add(
                    cb.equal(root.get("motorista").get("id"), usuarioId )
                );
            }

            if (vagaId != null) {
                predicates.add(
                    cb.equal(root.get("vaga").get("id"), vagaId)
                );
            }

            if (placaVeiculo != null) {
                predicates.add(
                    cb.equal(root.get("veiculo").get("placa"), placaVeiculo.toUpperCase())
                );
            }

            if (data != null) {
                OffsetDateTime inicioDia = data.atStartOfDay(DateUtils.FUSO_BRASIL).toOffsetDateTime();
                OffsetDateTime inicioProximoDia = data.plusDays(1).atStartOfDay(DateUtils.FUSO_BRASIL).toOffsetDateTime();

                predicates.add(
                    cb.greaterThanOrEqualTo(root.get("inicio"), inicioDia)
                );
                predicates.add(
                    cb.lessThan(root.get("inicio"), inicioProximoDia)
                );
            }

            if (mes != null && ano != null) {
                LocalDate primeiroDiaMes = LocalDate.of(ano, mes, 1);
                LocalDate primeiroDiaProximoMes = primeiroDiaMes.plusMonths(1);

                OffsetDateTime inicioMes = primeiroDiaMes.atStartOfDay(DateUtils.FUSO_BRASIL).toOffsetDateTime();
                OffsetDateTime inicioProximoMes = primeiroDiaProximoMes.atStartOfDay(DateUtils.FUSO_BRASIL).toOffsetDateTime();

                predicates.add(
                    cb.greaterThanOrEqualTo(root.get("inicio"), inicioMes)
                );
                predicates.add(
                    cb.lessThan(root.get("inicio"), inicioProximoMes)
                );
            }

            if (listaStatus != null && !listaStatus.isEmpty()) {
                predicates.add(
                    root.get("status").in(listaStatus)
                );
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
