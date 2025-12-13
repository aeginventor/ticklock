package ticklock.controller.dto

data class EventResponse(
    val id: Long,
    val name: String,
    val totalSeats: Int,
    val remainingSeats: Int
)
