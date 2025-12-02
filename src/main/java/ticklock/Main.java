package ticklock;

import ticklock.simulation.AtomicSimulation;
import ticklock.simulation.NoLockSimulation;
import ticklock.simulation.SynchronizedSimulation;
import ticklock.simulation.ReentrantLockSimulation;

public class Main {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("ticklock – Step 1: Pure Java concurrency lab");
        System.out.println("온라인 티켓팅 동시성 실험을 시작합니다.");
        System.out.println("========================================");

        System.out.println("\n=== No-Lock 시뮬레이션 시작 ===");
        new NoLockSimulation().run();

        System.out.println("\n=== AtomicInteger 시뮬레이션 시작 ===");
        new AtomicSimulation().run();

        System.out.println("\n=== synchronized 시뮬레이션 시작 ===");
        new SynchronizedSimulation().run();

        System.out.println("\n=== ReentrantLock 시뮬레이션 시작 ===");
        new ReentrantLockSimulation().run();

        System.out.println("\n========================================");
        System.out.println("실험 종료");
        System.out.println("========================================");
    }
}