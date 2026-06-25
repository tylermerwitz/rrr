package com.example.rrr.dto;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_unlocked_equipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerUnlockedEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private PlayerMeta player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    private LocalDateTime unlockedAt;

    public PlayerUnlockedEquipment(PlayerMeta player, Equipment equipment) {
        this.player = player;
        this.equipment = equipment;
        this.unlockedAt = LocalDateTime.now();
    }
}
