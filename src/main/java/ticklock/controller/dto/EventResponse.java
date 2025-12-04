package ticklock.controller.dto;

import ticklock.domain.Event;

public record EventResponse(
        Long id,
        String name,
        int totalSeats,
        int remainingSeats
) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getTotalSeats(),
                event.getRemainingSeats()
        );
    }
}

