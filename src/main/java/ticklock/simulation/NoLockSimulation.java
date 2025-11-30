package ticklock.simulation;

import ticklock.domain.Event;
import ticklock.service.NoLockTicketPurchaseService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class NoLockSimulation {

    private final NoLockTicketPurchaseService service = new NoLockTicketPurchaseService();

    public void run() {
        int totalSeats = 100;
        int threadCount = 500; // 동시에 500명이 한 장씩 예매를 시도
        Event event = new Event(1L, "no-lock 시뮬레이션 공연", totalSeats);

        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        List<Boolean> results = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                boolean success = service.purchase(event);
                results.add(success);

                done.countDown();
            });
            t.start();
        }

        try {
            ready.await();
            start.countDown();
            done.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long successCount = results.stream().filter(b -> b).count();
        long failureCount = results.size() - successCount;
        int finalRemainingSeats = event.getRemainingSeats();
        int expectedRemainingSeats = totalSeats - (int) successCount;

        System.out.println("\n=== No-Lock 시뮬레이션 결과 ===");
        System.out.println("총 좌석 수: " + totalSeats);
        System.out.println("동시 구매 시도 수: " + threadCount);
        System.out.println();
        System.out.println("구매 성공 응답 수: " + successCount);
        System.out.println("구매 실패 응답 수: " + failureCount);
        System.out.println();
        System.out.println("최종 남은 좌석 수: " + finalRemainingSeats);
        System.out.println("예상 남은 좌석 수: " + expectedRemainingSeats);
        System.out.println();
        
        // 동시성 문제 검증
        long oversoldTickets = successCount - totalSeats;
        int actualDecreaseCount = totalSeats - finalRemainingSeats;  // 실제로 감소한 횟수
        int missedDecreaseCount = (int) successCount - actualDecreaseCount;  // 누락된 감소 연산
        
        boolean hasRaceCondition1 = oversoldTickets > 0;  // 경쟁 조건 #1
        boolean hasRaceCondition2 = missedDecreaseCount > 0;  // 경쟁 조건 #2
        
        if (hasRaceCondition1 || hasRaceCondition2) {
            System.out.println("Race Condition 발생\n");
        }
        
        if (hasRaceCondition1) {
            System.out.println("[경쟁 조건 #1] 재고 체크 단계에서 동시성 문제 발생");
            System.out.println("    실제 좌석: " + totalSeats + "석");
            System.out.println("    판매 성공: " + successCount + "명");
            System.out.println("    초과 판매: " + oversoldTickets + "명(문제)");
            System.out.println();
        }
        
        if (hasRaceCondition2) {
            System.out.println("[경쟁 조건 #2] 재고 감소 단계에서 동시성 문제 발생");
            System.out.println("    구매 성공 횟수: " + successCount + "회");
            System.out.println("    실제 감소 횟수: " + actualDecreaseCount + "회");
            System.out.println("    누락된 감소: " + missedDecreaseCount + "회(문제)");
            System.out.println("    최종 재고: " + finalRemainingSeats + "석 (예상: " + expectedRemainingSeats + "석)");
            System.out.println();
        }
        
        if (!hasRaceCondition1 && !hasRaceCondition2) {
            System.out.println("이번 실행에서는 문제가 발생하지 않았습니다.");
        }
    }
}