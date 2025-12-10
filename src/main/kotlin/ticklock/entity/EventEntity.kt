package ticklock.entity

import jakarta.persistence.*

@Entity
@Table(name = "events")
class EventEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val name: String,

    val totalSeats: Int,

    var remainingSeats: Int = totalSeats,

    @Version
    val version: Long? = null
) {
    constructor(name: String, totalSeats: Int) : this(
        id = null,
        name = name,
        totalSeats = totalSeats,
        remainingSeats = totalSeats,
        version = null
    )
    fun hasRemainingSeats(): Boolean = remainingSeats > 0

    fun decreaseSeat() {
        if (remainingSeats <= 0) {
            throw IllegalStateException("남은 좌석이 없습니다.")
        }
        remainingSeats--
    }
}