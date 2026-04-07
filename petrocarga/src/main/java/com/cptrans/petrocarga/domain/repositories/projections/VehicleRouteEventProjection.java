package com.cptrans.petrocarga.domain.repositories.projections;

import java.time.Instant;
import java.util.UUID;

public interface VehicleRouteEventProjection {
    String getPlaca();
    String getSource();
    Instant getInicio();
    Instant getFim();
    String getCidadeOrigem();
    String getEntradaCidade();
    UUID getVagaId();
    String getVagaLabel();
}