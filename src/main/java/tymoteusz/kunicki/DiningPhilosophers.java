package tymoteusz.kunicki;

import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Fork {
    private final int id;
    private final Lock lock;

    public Fork(int id) {
        this.id = id;
        this.lock = new ReentrantLock(true);
    }

    public void pickUp() throws InterruptedException {
        lock.lockInterruptibly();
    }

    public void putDown() {
        lock.unlock();
    }

    public int getId() {
        return id;
    }
}

class Philosopher extends Thread {
    private final int id;
    private final Fork leftFork;
    private final Fork rightFork;

    private final Fork firstFork;
    private final Fork secondFork;

    private volatile boolean running = true;
    private long totalEatingTime = 0;

    public Philosopher(int id, Fork leftFork, Fork rightFork) {
        this.id = id;
        this.leftFork = leftFork;
        this.rightFork = rightFork;

        if (leftFork.getId() < rightFork.getId()) {
            this.firstFork = leftFork;
            this.secondFork = rightFork;
        } else {
            this.firstFork = rightFork;
            this.secondFork = leftFork;
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                think();

                // Zawsze podnosimy najpierw widelec o mniejszym ID
                firstFork.pickUp();
                try {
                    // Następnie podnosimy widelec o wyższym ID
                    secondFork.pickUp();
                    try {
                        eat();
                    } finally {
                        secondFork.putDown();
                    }
                } finally {
                    firstFork.putDown();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Filozof " + id + " został przerwany.");
            Thread.currentThread().interrupt();
        }
    }

    private void think() throws InterruptedException {
        System.out.println("Filozof " + id + " myśli.");
        Thread.sleep((long) (Math.random() * 50));
    }

    private void eat() throws InterruptedException {
        long start = System.currentTimeMillis();
        long eatDuration = (long) (Math.random() * 50);

        System.out.println("Filozof " + id + " je (widelce: " + leftFork.getId() + " i " + rightFork.getId() + ").");
        Thread.sleep(eatDuration);

        long end = System.currentTimeMillis();
        totalEatingTime += (end - start);
    }

    public void stopPhilosopher() {
        running = false;
        this.interrupt();
    }

    public long getTotalEatingTime() {
        return totalEatingTime;
    }

    public int getPhilosopherId() {
        return id;
    }
}


public class DiningPhilosophers {
    static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Podaj liczbę filozofów: ");
        int numberOfPhilosophers = scanner.nextInt();

        if (numberOfPhilosophers < 2) {
            System.out.println("Do uruchomienia symulacji potrzeba co najmniej 2 filozofów.");
            return;
        }

        System.out.print("Podaj czas działania programu (w sekundach): ");
        double runTimeMinutes = scanner.nextDouble();
        long runTimeMillis = (long) (runTimeMinutes * 1000);

        System.out.println("Uruchamianie symulacji na " + runTimeMinutes + " min...");

        Fork[] forks = new Fork[numberOfPhilosophers];
        for (int i = 0; i < numberOfPhilosophers; i++) {
            forks[i] = new Fork(i);
        }

        Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
        for (int i = 0; i < numberOfPhilosophers; i++) {
            Fork leftFork = forks[i];
            Fork rightFork = forks[(i + 1) % numberOfPhilosophers];
            philosophers[i] = new Philosopher(i, leftFork, rightFork);
            philosophers[i].start();
        }

        Thread.sleep(runTimeMillis);
        System.out.println("Zatrzymywanie symulacji...");

        for (Philosopher p : philosophers) {
            p.stopPhilosopher();
        }

        for (Philosopher p : philosophers) {
            p.join();
        }

        System.out.println("ID_Filozofa, LacznyCzasJedzenia_ms");
        for (Philosopher p : philosophers) {
            System.out.println(p.getPhilosopherId() + "," + p.getTotalEatingTime());
        }

        scanner.close();
    }
}