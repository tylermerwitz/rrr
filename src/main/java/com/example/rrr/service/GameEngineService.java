package com.example.rrr.service;

import com.example.rrr.dto.PlayerMeta;
import com.example.rrr.dto.PlayerRun;
import com.example.rrr.model.EventOutcome;
import com.example.rrr.model.GameEvent;
import com.example.rrr.model.TurnResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameEngineService {

    private final EventResolverService resolver;
    private final TurnProcessor turnProcessor;

    @Transactional
    public TurnResult processAction(
            GameEvent event,
            PlayerMeta meta,
            PlayerRun run
    ) {

        EventOutcome outcome = resolver.resolve(event, meta, run);

        turnProcessor.processTurn(meta, run);

        return new TurnResult(run, meta, outcome);
    }
}
