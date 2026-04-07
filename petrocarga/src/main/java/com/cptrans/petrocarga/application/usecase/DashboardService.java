package com.cptrans.petrocarga.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.cptrans.petrocarga.application.dto.dashboard.ActiveDuringPeriodStatsDTO;
import com.cptrans.petrocarga.application.dto.dashboard.DashboardKpiDTO;
import com.cptrans.petrocarga.application.dto.dashboard.DashboardSummaryDTO;
import com.cptrans.petrocarga.application.dto.dashboard.LengthOccupancyStatsDTO;
import com.cptrans.petrocarga.application.dto.dashboard.LocationStatDTO;
import com.cptrans.petrocarga.application.dto.dashboard.StayDurationStatsDTO;
import com.cptrans.petrocarga.application.dto.dashboard.VehicleRouteReportDTO;
import com.cptrans.petrocarga.application.dto.dashboard.VehicleRouteStopDTO;
import com.cptrans.petrocarga.application.dto.dashboard.VehicleTypeStatDTO;
import com.cptrans.petrocarga.domain.repositories.ReservaRapidaRepository;
import com.cptrans.petrocarga.domain.repositories.ReservaRepository;
import com.cptrans.petrocarga.domain.repositories.VagaRepository;
import com.cptrans.petrocarga.domain.repositories.projections.StayDurationAggProjection;
import com.cptrans.petrocarga.domain.repositories.projections.VehicleRouteEventProjection;

@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ReservaRapidaRepository reservaRapidaRepository;

    @Autowired
    private VagaRepository vagaRepository;

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private StayDurationStatsDTO getStayDurationStats(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            OffsetDateTime resolvedStart = resolveStartDate(startDate);
            OffsetDateTime resolvedEnd = resolveEndDate(endDate);

            StayDurationAggProjection reservaAgg = reservaRepository.getStayDurationAggCompletedReservations(resolvedStart, resolvedEnd);
            StayDurationAggProjection rapidaAgg = reservaRapidaRepository.getStayDurationAggCompletedReservations(resolvedStart, resolvedEnd);

            long countReserva = reservaAgg != null && reservaAgg.getTotalCount() != null ? reservaAgg.getTotalCount() : 0L;
            long countRapida = rapidaAgg != null && rapidaAgg.getTotalCount() != null ? rapidaAgg.getTotalCount() : 0L;
            long totalCount = countReserva + countRapida;

            if (totalCount <= 0) {
                log.info("getStayDurationStats: nenhuma reserva concluída no período, retornando valores padrão");
                return new StayDurationStatsDTO(0.0, 0.0, 0.0);
            }

            BigDecimal sumReserva = reservaAgg != null ? reservaAgg.getSumMinutes() : null;
            BigDecimal sumRapida = rapidaAgg != null ? rapidaAgg.getSumMinutes() : null;
            BigDecimal totalSum = (sumReserva != null ? sumReserva : BigDecimal.ZERO)
                .add(sumRapida != null ? sumRapida : BigDecimal.ZERO);

            BigDecimal avgMinutes = totalSum.divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP);

            BigDecimal minReserva = reservaAgg != null ? reservaAgg.getMinMinutes() : null;
            BigDecimal minRapida = rapidaAgg != null ? rapidaAgg.getMinMinutes() : null;
            BigDecimal maxReserva = reservaAgg != null ? reservaAgg.getMaxMinutes() : null;
            BigDecimal maxRapida = rapidaAgg != null ? rapidaAgg.getMaxMinutes() : null;

            BigDecimal minMinutes;
            if (minReserva != null && minRapida != null) {
                minMinutes = minReserva.min(minRapida);
            } else {
                minMinutes = minReserva != null ? minReserva : minRapida;
            }

            BigDecimal maxMinutes;
            if (maxReserva != null && maxRapida != null) {
                maxMinutes = maxReserva.max(maxRapida);
            } else {
                maxMinutes = maxReserva != null ? maxReserva : maxRapida;
            }

            return new StayDurationStatsDTO(
                avgMinutes.doubleValue(),
                minMinutes != null ? minMinutes.doubleValue() : 0.0,
                maxMinutes != null ? maxMinutes.doubleValue() : 0.0
            );
        } catch (Exception e) {
            log.error("Erro em getStayDurationStats: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return new StayDurationStatsDTO(0.0, 0.0, 0.0);
        }
    }

    private ActiveDuringPeriodStatsDTO getActiveDuringPeriodStats(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            OffsetDateTime resolvedStart = resolveStartDate(startDate);
            OffsetDateTime resolvedEnd = resolveEndDate(endDate);

            Long reserva = reservaRepository.countActiveReservationsDuringPeriod(resolvedStart, resolvedEnd);
            Long rapida = reservaRapidaRepository.countActiveReservationsDuringPeriod(resolvedStart, resolvedEnd);

            int reservaCount = toInt(reserva);
            int rapidaCount = toInt(rapida);
            int total = reservaCount + rapidaCount;

            return new ActiveDuringPeriodStatsDTO(total, reservaCount, rapidaCount);
        } catch (Exception e) {
            log.error("Erro em getActiveDuringPeriodStats: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return new ActiveDuringPeriodStatsDTO(0, 0, 0);
        }
    }

    private LengthOccupancyStatsDTO getLengthOccupancyStats(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            OffsetDateTime resolvedStart = resolveStartDate(startDate);
            OffsetDateTime resolvedEnd = resolveEndDate(endDate);

            long periodMinutes = ChronoUnit.MINUTES.between(resolvedStart.toInstant(), resolvedEnd.toInstant());
            if (periodMinutes <= 0) {
                log.info("getLengthOccupancyStats: período inválido ({}min), retornando valores padrão", periodMinutes);
                return new LengthOccupancyStatsDTO(0.0, 0.0, 0.0);
            }

            Long availableMetersLong = vagaRepository.sumTotalAvailableLengthMeters();
            double availableMeters = availableMetersLong != null ? availableMetersLong.doubleValue() : 0.0;
            if (availableMeters <= 0.0) {
                return new LengthOccupancyStatsDTO(0.0, 0.0, 0.0);
            }

            BigDecimal occReserva = reservaRepository.sumOccupiedLengthMinutesActiveDuringPeriod(resolvedStart, resolvedEnd);
            BigDecimal occRapida = reservaRapidaRepository.sumOccupiedLengthMinutesActiveDuringPeriod(resolvedStart, resolvedEnd);
            BigDecimal totalOccLengthMinutes = (occReserva != null ? occReserva : BigDecimal.ZERO)
                .add(occRapida != null ? occRapida : BigDecimal.ZERO);

            BigDecimal avgOccupiedMeters = totalOccLengthMinutes
                .divide(BigDecimal.valueOf(periodMinutes), 6, RoundingMode.HALF_UP);

            BigDecimal ratePercent = avgOccupiedMeters
                .divide(BigDecimal.valueOf(availableMeters), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

            return new LengthOccupancyStatsDTO(
                availableMeters,
                avgOccupiedMeters.doubleValue(),
                ratePercent.doubleValue()
            );
        } catch (Exception e) {
            log.error("Erro em getLengthOccupancyStats: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return new LengthOccupancyStatsDTO(0.0, 0.0, 0.0);
        }
    }

    private List<VehicleRouteReportDTO> getVehicleRoutes(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            OffsetDateTime resolvedStart = resolveStartDate(startDate);
            OffsetDateTime resolvedEnd = resolveEndDate(endDate);

            List<VehicleRouteEventProjection> events = reservaRepository.getVehicleRouteEventsDuringPeriod(resolvedStart, resolvedEnd);
            if (events == null || events.isEmpty()) {
                return List.of();
            }

            Map<String, List<VehicleRouteStopDTO>> byPlate = new LinkedHashMap<>();
            for (VehicleRouteEventProjection e : events) {
                if (e == null || e.getPlaca() == null) {
                    continue;
                }
                byPlate.computeIfAbsent(e.getPlaca(), k -> new java.util.ArrayList<>())
                    .add(new VehicleRouteStopDTO(
                        e.getSource(),
                        e.getInicio() != null ? e.getInicio().atZone(ZONE_ID).toOffsetDateTime() : null,
                        e.getFim() != null ? e.getFim().atZone(ZONE_ID).toOffsetDateTime() : null,
                        e.getCidadeOrigem(),
                        e.getEntradaCidade(),
                        e.getVagaId(),
                        e.getVagaLabel()
                    ));
            }

            return byPlate.entrySet().stream()
                .map(entry -> new VehicleRouteReportDTO(entry.getKey(), resolvedStart, resolvedEnd, entry.getValue()))
                .toList();
        } catch (Exception e) {
            log.error("Erro em getVehicleRoutes: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return List.of();
        }
    }

    private OffsetDateTime resolveStartDate(OffsetDateTime startDate) {
        if (startDate != null) {
            return startDate.toLocalDate().atStartOfDay(ZONE_ID).toOffsetDateTime();
        }
        return LocalDate.now(ZONE_ID).atStartOfDay(ZONE_ID).toOffsetDateTime();
    }

    private OffsetDateTime resolveEndDate(OffsetDateTime endDate) {
        if (endDate != null) {
            return endDate.toLocalDate().atTime(23, 59, 59).atZone(ZONE_ID).toOffsetDateTime();
        }
        return LocalDate.now(ZONE_ID).atTime(23, 59, 59).atZone(ZONE_ID).toOffsetDateTime();
    }

    @Cacheable(value = "dashboard-kpi", key = "#startDate?.toString() + '-' + #endDate?.toString()", unless = "#result == null")
    public DashboardKpiDTO getKpis(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            OffsetDateTime resolvedStart = resolveStartDate(startDate);
            OffsetDateTime resolvedEnd = resolveEndDate(endDate);

            Long totalSlots = reservaRepository.countTotalSlots();
            long activeReservations = safeSum(
                reservaRepository.countActiveReservations(resolvedStart, resolvedEnd),
                reservaRapidaRepository.countActiveReservations(resolvedStart, resolvedEnd));
            Long pendingReservations = reservaRepository.countPendingReservations(resolvedStart, resolvedEnd);
            long completedReservations = safeSum(
                reservaRepository.countCompletedReservations(resolvedStart, resolvedEnd),
                reservaRapidaRepository.countCompletedReservations(resolvedStart, resolvedEnd));
            Long canceledReservations = reservaRepository.countCanceledReservations(resolvedStart, resolvedEnd);
            long removedReservations = safeSum(
                reservaRepository.countRemovedReservations(resolvedStart, resolvedEnd),
                reservaRapidaRepository.countRemovedReservations(resolvedStart, resolvedEnd));
            long totalReservations = safeSum(
                reservaRepository.countTotalReservationsInPeriod(resolvedStart, resolvedEnd),
                reservaRapidaRepository.countTotalReservationsInPeriod(resolvedStart, resolvedEnd));
            Long multipleSlotReservations = reservaRepository.countMultipleSlotReservations(resolvedStart, resolvedEnd);

            Double occupancyRate = totalSlots != null && totalSlots > 0
                ? (double) activeReservations / totalSlots * 100
                : 0.0;

            return new DashboardKpiDTO(
                toInt(totalSlots),
                toInt(activeReservations),
                toInt(pendingReservations),
                occupancyRate,
                toInt(completedReservations),
                toInt(canceledReservations),
                toInt(removedReservations),
                toInt(totalReservations),
                toInt(multipleSlotReservations),
                resolvedStart,
                resolvedEnd
            );
        } catch (Exception e) {
            log.error("Erro em getKpis: {} - {}", e.getClass().getName(), e.getMessage(), e);
            throw e;
        }
    }

    @Cacheable(value = "dashboard-vehicle-types", key = "#startDate?.toString() + '-' + #endDate?.toString()", unless = "#result == null || #result.isEmpty()")
    public List<VehicleTypeStatDTO> getVehicleTypeStats(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            OffsetDateTime resolvedStart = resolveStartDate(startDate);
            OffsetDateTime resolvedEnd = resolveEndDate(endDate);

            List<Map<String, Object>> results = reservaRepository.getVehicleTypeStats(resolvedStart, resolvedEnd);
            if (results == null) return List.of();

            return results.stream()
                .map(row -> new VehicleTypeStatDTO(
                    (String) row.get("tipo"),
                    toInt((Number) row.get("count")),
                    toInt((Number) row.get("uniqueVehicles"))
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erro em getVehicleTypeStats: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return List.of();
        }
    }

    @Cacheable(value = "dashboard-districts", key = "#startDate?.toString() + '-' + #endDate?.toString()", unless = "#result == null || #result.isEmpty()")
    public List<LocationStatDTO> getDistrictStats(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            OffsetDateTime resolvedStart = resolveStartDate(startDate);
            OffsetDateTime resolvedEnd = resolveEndDate(endDate);

            List<Map<String, Object>> results = reservaRepository.getDistrictStats(resolvedStart, resolvedEnd);
            if (results == null) return List.of();

            return results.stream()
                .map(row -> new LocationStatDTO(
                    (String) row.get("name"),
                    "district",
                    toInt((Number) row.get("count"))
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erro em getDistrictStats: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return List.of();
        }
    }

    @Cacheable(value = "dashboard-origins", key = "#startDate?.toString() + '-' + #endDate?.toString()", unless = "#result == null || #result.isEmpty()")
    public List<LocationStatDTO> getOriginStats(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            OffsetDateTime resolvedStart = resolveStartDate(startDate);
            OffsetDateTime resolvedEnd = resolveEndDate(endDate);

            List<Map<String, Object>> results = reservaRepository.getOriginStats(resolvedStart, resolvedEnd);
            if (results == null) return List.of();

            return results.stream()
                .map(row -> new LocationStatDTO(
                    (String) row.get("name"),
                    "origin",
                    toInt((Number) row.get("count"))
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erro em getOriginStats: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return List.of();
        }
    }

    @Cacheable(value = "dashboard-entry-origins", key = "#startDate?.toString() + '-' + #endDate?.toString()", unless = "#result == null || #result.isEmpty()")
    public List<LocationStatDTO> getEntryOriginStats(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            OffsetDateTime resolvedStart = resolveStartDate(startDate);
            OffsetDateTime resolvedEnd = resolveEndDate(endDate);

            List<Map<String, Object>> results = reservaRepository.getEntryOriginStats(resolvedStart, resolvedEnd);
            if (results == null) return List.of();

            return results.stream()
                .map(row -> new LocationStatDTO(
                    (String) row.get("name"),
                    "entry-origin",
                    toInt((Number) row.get("count"))
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Erro em getEntryOriginStats: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return List.of();
        }
    }
    @Cacheable(value = "dashboard-most-used", key = "#startDate?.toString() + '-' + #endDate?.toString()", unless = "#result == null || #result.isEmpty()")
    public List<LocationStatDTO> getMostUsedVagas(OffsetDateTime startDate, OffsetDateTime endDate) {
        try {
            OffsetDateTime resolvedStart = resolveStartDate(startDate);
            OffsetDateTime resolvedEnd = resolveEndDate(endDate);

            List<Object[]> results = reservaRepository.getMostUsedVagas(resolvedStart, resolvedEnd);

            if (results == null || results.isEmpty()) {
                return List.of();
            }

            return results.stream()
                .map(r -> new LocationStatDTO(
                    (String) r[0],
                    "most-used",
                    toInt((Number) r[1])
                ))
                .toList();
        } catch (Exception e) {
            log.error("Erro em getMostUsedVagas: {} - {}", e.getClass().getName(), e.getMessage(), e);
            return List.of();
        }
    }

    @Cacheable(value = "dashboard-summary", key = "#startDate?.toString() + '-' + #endDate?.toString()", unless = "#result == null")
    public DashboardSummaryDTO getSummary(OffsetDateTime startDate, OffsetDateTime endDate) {
        log.info("=== INÍCIO getSummary ===");
        log.info("Parâmetros: startDate={}, endDate={}", startDate, endDate);
        try {
            log.info("Etapa 1/10: Buscando KPIs...");
            DashboardKpiDTO kpis = getKpis(startDate, endDate);
            log.info("✓ KPIs obtido: {}", kpis);

            log.info("Etapa 2/10: Buscando VehicleTypeStats...");
            List<VehicleTypeStatDTO> vehicleTypes = getVehicleTypeStats(startDate, endDate);
            log.info("✓ VehicleTypes: {} items", vehicleTypes != null ? vehicleTypes.size() : "null");

            log.info("Etapa 3/10: Buscando DistrictStats...");
            List<LocationStatDTO> districts = getDistrictStats(startDate, endDate);
            log.info("✓ Districts: {} items", districts != null ? districts.size() : "null");

            log.info("Etapa 4/10: Buscando OriginStats...");
            List<LocationStatDTO> origins = getOriginStats(startDate, endDate);
            log.info("✓ Origins: {} items", origins != null ? origins.size() : "null");

            log.info("Etapa 5/10: Buscando EntryOriginStats...");
            List<LocationStatDTO> entryOrigins = getEntryOriginStats(startDate, endDate);
            log.info("✓ EntryOrigins: {} items", entryOrigins != null ? entryOrigins.size() : "null");

            log.info("Etapa 6/10: Buscando MostUsedVagas...");
            List<LocationStatDTO> mostUsedVagas = getMostUsedVagas(startDate, endDate);
            log.info("✓ MostUsedVagas: {} items", mostUsedVagas != null ? mostUsedVagas.size() : "null");

            log.info("Etapa 7/10: Buscando StayDurationStats...");
            StayDurationStatsDTO stayDurationStats = getStayDurationStats(startDate, endDate);
            log.info("✓ StayDurationStats: {}", stayDurationStats);

            log.info("Etapa 8/10: Buscando ActiveDuringPeriodStats...");
            ActiveDuringPeriodStatsDTO activeDuringPeriodStats = getActiveDuringPeriodStats(startDate, endDate);
            log.info("✓ ActiveDuringPeriodStats: {}", activeDuringPeriodStats);

            log.info("Etapa 9/10: Buscando LengthOccupancyStats...");
            LengthOccupancyStatsDTO lengthOccupancyStats = getLengthOccupancyStats(startDate, endDate);
            log.info("✓ LengthOccupancyStats: {}", lengthOccupancyStats);

            log.info("Etapa 10/10: Buscando VehicleRoutes...");
            List<VehicleRouteReportDTO> vehicleRoutes = getVehicleRoutes(startDate, endDate);
            log.info("✓ VehicleRoutes: {} items", vehicleRoutes != null ? vehicleRoutes.size() : "null");

            log.info("Construindo DashboardSummaryDTO...");
            DashboardSummaryDTO result = new DashboardSummaryDTO(
                kpis, vehicleTypes, districts, origins, entryOrigins,
                mostUsedVagas, stayDurationStats, activeDuringPeriodStats,
                lengthOccupancyStats, vehicleRoutes
            );
            log.info("✓ DashboardSummaryDTO construído com sucesso");

            log.info("=== FIM getSummary (SUCESSO) ===");
            return result;

        } catch (Exception e) {
            log.error("=== ERRO FATAL em getSummary ===");
            log.error("Tipo da exceção: {}", e.getClass().getName());
            log.error("Mensagem: {}", e.getMessage());
            log.error("Stack trace completo:", e);
            throw e;
        }
    }

    /** Soma segura de dois Long nullable — evita NPE no auto-unboxing. */
    private static long safeSum(Long a, Long b) {
        return (a != null ? a : 0L) + (b != null ? b : 0L);
    }

    private static int toInt(Long value) {
        return value == null ? 0 : value.intValue();
    }

    private static int toInt(long value) {
        return (int) value;
    }

    private static int toInt(Number value) {
        return value == null ? 0 : value.intValue();
    }
}