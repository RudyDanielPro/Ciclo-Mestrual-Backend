package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.CycleRequest;
import com.example.demo.dto.request.UpdateCycleRequest;
import com.example.demo.dto.response.CycleResponse;
import com.example.demo.dto.response.CycleStatsResponse;
import com.example.demo.entity.Cycle;
import com.example.demo.entity.User;
import com.example.demo.service.CycleService;
import com.example.demo.service.UserService;
import com.example.demo.util.CycleCalculator;
import com.example.demo.util.SecurityUtils;

@RestController
@RequestMapping("/api/cycles")
public class CycleController {

    private final CycleService cycleService;
    private final UserService userService;

    public CycleController(CycleService cycleService, UserService userService) {
        this.cycleService = cycleService;
        this.userService = userService;
    }

    // ==================== Crear ====================

    @PostMapping
    public ResponseEntity<CycleResponse> createCycle(@RequestBody CycleRequest request) {
        User user = getCurrentUser();
        Cycle cycle = cycleService.createCycle(user.getId(), request);
        return new ResponseEntity<>(toResponse(cycle), HttpStatus.CREATED);
    }

    // ==================== Listar propios ====================

    @GetMapping
    public ResponseEntity<List<CycleResponse>> getMyCycles() {
        User user = getCurrentUser();
        List<CycleResponse> cycles = cycleService
                .getCyclesByUser(user.getId(), user.getId(), user.getRole())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(cycles);
    }

    // ==================== Obtener uno ====================

    @GetMapping("/{id}")
    public ResponseEntity<CycleResponse> getCycleById(@PathVariable Long id) {
        User user = getCurrentUser();
        Cycle cycle = cycleService.getCycleById(id, user.getId(), user.getRole());
        return ResponseEntity.ok(toResponse(cycle));
    }

    // ==================== Editar ====================

    @PutMapping("/{id}")
    public ResponseEntity<CycleResponse> updateCycle(@PathVariable Long id,
                                                     @RequestBody UpdateCycleRequest request) {
        User user = getCurrentUser();
        Cycle cycle = cycleService.updateCycle(id, request, user.getId(), user.getRole());
        return ResponseEntity.ok(toResponse(cycle));
    }

    // ==================== Cerrar ====================

    @PutMapping("/{id}/close")
    public ResponseEntity<CycleResponse> closeCycle(@PathVariable Long id,
                                                    @RequestBody UpdateCycleRequest request) {
        User user = getCurrentUser();
        LocalDate endDate = request.getEndDate();
        Cycle cycle = cycleService.closeCycle(id, endDate, user.getId(), user.getRole());
        return ResponseEntity.ok(toResponse(cycle));
    }

    // ==================== Eliminar ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCycle(@PathVariable Long id) {
        User user = getCurrentUser();
        cycleService.deleteCycle(id, user.getId(), user.getRole());
        return ResponseEntity.ok("Ciclo eliminado correctamente");
    }

    // ==================== Estadísticas ====================

    @GetMapping("/stats")
    public ResponseEntity<CycleStatsResponse> getMyStats() {
        User user = getCurrentUser();
        CycleStatsResponse stats = cycleService.getStats(user.getId(), user.getId(), user.getRole());
        return ResponseEntity.ok(stats);
    }

    // ==================== Helpers ====================

    private User getCurrentUser() {
        String username = SecurityUtils.getAuthenticatedUsername();
        return userService.findByUsername(username);
    }

    private CycleResponse toResponse(Cycle cycle) {
        Long duration = null;
        if (cycle.getEndDate() != null) {
            duration = (long) CycleCalculator.calcularDuracionCiclo(cycle.getStartDate(), cycle.getEndDate());
        }
        return new CycleResponse(
                cycle.getId(),
                cycle.getStartDate(),
                cycle.getEndDate(),
                duration
        );
    }
}