package com.cptrans.petrocarga.shared.utils;

import com.cptrans.petrocarga.domain.entities.Vaga;

public class VagaUtils {

    public static void preencherCoordenadasSeNecessario(Vaga vaga) {

        if (vaga.getLatitudeInicio() == null || vaga.getLongitudeInicio() == null) {
            if (vaga.getReferenciaGeoInicio() != null) {
                try {
                    String[] parts = vaga.getReferenciaGeoInicio().split(",");

                    vaga.setLatitudeInicio(Double.parseDouble(parts[0].trim()));
                    vaga.setLongitudeInicio(Double.parseDouble(parts[1].trim()));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Formato inválido de referenciaGeoInicio: "
                            + vaga.getReferenciaGeoInicio());
                }
            }
        }

        if (vaga.getLatitudeFim() == null || vaga.getLongitudeFim() == null) {
            if (vaga.getReferenciaGeoFim() != null) {
                try {
                    String[] parts = vaga.getReferenciaGeoFim().split(",");

                    vaga.setLatitudeFim(Double.parseDouble(parts[0].trim()));
                    vaga.setLongitudeFim(Double.parseDouble(parts[1].trim()));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Formato inválido de referenciaGeoFim: "
                            + vaga.getReferenciaGeoFim());
                }
            }
        }
    }
}