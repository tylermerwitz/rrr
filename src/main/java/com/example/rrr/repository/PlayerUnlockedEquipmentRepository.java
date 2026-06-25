package com.example.rrr.repository;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerUnlockedEquipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerUnlockedEquipmentRepository
        extends JpaRepository<PlayerUnlockedEquipment, Long> {
    List<PlayerUnlockedEquipment> findByPlayer(PlayerMeta player);
}
