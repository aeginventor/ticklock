package ticklock.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ticklock.controller.dto.EventResponse
import ticklock.controller.dto.PurchaseResponse
import ticklock.entity.EventEntity
import ticklock.repository.EventRepository
import ticklock.service.unified.*

@RestController
@RequestMapping("/api/events")
class UnifiedEventController(
    private val eventRepository: EventRepository,
    private val noLockService: NoLockUnifiedService,
    private val synchronizedService: SynchronizedUnifiedService,
    private val reentrantLockService: ReentrantLockUnifiedService,
    private val pessimisticLockService: PessimisticLockUnifiedService,
    private val optimisticLockService: OptimisticLockUnifiedService,
    private val redisLockService: RedisLockUnifiedService? = null
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
        executePurchase(id, noLockService)

    @PostMapping("/{id}/purchase/synchronized")
    fun purchaseSynchronized(@PathVariable id: Long): ResponseEntity<PurchaseResponse> =
        executePurchase(id, synchronizedService)

    @PostMapping("/{id}/purchase/reentrant-lock")
    fun purchaseReentrantLock(@PathVariable id: Long): ResponseEntity<PurchaseResponse> =
        executePurchase(id, reentrantLockService)

    @PostMapping("/{id}/purchase/pessimistic")
    fun purchasePessimistic(@PathVariable id: Long): ResponseEntity<PurchaseResponse> =
        executePurchase(id, pessimisticLockService)

    @PostMapping("/{id}/purchase/optimistic")
    fun purchaseOptimistic(@PathVariable id: Long): ResponseEntity<PurchaseResponse> =
        executePurchase(id, optimisticLockService)

    @PostMapping("/{id}/purchase/redis")
    fun purchaseRedis(@PathVariable id: Long): ResponseEntity<PurchaseResponse> =
        redisLockService?.let { executePurchase(id, it) }
            ?: ResponseEntity.ok(PurchaseResponse.failure("Redis가 비활성화되어 있습니다. spring.redis.enabled=true로 설정하세요."))

    private fun executePurchase(
        eventId: Long,
        service: UnifiedTicketPurchaseService
    ): ResponseEntity<PurchaseResponse> =
        runCatching { service.purchase(eventId) }
            .fold(
                onSuccess = { success -> createResponse(eventId, success) },
                onFailure = { e -> ResponseEntity.ok(PurchaseResponse.failure(e.message ?: "알 수 없는 오류")) }
            )

    private fun createResponse(
        eventId: Long,
        success: Boolean
    ): ResponseEntity<PurchaseResponse> =
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