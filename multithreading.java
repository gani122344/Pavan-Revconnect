class PrimeTask implements Runnable {

    int start, end;

    PrimeTask(int start, int end) {
        this.start = start;
        this.end = end;
    }

    boolean isPrime(int n) {
        if (n <= 1) return false;

        for (int i = 2; i <= n / 2; i++) {
            if (n % i == 0)
                return false;
        }
        return true;
    }

    public void run() {
        for (int i = start; i <= end; i++) {
            if (isPrime(i)) {
                System.out.println(Thread.currentThread().getName()
                        + " -> " + i);
            }
        }
    }
}

public class multithreading {

    public static void main(String[] args) throws InterruptedException {

        Thread[] t = new Thread[10];
        int start = 1;

        for (int i = 0; i < 10; i++) {
            t[i] = new Thread(
                    new PrimeTask(start, start + 99),
                    "Thread-" + (i + 1)
            );
            t[i].start();
            start += 100;
        }

        for (Thread th : t) {
            th.join();
        }

        System.out.println("Done");
    }
}