public class PrimeFinder {
    private static boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;    for (int i = 3; i * i <= n; i += 2) {
        if (n % i == 0) return false;
    }
    return true;
}

public static void main(String[] args) throws InterruptedException {
    final int MAX = 1000;
    final int THREADS = 10;
    final int RANGE = MAX / THREADS;

    Thread[] threads = new Thread[THREADS];

    for (int i = 0; i < THREADS; i++) {
        final int start = i * RANGE + 1;
        final int end = (i == THREADS - 1) ? MAX : (i + 1) * RANGE;

        threads[i] = new Thread(() -> {
            for (int num = start; num <= end; num++) {
                if (isPrime(num)) {
                    System.out.println(Thread.currentThread().getName() + " → " + num);
                }
            }
        }, "T" + (i + 1));

        threads[i].start();
    }

    for (Thread t : threads) {
        t.join();
    }

    System.out.println("\nFinished finding primes up to " + MAX);
}}

