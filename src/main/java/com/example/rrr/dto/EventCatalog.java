package com.example.rrr.dto;

import com.example.rrr.model.GameEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Root shape of {@code events_floor_1.json}: the full list of authored events. */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventCatalog {

    private List<GameEvent> events;
}
