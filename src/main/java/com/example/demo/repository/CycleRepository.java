package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Cycle;

@Repository
public interface CycleRepository extends JpaRepository<Cycle, Long> {
    
    List<Cycle> findByUserIdOrderByStartDateDesc(Long userId);

    List<Cycle> findByUserIdAndEndDateIsNotNullOrderByStartDateDesc(Long userId);

    Optional<Cycle> findFirstByUserIdAndEndDateIsNullOrderByStartDateDesc(Long userId);

    Optional<Cycle> findFirstByUserIdAndEndDateIsNotNullOrderByStartDateDesc(Long userId);
}
