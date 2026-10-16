package com.example.demo.dto.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleResponse {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long durationDays;   // null si endDate es null
}