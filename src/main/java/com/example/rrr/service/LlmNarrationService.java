package com.example.rrr.service;

import com.example.rrr.dto.ChoiceOption;
import com.example.rrr.dto.NarrationRequest;
import com.example.rrr.dto.NarrationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LlmNarrationService {

    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;

    public NarrationResponse generate(NarrationRequest request) {

        String prompt = buildPrompt(request);

        try {
            String rawResponse = llmClient.complete(prompt);

            System.out.println("RAW LLM RESPONSE:");
            System.out.println(rawResponse);

            rawResponse = rawResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            NarrationResponse response =
                    objectMapper.readValue(rawResponse, NarrationResponse.class);

            if (response.getChoices() == null ||
                    response.getChoices().size() != 3) {
                return fallbackResponse();
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackResponse();
        }
    }

    private NarrationResponse fallbackResponse() {

        return new NarrationResponse(
                "Something unexpected happened...",
                List.of(
                        new ChoiceOption("SAFE", "Proceed cautiously", "SAFE")
                )
        );
    }

    private String buildPrompt(NarrationRequest request) {

        return """
You are the narrator of a dark roguelike adventure.

You MUST:
- Describe the scene in 2–3 paragraphs.
- Reflect the outcome of the event.
- NEVER modify stats.
- NEVER invent new mechanics.
- NEVER describe math calculations.
- Only narrate what is provided.
- Provide exactly 3 choices.

Return ONLY valid JSON:

{
  "narration": "...",
  "choices": [
    {"id": "A", "text": "...", "riskLevel": "SAFE"},
    {"id": "B", "text": "...", "riskLevel": "MODERATE"},
    {"id": "C", "text": "...", "riskLevel": "RISKY"}
  ]
}

Current State:
Floor: %d - %s
Humiliation: %d / %d
Regression: %d
Arousal: %d
Bladder: %.2f%%
Bowel: %.2f%%
Room Type: %s
Event Outcome:
- Success: %s
- Humiliation Added: %d
- Regression Added: %d
- Arousal Added: %d
- Accident Triggered: %s
Connected Rooms: %s
Equipped Items: %s
""".formatted(
                request.getFloorNumber(),
                request.getFloorName(),
                request.getHumiliation(),
                request.getHumiliationMax(),
                request.getRegression(),
                request.getArousal(),
                request.getBladderPercent(),
                request.getBowelPercent(),
                request.getRoomType(),
                request.getOutcome().isSuccess(),
                request.getOutcome().getHumiliationAdded(),
                request.getOutcome().getRegressionAdded(),
                request.getOutcome().getArousalAdded(),
                request.getOutcome().isTriggeredAccident(),
                request.getVisibleRoomConnections(),
                request.getEquippedItems()
        );
    }
}
