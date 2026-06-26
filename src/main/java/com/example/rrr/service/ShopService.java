package com.example.rrr.service;

import com.example.rrr.dto.Equipment;
import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerUnlockedEquipment;
import com.example.rrr.model.ShopListing;
import com.example.rrr.repository.EquipmentRepository;
import com.example.rrr.repository.PlayerUnlockedEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final EquipmentRepository equipmentRepository;
    private final PlayerUnlockedEquipmentRepository unlockedEquipmentRepository;
    private final TierService tierService;

    public List<ShopListing> getCatalog(PlayerMeta player) {

        Set<Long> ownedIds = unlockedEquipmentRepository.findByPlayer(player)
                .stream()
                .map(unlock -> unlock.getEquipment().getId())
                .collect(Collectors.toSet());

        int regressionTier = tierService.getRegressionTier(player.getRegressionPoints());
        int arousalTier = tierService.getArousalTier(player.getArousalPoints());

        return equipmentRepository.findAll()
                .stream()
                .map(equipment -> new ShopListing(
                        equipment.getId(),
                        equipment.getName(),
                        equipment.getType(),
                        equipment.getFlatDefense(),
                        equipment.getSpeedModifier(),
                        equipment.getBladderModifier(),
                        equipment.getBowelModifier(),
                        equipment.getUnlockCost(),
                        equipment.getRequiredRegressionTier(),
                        equipment.getRequiredArousalTier(),
                        equipment.isLegendary(),
                        ownedIds.contains(equipment.getId()),
                        regressionTier >= equipment.getRequiredRegressionTier()
                                && arousalTier >= equipment.getRequiredArousalTier()
                ))
                .toList();
    }

    @Transactional
    public void purchase(PlayerMeta player, Long equipmentId) {

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown equipment: " + equipmentId));

        boolean alreadyOwned = unlockedEquipmentRepository.findByPlayer(player)
                .stream()
                .anyMatch(unlock -> unlock.getEquipment().getId().equals(equipmentId));

        if (alreadyOwned) {
            throw new IllegalStateException("Equipment already unlocked: " + equipment.getName());
        }

        int regressionTier = tierService.getRegressionTier(player.getRegressionPoints());
        int arousalTier = tierService.getArousalTier(player.getArousalPoints());

        if (regressionTier < equipment.getRequiredRegressionTier()
                || arousalTier < equipment.getRequiredArousalTier()) {
            throw new IllegalStateException(
                    "Player does not meet the tier requirements for: " + equipment.getName());
        }

        player.spendCoins(equipment.getUnlockCost());

        unlockedEquipmentRepository.save(new PlayerUnlockedEquipment(player, equipment));
    }
}
