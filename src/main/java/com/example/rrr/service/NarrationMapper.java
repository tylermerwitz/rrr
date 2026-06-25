package com.example.rrr.service;

import com.example.rrr.dto.NarrationRequest;
import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.model.FloorDefinition;
import com.example.rrr.model.TurnResult;
import com.example.rrr.repository.PlayerRunEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NarrationMapper {

    private final PlayerRunEquipmentRepository equipmentRepository;
    private final FloorDefinitionService floorDefinitionService;

    public NarrationRequest build(
            TurnResult result
    ) {

        PlayerRun run = result.getRunState();
        PlayerMeta meta = result.getMetaState();

        List<String> equipmentNames =
                equipmentRepository.findByRun(run)
                        .stream()
                        .map(e -> e.getEquipment().getName())
                        .toList();

        FloorDefinition floorDef =
                floorDefinitionService.getDefinition(run.getCurrentFloor());

        return NarrationRequest.builder()
                .floorName(floorDef.name())
                .floorNumber(run.getCurrentFloor())
                .humiliation(run.getHumiliation())
                .humiliationMax(2000)
                .regression(meta.getRegressionPoints())
                .arousal(meta.getArousalPoints())
                .bladderPercent(run.getBladderPercent())
                .bowelPercent(run.getBowelPercent())
                .roomType("NORMAL") // Replace when room tracking added
                .outcome(result.getOutcome())
                .visibleRoomConnections(List.of("Next Room")) // Replace with graph data
                .equippedItems(equipmentNames)
                .build();
    }
}
