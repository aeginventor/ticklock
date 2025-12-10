package ticklock.controller.dto

import ticklock.domain.Event

data class EventResponse(
    val id: Long,
    val name: String,
    val totalSeats: Int,
    val remainingSeats: Int
) {
    companion object {
        @JvmStatic
        fun from(event: Event): EventResponse = EventResponse(
            id = event.id,
            name = event.name,
            totalSeats = event.totalSeats,
            remainingSeats = event.remainingSeats
        )
    }
}