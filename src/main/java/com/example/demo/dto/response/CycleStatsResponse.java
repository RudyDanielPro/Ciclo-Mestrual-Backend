package com.example.demo.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleStatsResponse {
    private LocalDate lastCycleStart;
    private LocalDate lastCycleEnd;
    private Long lastCycleDuration;

    private Double averageCycleDuration;      // promedio entre inicios
    private Double averagePeriodDuration;     // promedio de días de sangrado

    private LocalDate predictedNextStartRangeStart;
    private LocalDate predictedNextStartRangeEnd;
    private LocalDate mostLikelyNextStartDate;
    private LocalDate mostLikelyNextEndDate;
}