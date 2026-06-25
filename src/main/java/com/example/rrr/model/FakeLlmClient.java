package com.example.rrr.model;

import com.example.rrr.service.LlmClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class FakeLlmClient implements LlmClient
{

    @Override
    public String complete(String prompt) {
        return """
        {
          "narration": "You cautiously move forward.",
          "choices": [
            {"id":"A","text":"Proceed","riskLevel":"SAFE"},
            {"id":"B","text":"Investigate","riskLevel":"MODERATE"},
            {"id":"C","text":"Charge ahead","riskLevel":"RISKY"}
          ]
        }
        """;
    }
}
