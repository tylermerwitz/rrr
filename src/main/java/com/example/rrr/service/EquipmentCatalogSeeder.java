package com.example.rrr.service;

import com.example.rrr.dto.Equipment;
import com.example.rrr.model.EquipmentType;
import com.example.rrr.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the small pool of reward items that event "New Item" consequences can grant. Authored
 * branches reference these by name (see {@code events.json} {@code itemReward}). Idempotent: each
 * item is only created if its name isn't already in the catalog, so it's safe across restarts.
 *
 * <p>These stats/names/themes are a starting set meant to be tuned — edit freely.
 */
@Component
@RequiredArgsConstructor
public class EquipmentCatalogSeeder implements CommandLineRunner {

    private final EquipmentRepository equipmentRepository;

    @Override
    public void run(String... args) {
        //      name                    type                  def spd  bladderMod bowelMod cost  rTier aTier legendary
        seed("Reinforced Diaper",   EquipmentType.DIAPER,      3, -1,   10.0,      5.0,     40,   0,    0,    false);
        seed("Padded Trousers",     EquipmentType.OUTFIT,      2,  0,    0.0,      0.0,     30,   0,    0,    false);
        seed("Pacifier Charm",      EquipmentType.ACCESSORY,   0,  1,    5.0,      5.0,     25,   0,    0,    false);
        seed("Locking Plastic Pants", EquipmentType.LOCKING,   1, -1,   15.0,     15.0,     60,   1,    0,    false);
        seed("Crinkleweave Cloak",  EquipmentType.LEGENDARY,   6,  2,    0.0,      0.0,    120,   2,    1,    true);
    }

    private void seed(
            String name,
            EquipmentType type,
            int flatDefense,
            int speedModifier,
            double bladderModifier,
            double bowelModifier,
            int unlockCost,
            int requiredRegressionTier,
            int requiredArousalTier,
            boolean legendary
    ) {
        if (equipmentRepository.findByName(name).isPresent()) {
            return;
        }
        equipmentRepository.save(new Equipment(
                name, type, flatDefense, speedModifier, bladderModifier, bowelModifier,
                unlockCost, requiredRegressionTier, requiredArousalTier, legendary
        ));
    }
}
