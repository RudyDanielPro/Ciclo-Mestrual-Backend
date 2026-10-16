package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Cycle;

@Repository
public interface CycleRepository extends JpaRepository<Cycle, Long> {

    List<Cycle> findByUserIdOrderByStartDateDesc(Long userId);

    List<Cycle> findByUserIdAndEndDateIsNotNullOrderByStartDateDesc(Long userId);

    Optional<Cycle> findFirstByUserIdAndEndDateIsNullOrderByStartDateDesc(Long userId);

    Optional<Cycle> findFirstByUserIdAndEndDateIsNotNullOrderByStartDateDesc(Long userId);

    // NUEVO: contar ciclos agrupados por user.id
    @Query("SELECT c.user.id, COUNT(c) FROM Cycle c GROUP BY c.user.id")
    List<Object[]> countCyclesByUser();
}