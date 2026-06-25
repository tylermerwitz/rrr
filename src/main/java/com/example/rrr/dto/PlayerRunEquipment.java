package com.example.rrr.dto;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_run_equipment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerRunEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    private PlayerRun run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    public PlayerRunEquipment(PlayerRun run, Equipment equipment) {
        this.run = run;
        this.equipment = equipment;
    }
}