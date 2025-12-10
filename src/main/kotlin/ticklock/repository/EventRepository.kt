package ticklock.repository

import jakarta.persistence.LockModeType
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.query.Param
import ticklock.entity.EventEntity
import java.util.Optional

interface EventRepository : JpaRepository<EventEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT e FROM EventEntity e WHERE e.id = :id")
    fun findByIdWithPessimisticLock(@Param("id") id: Long): Optional<EventEntity>

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT e FROM EventEntity e WHERE e.id = :id")
    fun findByIdWithOptimisticLock(@Param("id") id: Long): Optional<EventEntity>
}