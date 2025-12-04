package ticklock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ticklock.controller.dto.EventResponse;
import ticklock.controller.dto.PurchaseRequest;
import ticklock.controller.dto.PurchaseResponse;
import ticklock.domain.Event;
import ticklock.service.TicketPurchaseService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/events")
public class EventController {

    private final Map<Long, Event> eventStore = new ConcurrentHashMap<>();
    private final TicketPurchaseService purchaseService;

    public EventController(TicketPurchaseService purchaseService) {
        this.purchaseService = purchaseService;
        // 초기 데이터 세팅
        eventStore.put(1L, new Event(1L, "TICKLOCK 콘서트", 100));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        Event event = eventStore.get(id);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(EventResponse.from(event));
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<PurchaseResponse> purchase(@PathVariable Long id, 
                                                      @RequestBody(required = false) PurchaseRequest request) {
        Event event = eventStore.get(id);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }

        boolean success = purchaseService.purchase(event);
        
        if (success) {
            return ResponseEntity.ok(PurchaseResponse.success(event.getRemainingSeats()));
        } else {
            return ResponseEntity.ok(PurchaseResponse.failure("매진되었습니다."));
        }
    }

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventResponse request) {
        long id = eventStore.size() + 1;
        Event event = new Event(id, request.name(), request.totalSeats());
        eventStore.put(id, event);
        return ResponseEntity.ok(EventResponse.from(event));
    }
}

