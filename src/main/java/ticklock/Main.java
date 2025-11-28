package ticklock;

import ticklock.simulation.NoLockSimulation;

public class Main {
    public static void main(String[] args) {
        System.out.println("ticklock – Step 1: Pure Java concurrency lab");
        System.out.println("온라인 티켓팅 동시성 실험을 시작합니다.");
        System.out.println();

        NoLockSimulation simulation = new NoLockSimulation();
        simulation.run();
    }
}