package ticklock;

import ticklock.simulation.NoLockSimulation;
import ticklock.simulation.SynchronizedSimulation;
import ticklock.simulation.ReentrantLockSimulation;
import ticklock.simulation.AtomicSimulation;

public class Main {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("ticklock – Step 1: Pure Java concurrency lab");
        System.out.println("온라인 티켓팅 동시성 실험을 시작합니다.");
        System.out.println("========================================");

        System.out.println("\n=== 1. No-Lock (문제 상황) ===");
        new NoLockSimulation().run();

        System.out.println("\n=== 2. synchronized ===");
        new SynchronizedSimulation().run();

        System.out.println("\n=== 3. ReentrantLock ===");
        new ReentrantLockSimulation().run();

        System.out.println("\n=== 4. AtomicInteger ===");
        new AtomicSimulation().run();

        System.out.println("\n========================================");
        System.out.println("실험 종료");
        System.out.println("========================================");
    }
}