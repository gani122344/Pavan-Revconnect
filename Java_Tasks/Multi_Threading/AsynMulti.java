class AsyMul implements Runnable {

    int[][] matrix;
    int row;
    int[] result;

    AsyMul(int[][] matrix, int row, int[] result) {
        this.matrix = matrix;
        this.row = row;
        this.result = result;
    }

    public void run() {
        int sum = 0;
        for (int col = 0; col < matrix[row].length; col++) {
            sum += matrix[row][col];
        }
        result[row] = sum; 
    }
}

public class AsynMulti {

    public static void main(String[] args) throws InterruptedException {

        
        int[][] matrix = {
                {1, 2, 3, 4, 5, 6},
                {7, 8, 9, 10, 11, 12},
                {13, 14, 15, 16, 17, 18},
                {19, 20, 21, 22, 23, 24},
                {25, 26, 27, 28, 29, 30},
                {31, 32, 33, 34, 35, 36}
        };

        Thread[] threads = new Thread[6];
        int[] rowSums = new int[6]; 

        for (int i = 0; i < 6; i++) {
            threads[i] = new Thread(
                    new AsyMul(matrix, i, rowSums),
                    "Row-Thread-" + i
            );
            threads[i].start(); 
        }

        for (Thread t : threads) {
            t.join();
        }

        for (int i = 0; i < rowSums.length; i++) {
            System.out.println("Sum of row " + i + " = " + rowSums[i]);
        }
    }
}