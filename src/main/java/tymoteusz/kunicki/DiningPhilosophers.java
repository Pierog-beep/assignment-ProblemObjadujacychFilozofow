package tymoteusz.kunicki;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Stan w jakim znajduje się filozof
enum PhilosophersState {
    THINKING,
    HUNGRY,
    EATING
}

// Klasa reprezentująca współdzielony zasób (widelec)
class Fork {
    private final int id;
    private final Lock lock;

    public Fork(int id) {
        this.id = id;
        this.lock = new ReentrantLock();
    }

    // Blokuje zasób
    public void pickUp() {
        lock.lock();
    }

    // Zwalnia zasób
    public void putDown() {
        lock.unlock();
    }

    public int getId() {
        return id;
    }
}

// Klasa Arbitra / Kelnera, który zapobiega zakleszczeniom
class Waiter {
    private int currentEating;
    private final int maxAllowed;

    public Waiter(int maxAllowed) {
        this.currentEating = 0;
        this.maxAllowed = maxAllowed;
    }

    // Filozof wywołuje to, aby poprosić o pozwolenie na jedzenie
    public synchronized void askForPermission() throws InterruptedException {
        while (currentEating >= maxAllowed) {
            wait();
        }
        currentEating++;
    }

    // Filozof wywołuje to po zakończeniu jedzenia
    public synchronized void releaseForks() {
        currentEating--;
        notifyAll();
    }
}

// Klasa reprezentująca Filozofa (działa jako osobny wątek)
class Philosopher extends Thread {
    private final int id;
    private PhilosophersState philosophersState;
    private final Fork leftFork;
    private final Fork rightFork;
    private final Waiter waiter;

    public Philosopher(int id, Fork leftFork, Fork rightFork, Waiter waiter) {
        this.id = id;
        this.philosophersState = PhilosophersState.THINKING;
        this.leftFork = leftFork;
        this.rightFork = rightFork;
        this.waiter = waiter;
    }

    @Override
    public void run() {
        try {
            // Pętla symulująca ciągłe życie filozofa
            while (true) {
                think();

                changePhilosophersState(PhilosophersState.HUNGRY);
                System.out.println("Filozof " + id + " jest głodny i prosi kelnera o pozwolenie.");

                waiter.askForPermission();

                // Gdy kelner pozwoli, podnosimy widelce
                leftFork.pickUp();
                rightFork.pickUp();

                eat();

                // Po jedzeniu odkładamy widelce
                leftFork.putDown();
                rightFork.putDown();

                // Informujemy kelnera
                waiter.releaseForks();
            }
        } catch (InterruptedException e) {
            System.out.println("Filozof " + id + " został przerwany.");
            Thread.currentThread().interrupt();
        }
    }

    private void think() throws InterruptedException {
        changePhilosophersState(PhilosophersState.THINKING);
        System.out.println("Filozof " + id + " myśli.");
        // Symulacja myślenia (losowy czas do 1000 ms)
        Thread.sleep((long) (Math.random() * 1000));
    }

    private void eat() throws InterruptedException {
        changePhilosophersState(PhilosophersState.EATING);
        System.out.println("Filozof " + id + " je (widelce: " + leftFork.getId() + " i " + rightFork.getId() + ").");
        // Symulacja jedzenia (losowy czas do 1000 ms)
        Thread.sleep((long) (Math.random() * 1000));
    }

    public void changePhilosophersState(PhilosophersState newPhilosophersState) {
        this.philosophersState = newPhilosophersState;
    }
}

// Klasa główna testująca rozwiązanie
public class DiningPhilosophers {
    public static void main(String[] args) {
        int numberOfPhilosophers = 5;

        Fork[] forks = new Fork[numberOfPhilosophers];
        for (int i = 0; i < numberOfPhilosophers; i++) {
            forks[i] = new Fork(i);
        }

        Waiter waiter = new Waiter(numberOfPhilosophers - 1);

        Philosopher[] philosophers = new Philosopher[numberOfPhilosophers];
        for (int i = 0; i < numberOfPhilosophers; i++) {
            Fork leftFork = forks[i];
            Fork rightFork = forks[(i + 1) % numberOfPhilosophers];

            philosophers[i] = new Philosopher(i, leftFork, rightFork, waiter);
            philosophers[i].start();
        }
    }
}