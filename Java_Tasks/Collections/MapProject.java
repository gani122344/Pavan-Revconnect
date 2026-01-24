package Java.Collections;

import java.util.*;

public class MapProject {
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
            stock.put(product,
                    stock.getOrDefault(product, 0) + incoming.get(product));
        }
        Map<String, Integer> sortedStock = new TreeMap<>(stock);
        int total = 0;
        System.out.println("Updated Stock:");
        for (String product : sortedStock.keySet()) {
            System.out.println(product + " -> " + sortedStock.get(product));
            total += sortedStock.get(product);
        }
        System.out.println("Total Units in Store: " + total);
    }
}
