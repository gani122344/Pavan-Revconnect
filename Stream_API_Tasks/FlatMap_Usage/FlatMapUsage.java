import java.util.*;
import java.util.stream.Collectors;

public class FlatMapUsage {
  public static void main(String[] args) {
    List<List<String>> data = Arrays.asList(
        Arrays.asList("apple", "banana"),
        Arrays.asList("banana", "cherry"),
        Arrays.asList("date", "apple"));

    System.out.println("Original Nested List: " + data);

    // Remove duplicates -> Sort reverse alphabetical
    List<String> result = data.stream()
        .flatMap(List::stream) // Flatten
        .distinct() // Remove duplicates
        .sorted(Comparator.reverseOrder()) // Sort reverse
        .collect(Collectors.toList());

    System.out.println("Processed List: " + result);
  }
}
