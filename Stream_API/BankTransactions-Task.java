import java.util.*;
import java.util.stream.*;

public class BankTransactions {
    public static void main(String[] args) {

        List<String> transactions = List.of(
                "TXN1001:SAVINGS:12000",
                "TXN1002:CURRENT:15000",
                "TXN1003:SAVINGS:20000",
                "TXN1004:SAVINGS:12000",
                "TXN1005:SAVINGS:8000",
                "TXN1006:CURRENT:30000"
        );

        List<Double> list =
                transactions.stream()
                        .filter(t -> t.split(":")[1].equals("SAVINGS"))
                        .map(t -> Double.parseDouble(t.split(":")[2]))
                        .filter(amount -> amount >= 10000)
                        .map(amount -> amount + (amount * 0.18))
                        .distinct()
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());

        long count =
                list.stream()
                        .filter(amount -> amount > 20000)
                        .count();

        System.out.println("Processed Amounts: " + list);
        System.out.println("Count of amounts > 20000: " + count);
    }
}
