package ticklock.entity

import jakarta.persistence.*

@Entity
@Table(name = "ticket_types")
class TicketTypeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String,

    val price: Int,

    val totalSeats: Int,

    var remainingSeats: Int = totalSeats,

    @Version
    val version: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    val event: EventEntity? = null
) {
    constructor(name: String, price: Int, totalSeats: Int, event: EventEntity) : this(
        id = null,
        name = name,
        price = price,
        totalSeats = totalSeats,
        remainingSeats = totalSeats,
        version = null,
        event = event
    )
    fun hasRemainingSeats(): Boolean = remainingSeats > 0

    fun decreaseSeat() {
        if (remainingSeats <= 0) {
            throw IllegalStateException("남은 좌석이 없습니다: $name")
        }
        remainingSeats--
    }
}