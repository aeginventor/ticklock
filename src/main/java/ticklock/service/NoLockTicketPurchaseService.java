package ticklock.service;

import ticklock.domain.Event;

public class NoLockTicketPurchaseService {

    public boolean purchase(Event event) {
        if (!event.hasRemainingSeats()) {
            return false;
        }
        
        // 실무에서는 여기서 결제 처리, DB 조회 등이 발생할 수 있음
        // 이 짧은 시간 동안 다른 스레드가 끼어들 수 있음
        try {
            Thread.sleep(0, 100); // 100 nanoseconds - 아주 짧은 지연
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 재고 감소 (여기서 동시성 문제 발생)
        event.decreaseSeat();
        return true;
    }
}