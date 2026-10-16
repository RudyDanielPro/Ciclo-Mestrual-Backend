package com.example.demo.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.dto.request.CycleRequest;
import com.example.demo.dto.request.UpdateCycleRequest;
import com.example.demo.dto.response.CycleStatsResponse;
import com.example.demo.entity.Cycle;
import com.example.demo.entity.User;
import com.example.demo.enums.Role;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.CycleRepository;
import com.example.demo.service.CycleService;
import com.example.demo.service.UserService;
import com.example.demo.util.CycleCalculator;

@Service
public class CycleServiceImpl implements CycleService {

    private final CycleRepository cycleRepository;
    private final UserService userService;

    public CycleServiceImpl(CycleRepository cycleRepository, UserService userService) {
        this.cycleRepository = cycleRepository;
        this.userService = userService;
    }

    // ==================== Crear ====================

    @Override
    public Cycle createCycle(Long userId, CycleRequest request) {
        User user = userService.findById(userId);

        // Validar fechas
        validarFechas(request.getStartDate(), request.getEndDate());

        // Validar que no tenga ya un ciclo activo
        if (request.getEndDate() == null) {
            cycleRepository.findFirstByUserIdAndEndDateIsNullOrderByStartDateDesc(userId)
                    .ifPresent(c -> {
                        throw new BadRequestException("El usuario ya tiene un ciclo activo. Ciérralo antes de crear uno nuevo.");
                    });
        }

        Cycle cycle = new Cycle();
        cycle.setUser(user);
        cycle.setStartDate(request.getStartDate());
        cycle.setEndDate(request.getEndDate());

        return cycleRepository.save(cycle);
    }

    // ==================== Actualizar ====================

    @Override
    public Cycle updateCycle(Long cycleId, UpdateCycleRequest request, Long requesterId, Role requesterRole) {
        Cycle cycle = getCycleById(cycleId, requesterId, requesterRole);

        validarFechas(request.getStartDate(), request.getEndDate());

        cycle.setStartDate(request.getStartDate());
        cycle.setEndDate(request.getEndDate());

        return cycleRepository.save(cycle);
    }

    // ==================== Cerrar ciclo ====================

    @Override
    public Cycle closeCycle(Long cycleId, LocalDate endDate, Long requesterId, Role requesterRole) {
        Cycle cycle = getCycleById(cycleId, requesterId, requesterRole);

        if (cycle.getEndDate() != null) {
            throw new BadRequestException("Este ciclo ya está cerrado");
        }

        validarFechas(cycle.getStartDate(), endDate);

        cycle.setEndDate(endDate);
        return cycleRepository.save(cycle);
    }

    // ==================== Eliminar ====================

    @Override
    public void deleteCycle(Long cycleId, Long requesterId, Role requesterRole) {
        Cycle cycle = getCycleById(cycleId, requesterId, requesterRole);
        cycleRepository.delete(cycle);
    }

    // ==================== Consultas ====================

    @Override
    public List<Cycle> getCyclesByUser(Long userId, Long requesterId, Role requesterRole) {
        // Solo el dueño o el admin
        if (!userId.equals(requesterId) && requesterRole != Role.ADMIN) {
            throw new UnauthorizedException("No puedes ver los ciclos de otro usuario");
        }
        return cycleRepository.findByUserIdOrderByStartDateDesc(userId);
    }

    @Override
    public Cycle getCycleById(Long cycleId, Long requesterId, Role requesterRole) {
        Cycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new ResourceNotFoundException("Ciclo no encontrado con id: " + cycleId));

        // Solo el dueño o el admin
        if (!cycle.getUser().getId().equals(requesterId) && requesterRole != Role.ADMIN) {
            throw new UnauthorizedException("No tienes permiso para acceder a este ciclo");
        }

        return cycle;
    }

    // ==================== Conteo por usuario (admin) ====================

    @Override
    public Map<Long, Long> getCycleCountByUser() {
        Map<Long, Long> result = new HashMap<>();
        for (Object[] row : cycleRepository.countCyclesByUser()) {
            Long userId = ((Number) row[0]).longValue();
            Long count = ((Number) row[1]).longValue();
            result.put(userId, count);
        }
        return result;
    }

    // ==================== Estadísticas ====================

    @Override
    public CycleStatsResponse getStats(Long userId, Long requesterId, Role requesterRole) {
        // Validar permiso
        if (!userId.equals(requesterId) && requesterRole != Role.ADMIN) {
            throw new UnauthorizedException("No puedes ver las estadísticas de otro usuario");
        }

        // Traer todos los ciclos del usuario, ordenados por inicio descendente
        List<Cycle> allCycles = cycleRepository.findByUserIdOrderByStartDateDesc(userId);

        CycleStatsResponse stats = new CycleStatsResponse();

        // Si no hay ciclos, devolver todo en null
        if (allCycles.isEmpty()) {
            return stats;
        }

        // ===== Último ciclo =====
        Cycle ultimo = allCycles.get(0);
        stats.setLastCycleStart(ultimo.getStartDate());

        // ===== Buscar el último ciclo cerrado (para lastCycleEnd) =====
        Cycle ultimoCerrado = null;
        for (Cycle c : allCycles) {
            if (c.getEndDate() != null) {
                ultimoCerrado = c;
                break;
            }
        }

        if (ultimoCerrado != null) {
            stats.setLastCycleEnd(ultimoCerrado.getEndDate());
            stats.setLastCycleDuration(
                CycleCalculator.calcularDuracionCiclo(ultimoCerrado.getStartDate(), ultimoCerrado.getEndDate())
            );
        }

        // ===== Recopilar intervalos entre inicios =====
        List<Cycle> ordenadosAsc = new ArrayList<>(allCycles);
        ordenadosAsc.sort((a, b) -> a.getStartDate().compareTo(b.getStartDate()));

        List<Long> intervalos = new ArrayList<>();
        for (int i = 1; i < ordenadosAsc.size(); i++) {
            long diff = CycleCalculator.diasEntre(
                    ordenadosAsc.get(i - 1).getStartDate(),
                    ordenadosAsc.get(i).getStartDate()
            );
            intervalos.add(diff);
        }

        // ===== Promedio de intervalo =====
        double promedioIntervalo = 0;
        double desviacionIntervalo = 0;
        if (!intervalos.isEmpty()) {
            promedioIntervalo = CycleCalculator.calcularPromedio(intervalos);
            desviacionIntervalo = CycleCalculator.calcularDesviacion(intervalos, promedioIntervalo);
            stats.setAverageCycleDuration(CycleCalculator.redondear(promedioIntervalo));
        }

        // ===== Recopilar duraciones de sangrado =====
        List<Long> duracionesSangrado = new ArrayList<>();
        for (Cycle c : allCycles) {
            if (c.getEndDate() != null) {
                long dur = CycleCalculator.calcularDuracionCiclo(c.getStartDate(), c.getEndDate());
                duracionesSangrado.add(dur);
            }
        }

        double promedioSangrado = 0;
        double desviacionSangrado = 0;
        if (!duracionesSangrado.isEmpty()) {
            promedioSangrado = CycleCalculator.calcularPromedio(duracionesSangrado);
            desviacionSangrado = CycleCalculator.calcularDesviacion(duracionesSangrado, promedioSangrado);
            stats.setAveragePeriodDuration(CycleCalculator.redondear(promedioSangrado));
        }

        // ===== Predicción del próximo inicio =====
        if (!intervalos.isEmpty()) {
            LocalDate ultimoInicio = ordenadosAsc.get(ordenadosAsc.size() - 1).getStartDate();

            double desvMin = CycleCalculator.desviacionConMinimo(desviacionIntervalo);

            LocalDate inicioMasProbable = ultimoInicio.plusDays((long) Math.round(promedioIntervalo));
            LocalDate rangoInicioDesde = ultimoInicio.plusDays((long) Math.round(promedioIntervalo - desvMin));
            LocalDate rangoInicioHasta = ultimoInicio.plusDays((long) Math.round(promedioIntervalo + desvMin));

            stats.setMostLikelyNextStartDate(inicioMasProbable);
            stats.setPredictedNextStartRangeStart(rangoInicioDesde);
            stats.setPredictedNextStartRangeEnd(rangoInicioHasta);

            // ===== Predicción del próximo fin =====
            if (!duracionesSangrado.isEmpty()) {
                double desvSangradoMin = CycleCalculator.desviacionConMinimo(desviacionSangrado);

                LocalDate finMasProbable = inicioMasProbable.plusDays((long) Math.round(promedioSangrado) - 1);
                LocalDate rangoFinDesde = rangoInicioDesde.plusDays((long) Math.round(promedioSangrado - desvSangradoMin) - 1);
                LocalDate rangoFinHasta = rangoInicioHasta.plusDays((long) Math.round(promedioSangrado + desvSangradoMin) - 1);

                stats.setMostLikelyNextEndDate(finMasProbable);
                stats.setPredictedNextEndRangeStart(rangoFinDesde);
                stats.setPredictedNextEndRangeEnd(rangoFinHasta);
            }
        }

        return stats;
    }

    // ==================== Helpers privados ====================

    private void validarFechas(LocalDate inicio, LocalDate fin) {
        if (inicio == null) {
            throw new BadRequestException("La fecha de inicio es obligatoria");
        }
        if (fin != null && fin.isBefore(inicio)) {
            throw new BadRequestException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }
}