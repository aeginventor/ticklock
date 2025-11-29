package ticklock.simulation;

import ticklock.domain.Event;
import ticklock.service.NoLockTicketPurchaseService;

import java.util.ArrayList;
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

        List<Boolean> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Thread t = new Thread(() -> {
                ready.countDown();
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                boolean success = false;
                try {
                    success = service.purchase(event);
                } catch (Exception e) {
                    System.out.println("예외 발생: " + e.getMessage());
                }
                synchronized (results) {
                    results.add(success);
                }

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

        System.out.println("=== no-Lock 시뮬레이션 결과 ===");
        System.out.println("총 좌석 수: " + totalSeats);
        System.out.println("스레드 수(시도 수): " + threadCount);
        System.out.println("성공 횟수: " + successCount);
        System.out.println("실패 횟수: " + failureCount);
        System.out.println("최종 남은 좌석 수: " + event.getRemainingSeats());
        System.out.println("이론상 남은 좌석 수(totalSeats - successCount): " + (totalSeats - successCount));
    }
}