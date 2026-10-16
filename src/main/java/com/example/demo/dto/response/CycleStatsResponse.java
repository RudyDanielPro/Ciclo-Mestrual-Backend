package com.example.demo.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleStatsResponse {

    // ===== Último ciclo =====
    private LocalDate lastCycleStart;
    private LocalDate lastCycleEnd;
    private Integer lastCycleDuration;

    // ===== Promedios =====
    private Integer averageCycleDuration;    // promedio entre inicios
    private Integer averagePeriodDuration;   // promedio de sangrado

    // ===== Predicción próximo inicio =====
    private LocalDate predictedNextStartRangeStart;
    private LocalDate predictedNextStartRangeEnd;
    private LocalDate mostLikelyNextStartDate;

    // ===== Predicción próximo fin =====
    private LocalDate predictedNextEndRangeStart;
    private LocalDate predictedNextEndRangeEnd;
    private LocalDate mostLikelyNextEndDate;
}