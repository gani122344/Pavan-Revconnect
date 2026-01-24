import java.util.*;

public class CustomCollector {

  static class WordStats {
    long totalWords = 0;
    Set<String> distinctWords = new HashSet<>();

    public WordStats merge(WordStats other) {
      this.totalWords += other.totalWords;
      this.distinctWords.addAll(other.distinctWords);
      return this;
    }

    @Override
    public String toString() {
      return "Total Words: " + totalWords + ", Distinct Words: " + distinctWords.size();
    }
  }

  public static void main(String[] args) {
    List<String> sentences = Arrays.asList(
        "hello world",
        "hello java world",
        "java streams are cool");

    System.out.println("Sentences: " + sentences);

    // Count total words and distinct words using a custom collector
    WordStats stats = sentences.stream()
        .flatMap(line -> Arrays.stream(line.split("\\s+"))) // Split into words
        .collect(Collector.of(
            WordStats::new, // Supplier: create new result container
            (s, word) -> { // Accumulator: add word to stats
              s.totalWords++;
              s.distinctWords.add(word);
            },
            WordStats::merge // Combiner: merge two stats (for parallel)
        ));

    System.out.println("Result: " + stats);
  }
}
