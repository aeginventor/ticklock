package ticklock.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.query.Param
import ticklock.entity.TicketTypeEntity
import java.util.Optional

interface TicketTypeRepository : JpaRepository<TicketTypeEntity, Long> {

    fun findByEventId(eventId: Long): List<TicketTypeEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT t FROM TicketTypeEntity t WHERE t.id = :id")
    fun findByIdWithPessimisticLock(@Param("id") id: Long): Optional<TicketTypeEntity>
}