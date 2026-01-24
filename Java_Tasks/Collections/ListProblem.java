package Java.Collections;

import java.util.*;

public class ListProblem {
    public static void main(String[] args) {
        int[] ratings = {9, 10, 12, 8, -1, 7, 11, 10, 6};
        List<Integer> cleanedRatings = new ArrayList<>();
        for (int rating : ratings) {
            if (rating >= 1 && rating <= 10) {
                cleanedRatings.add(rating);
            }
        }
        Collections.sort(cleanedRatings);
        int highest = Collections.max(cleanedRatings);
        int lowest = Collections.min(cleanedRatings);



        int sum = 0;
        for (int r : cleanedRatings) {
            sum += r;
        }
        int size=cleanedRatings.size();

        double average = (double) sum / size;
        System.out.println("Cleaned Ratings: " + cleanedRatings);
        System.out.println("Highest Rating: " + highest);
        System.out.println("Lowest Rating: " + lowest);
        System.out.printf("Average Rating: %.2f", average);
    }
}
