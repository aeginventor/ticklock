package ticklock;

import ticklock.simulation.NoLockSimulation;
import ticklock.simulation.SynchronizedSimulation;

public class Main {
    public static void main(String[] args) {
        System.out.println("ticklock – Step 1: Pure Java concurrency lab");
        System.out.println("온라인 티켓팅 동시성 실험을 시작합니다.");
        System.out.println();

        System.out.println("=== No-Lock 시뮬레이션 시작 ===");
        NoLockSimulation noLockSimulation = new NoLockSimulation();
        noLockSimulation.run();

        System.out.println();
        System.out.println("=== synchronized 시뮬레이션 시작 ===");
        SynchronizedSimulation synchronizedSimulation = new SynchronizedSimulation();
        synchronizedSimulation.run();
    }
}