package Java.Exceptions;

public class Fifa {

    static class InvalidScoreException extends RuntimeException {
        public InvalidScoreException(String message) {
            super(message);
        }
    }

    public static int goalDifference(int scored, int conceded) {
        if (scored < 0 || conceded < 0) {
            throw new InvalidScoreException("Scores cannot be negative");
        }
        return scored - conceded;
    }

    public static void main(String[] args) {
        try {
            int diff = goalDifference(3, -1);
            System.out.println("Goal Difference: " + diff);

        } catch (InvalidScoreException e) {
            System.out.println("Invalid score: " + e.getMessage());

        } catch (Exception e) {
            System.out.println("Unexpected error occurred");

        } finally {
            System.out.println("Match stats processed");
        }
    }
}
