package ticklock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ticklock.controller.dto.EventResponse;
import ticklock.controller.dto.PurchaseResponse;
import ticklock.entity.EventEntity;
import ticklock.repository.EventRepository;
import ticklock.service.distributed.RedisLockTicketPurchaseService;
import ticklock.service.jpa.NoLockJpaTicketPurchaseService;
import ticklock.service.jpa.OptimisticLockJpaTicketPurchaseService;
import ticklock.service.jpa.PessimisticLockJpaTicketPurchaseService;

@RestController
@RequestMapping("/jpa/events")
public class JpaEventController {

    private final EventRepository eventRepository;
    private final NoLockJpaTicketPurchaseService noLockService;
    private final PessimisticLockJpaTicketPurchaseService pessimisticLockService;
    private final OptimisticLockJpaTicketPurchaseService optimisticLockService;
    private final RedisLockTicketPurchaseService redisLockService;

    public JpaEventController(
            EventRepository eventRepository,
            NoLockJpaTicketPurchaseService noLockService,
            PessimisticLockJpaTicketPurchaseService pessimisticLockService,
            OptimisticLockJpaTicketPurchaseService optimisticLockService,
            RedisLockTicketPurchaseService redisLockService) {
        this.eventRepository = eventRepository;
        this.noLockService = noLockService;
        this.pessimisticLockService = pessimisticLockService;
        this.optimisticLockService = optimisticLockService;
        this.redisLockService = redisLockService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        return eventRepository.findById(id)
                .map(event -> ResponseEntity.ok(toResponse(event)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody CreateEventRequest request) {
        EventEntity event = new EventEntity(request.name(), request.totalSeats());
        EventEntity saved = eventRepository.save(event);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PostMapping("/{id}/purchase/no-lock")
    public ResponseEntity<PurchaseResponse> purchaseNoLock(@PathVariable Long id) {
        try {
            boolean success = noLockService.purchase(id);
            return createResponse(id, success);
        } catch (Exception e) {
            return ResponseEntity.ok(PurchaseResponse.failure(e.getMessage()));
        }
    }

    @PostMapping("/{id}/purchase/pessimistic")
    public ResponseEntity<PurchaseResponse> purchasePessimistic(@PathVariable Long id) {
        try {
            boolean success = pessimisticLockService.purchase(id);
            return createResponse(id, success);
        } catch (Exception e) {
            return ResponseEntity.ok(PurchaseResponse.failure(e.getMessage()));
        }
    }

    @PostMapping("/{id}/purchase/optimistic")
    public ResponseEntity<PurchaseResponse> purchaseOptimistic(@PathVariable Long id) {
        try {
            boolean success = optimisticLockService.purchase(id);
            return createResponse(id, success);
        } catch (Exception e) {
            return ResponseEntity.ok(PurchaseResponse.failure(e.getMessage()));
        }
    }

    @PostMapping("/{id}/purchase/redis")
    public ResponseEntity<PurchaseResponse> purchaseRedisLock(@PathVariable Long id) {
        try {
            boolean success = redisLockService.purchase(id);
            return createResponse(id, success);
        } catch (Exception e) {
            return ResponseEntity.ok(PurchaseResponse.failure(e.getMessage()));
        }
    }

    private ResponseEntity<PurchaseResponse> createResponse(Long eventId, boolean success) {
        if (success) {
            int remainingSeats = eventRepository.findById(eventId)
                    .map(EventEntity::getRemainingSeats)
                    .orElse(-1);
            return ResponseEntity.ok(PurchaseResponse.success(remainingSeats));
        } else {
            return ResponseEntity.ok(PurchaseResponse.failure("매진되었습니다."));
        }
    }

    private EventResponse toResponse(EventEntity event) {
        return new EventResponse(
                event.getId(),
                event.getName(),
                event.getTotalSeats(),
                event.getRemainingSeats()
        );
    }

    public record CreateEventRequest(String name, int totalSeats) {}
}