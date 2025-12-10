package ticklock.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ticklock.controller.dto.EventResponse
import ticklock.controller.dto.PurchaseResponse
import ticklock.entity.EventEntity
import ticklock.repository.EventRepository
import ticklock.service.distributed.RedisLockTicketPurchaseService
import ticklock.service.jpa.NoLockJpaTicketPurchaseService
import ticklock.service.jpa.OptimisticLockJpaTicketPurchaseService
import ticklock.service.jpa.PessimisticLockJpaTicketPurchaseService

@RestController
@RequestMapping("/jpa/events")
class JpaEventController(
    private val eventRepository: EventRepository,
    private val noLockService: NoLockJpaTicketPurchaseService,
    private val pessimisticLockService: PessimisticLockJpaTicketPurchaseService,
    private val optimisticLockService: OptimisticLockJpaTicketPurchaseService,
    private val redisLockService: RedisLockTicketPurchaseService
) {

    @GetMapping("/{id}")
    fun getEvent(@PathVariable id: Long): ResponseEntity<EventResponse> =
        eventRepository.findById(id)
            .map { event -> ResponseEntity.ok(event.toResponse()) }
            .orElse(ResponseEntity.notFound().build())

    @PostMapping
    fun createEvent(@RequestBody request: CreateEventRequest): ResponseEntity<EventResponse> {
        val event = EventEntity(request.name, request.totalSeats)
        val saved = eventRepository.save(event)
        return ResponseEntity.ok(saved.toResponse())
    }

    @PostMapping("/{id}/purchase/no-lock")
    fun purchaseNoLock(@PathVariable id: Long): ResponseEntity<PurchaseResponse> =
        runCatching { noLockService.purchase(id) }
            .fold(
                onSuccess = { success -> createResponse(id, success) },
                onFailure = { e -> ResponseEntity.ok(PurchaseResponse.failure(e.message ?: "알 수 없는 오류")) }
            )

    @PostMapping("/{id}/purchase/pessimistic")
    fun purchasePessimistic(@PathVariable id: Long): ResponseEntity<PurchaseResponse> =
        runCatching { pessimisticLockService.purchase(id) }
            .fold(
                onSuccess = { success -> createResponse(id, success) },
                onFailure = { e -> ResponseEntity.ok(PurchaseResponse.failure(e.message ?: "알 수 없는 오류")) }
            )

    @PostMapping("/{id}/purchase/optimistic")
    fun purchaseOptimistic(@PathVariable id: Long): ResponseEntity<PurchaseResponse> =
        runCatching { optimisticLockService.purchase(id) }
            .fold(
                onSuccess = { success -> createResponse(id, success) },
                onFailure = { e -> ResponseEntity.ok(PurchaseResponse.failure(e.message ?: "알 수 없는 오류")) }
            )

    @PostMapping("/{id}/purchase/redis")
    fun purchaseRedisLock(@PathVariable id: Long): ResponseEntity<PurchaseResponse> =
        runCatching { redisLockService.purchase(id) }
            .fold(
                onSuccess = { success -> createResponse(id, success) },
                onFailure = { e -> ResponseEntity.ok(PurchaseResponse.failure(e.message ?: "알 수 없는 오류")) }
            )

    private fun createResponse(eventId: Long, success: Boolean): ResponseEntity<PurchaseResponse> =
        if (success) {
            val remainingSeats = eventRepository.findById(eventId)
                .map { it.remainingSeats }
                .orElse(-1)
            ResponseEntity.ok(PurchaseResponse.success(remainingSeats))
        } else {
            ResponseEntity.ok(PurchaseResponse.failure("매진되었습니다."))
        }

    private fun EventEntity.toResponse(): EventResponse = EventResponse(
        id = id ?: 0L,
        name = name,
        totalSeats = totalSeats,
        remainingSeats = remainingSeats
    )

    data class CreateEventRequest(val name: String, val totalSeats: Int)
}