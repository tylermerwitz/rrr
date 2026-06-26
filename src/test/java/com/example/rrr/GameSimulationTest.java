package com.example.rrr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GameSimulationTest {

    @Autowired
    private GameSimulationRunner runner;

    @Test
    void runSimulation() {
        runner.runSimulation();
    }
}
