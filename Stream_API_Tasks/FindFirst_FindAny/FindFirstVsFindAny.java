import java.util.*;

public class FindFirstVsFindAny {
  public static void main(String[] args) {
    List<Integer> numbers = Arrays.asList(1, 3, 5, 8, 10, 12, 7, 9);

    System.out.println("Numbers: " + numbers);

    Optional<Integer> firstEven = numbers.stream()
        .filter(n -> n % 2 == 0)
        .findFirst();

    System.out.println("findFirst() Result: " + firstEven.orElse(-1));

    Optional<Integer> anyEven = numbers.parallelStream()
        .filter(n -> n % 2 == 0)
        .findAny();

    System.out.println("findAny() Result: " + anyEven.orElse(-1));

  
  }
}
