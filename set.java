package list;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
public class set {
    public static void main(String[] args) {
        List<String> applicants = Arrays.asList(
                "John", "Aisha", "Ravi", "John",
                "Mina", "Ravi", "Aisha", "Tom"
        );
        Set<String> uniqueApplicants = new TreeSet<>(applicants);
        int duplicatesRemoved = applicants.size() - uniqueApplicants.size();
        System.out.println("Unique Applicants: " + uniqueApplicants);
        System.out.println("Duplicates Removed: " + duplicatesRemoved);
    }


}