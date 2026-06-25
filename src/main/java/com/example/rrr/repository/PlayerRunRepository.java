package com.example.rrr.repository;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRunRepository extends JpaRepository<PlayerRun, Long>
{
    Optional<PlayerRun> findByPlayerAndActiveTrue(PlayerMeta player);
}
