package com.example.rrr.repository;

import com.example.rrr.dto.PlayerRun;
import com.example.rrr.dto.PlayerRunEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRunEquipmentRepository
        extends JpaRepository<PlayerRunEquipment, Long> {
    List<PlayerRunEquipment> findByRun(PlayerRun run);
}
