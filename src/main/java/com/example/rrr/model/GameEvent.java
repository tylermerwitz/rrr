package com.example.rrr.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * A hand-authored encounter, loaded from {@code events_floor_1.json}. An event is a {@link #scene} (the
 * narration shown when it's presented) plus its {@link #choices} — each a risk tier with its own
 * authored success/failure branches AND its own check {@link EventChoice#getCategory() category},
 * so different choices test different aptitudes. There is no per-floor scaling or runtime mutation:
 * the loaded definition is used directly, so the same instance can be shared across requests.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameEvent {

    private String id;
    private RoomType roomType;

    /**
     * Optional gating: conditions that must hold for this event to be eligible for the random floor
     * draw. Null (the common case) means the event is always eligible.
     */
    private EventRequirements requirements;

    /** The narration shown when this event is presented as the next beat. */
    private String scene;

    private List<EventChoice> choices;

    /** The authored choice for a given risk approach, or {@code null} if this event has none. */
    public EventChoice choiceFor(ActionType action) {
        if (choices == null) {
            return null;
        }
        for (EventChoice choice : choices) {
            if (choice.getRisk() == action) {
                return choice;
            }
        }
        return null;
    }
}
