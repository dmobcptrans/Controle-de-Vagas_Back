package com.cptrans.petrocarga.domain.repositories.projections;

import java.math.BigDecimal;

public interface StayDurationAggProjection {
    Long getTotalCount();

    BigDecimal getSumMinutes();

    BigDecimal getMinMinutes();

    BigDecimal getMaxMinutes();
}
