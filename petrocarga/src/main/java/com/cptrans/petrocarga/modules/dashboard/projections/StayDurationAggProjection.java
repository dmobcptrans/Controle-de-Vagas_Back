package com.cptrans.petrocarga.modules.dashboard.projections;

import java.math.BigDecimal;

public interface StayDurationAggProjection {
    Long getTotalCount();

    BigDecimal getSumMinutes();

    BigDecimal getMinMinutes();

    BigDecimal getMaxMinutes();
}
