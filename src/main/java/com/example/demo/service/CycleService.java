package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.dto.request.CycleRequest;
import com.example.demo.dto.request.UpdateCycleRequest;
import com.example.demo.dto.response.CycleStatsResponse;
import com.example.demo.entity.Cycle;
import com.example.demo.enums.Role;

public interface CycleService {

    // ===== CRUD =====
    Cycle createCycle(Long userId, CycleRequest request);
    Cycle updateCycle(Long cycleId, UpdateCycleRequest request, Long requesterId, Role requesterRole);
    Cycle closeCycle(Long cycleId, LocalDate endDate, Long requesterId, Role requesterRole);
    void deleteCycle(Long cycleId, Long requesterId, Role requesterRole);

    // ===== Consultas =====
    List<Cycle> getCyclesByUser(Long userId, Long requesterId, Role requesterRole);
    Cycle getCycleById(Long cycleId, Long requesterId, Role requesterRole);

    // ===== Estadísticas =====
    CycleStatsResponse getStats(Long userId, Long requesterId, Role requesterRole);
}