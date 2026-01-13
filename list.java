package list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class list {
    public static void main(String[] args) {

        List<Integer> rating = new ArrayList<>(
                Arrays.asList(9, 10, 12, 8, -1, 7, 11, 10, 6)
        );
        rating.removeIf(r -> r < 1 || r > 10);

        Collections.sort(rating);
        int highest = Collections.max(rating);
        int lowest = Collections.min(rating);

        int sum = 0;
        for (int value : rating) {
            sum += value;
        }
        double average = (double) sum / rating.size();
        System.out.println("Cleaned Ratings: " + rating);
        System.out.println("Highest Rating: " + highest);
        System.out.println("Lowest Rating: " + lowest);
        System.out.printf("Average Rating: %.2f", average);
    }
}