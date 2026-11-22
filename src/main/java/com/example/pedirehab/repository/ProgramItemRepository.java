package com.example.pedirehab.repository;

import com.example.pedirehab.entity.ProgramItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramItemRepository extends JpaRepository<ProgramItem, Long> {
    List<ProgramItem> findByProgramIdOrderByDisplayOrderAsc(Long programId);
}
