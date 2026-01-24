import java.util.*;
import java.util.stream.*;

public class ProductPrices {
    public static void main(String[] args){
        List<Integer> prices = List.of(
                500, 1200, 2500, 1200, 3000, 800, 2500
        );
        List<Integer> price = prices.stream().filter(n->n > 1000).distinct().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        long count =
                price.stream()
                        .filter(m -> m > 2000)
                        .count();

        System.out.println("Processed Prices: " + price );
        System.out.println("Count of Prices: " + count );

    }
}
