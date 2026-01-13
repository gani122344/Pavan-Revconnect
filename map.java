import java.util.*;
public class map {
    public static void main(String[] args) {
        Map<String, Integer> stock = new HashMap<>();
        stock.put("MacBook", 5);
        stock.put("iPhone", 10);
        stock.put("AirPods", 25);
        Map<String, Integer> incoming = new HashMap<>();
        incoming.put("iPhone", 5);
        incoming.put("AirPods", 5);
        incoming.put("VisionPro", 2);
        for (String product : incoming.keySet()) {
            int newQty = incoming.get(product);

            if (stock.containsKey(product)) {
                stock.put(product, stock.get(product) + newQty);
            } else {
                stock.put(product, newQty);
            }
        }
        Map<String, Integer> sortedStock = new TreeMap<>(stock);

        int total = 0;
        for (int qty : sortedStock.values()) {
            total += qty;
        }
        System.out.println("Updated Stock:");
        for (String product : sortedStock.keySet()) {
            System.out.println(product + " -> " + sortedStock.get(product));
        }
        System.out.println("\nTotal Units in Store: " + total);
    }
}