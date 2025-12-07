package ticklock.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import ticklock.entity.TicketTypeEntity;

import java.util.List;
import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketTypeEntity, Long> {

    List<TicketTypeEntity> findByEventId(Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT t FROM TicketTypeEntity t WHERE t.id = :id")
    Optional<TicketTypeEntity> findByIdWithPessimisticLock(@Param("id") Long id);
}