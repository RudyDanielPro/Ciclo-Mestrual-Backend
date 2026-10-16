package com.example.demo.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CycleRequest {
    private LocalDate startDate;
    private LocalDate endDate;  
}