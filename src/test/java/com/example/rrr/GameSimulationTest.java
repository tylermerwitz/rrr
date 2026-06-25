package com.example.rrr;

import com.example.rrr.controller.GameController;
import com.example.rrr.repository.PlayerMetaRepository;
import com.example.rrr.repository.PlayerRunRepository;
import com.example.rrr.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class GameSimulationTest {

    @Autowired
    private GameSimulationRunner runner;

    @MockBean
    private LlmClient llmClient; // Satisfies the LlmNarrationService dependency

    @Test
    void runSimulation() {
        runner.runSimulation();
    }
}