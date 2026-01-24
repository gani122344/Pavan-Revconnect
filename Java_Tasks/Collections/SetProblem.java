package Java.Collections;

import java.util.*;

public class SetProblem {
    public static void main(String[] args) {

        List<String> applicants = Arrays.asList(
                "John", "Aisha", "Ravi", "John", "Mina", "Ravi", "Aisha", "Tom"
        );
        Set<String> unique = new HashSet<>(applicants);
        int duplicates = applicants.size() - unique.size();
        System.out.println("Unique Applicants: " + unique);
        System.out.println("Duplicates Removed: " + duplicates);
    }
}
