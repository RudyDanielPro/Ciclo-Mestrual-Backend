package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.request.CycleRequest;
import com.example.demo.dto.response.CycleResponse;
import com.example.demo.dto.response.CycleStatsResponse;
import com.example.demo.entity.Cycle;
import com.example.demo.entity.User;
import com.example.demo.service.CycleService;
import com.example.demo.service.UserService;
import com.example.demo.util.CycleCalculator;
import com.example.demo.util.SecurityUtils;

@RestController
@RequestMapping("/api/admin/cycles")
public class AdminCycleController {

    private final CycleService cycleService;
    private final UserService userService;

    public AdminCycleController(CycleService cycleService, UserService userService) {
        this.cycleService = cycleService;
        this.userService = userService;
    }

    // ==================== Conteo de ciclos por usuario ====================

    @GetMapping("/counts")
    public ResponseEntity<Map<Long, Long>> getCountsByUser() {
        return ResponseEntity.ok(cycleService.getCycleCountByUser());
    }

    // ==================== Listar ciclos de un usuario ====================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CycleResponse>> getUserCycles(@PathVariable Long userId) {
        User admin = getCurrentUser();
        List<CycleResponse> cycles = cycleService
                .getCyclesByUser(userId, admin.getId(), admin.getRole())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(cycles);
    }

    // ==================== Estadísticas de un usuario ====================

    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<CycleStatsResponse> getUserStats(@PathVariable Long userId) {
        User admin = getCurrentUser();
        CycleStatsResponse stats = cycleService.getStats(userId, admin.getId(), admin.getRole());
        return ResponseEntity.ok(stats);
    }

    // ==================== Crear ciclo para un usuario ====================

    @PostMapping("/user/{userId}")
    public ResponseEntity<CycleResponse> createCycleForUser(@PathVariable Long userId,
                                                            @RequestBody CycleRequest request) {
        Cycle cycle = cycleService.createCycle(userId, request);
        return new ResponseEntity<>(toResponse(cycle), HttpStatus.CREATED);
    }

    // ==================== Eliminar ciclo de cualquier usuario ====================

    @DeleteMapping("/{cycleId}")
    public ResponseEntity<String> deleteAnyCycle(@PathVariable Long cycleId) {
        User admin = getCurrentUser();
        cycleService.deleteCycle(cycleId, admin.getId(), admin.getRole());
        return ResponseEntity.ok("Ciclo eliminado correctamente");
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