import java.util.LinkedList;
import java.util.Queue;

public class queue {
        public static void main(String[] args) {

            Queue<String> emergency = new LinkedList<>();
            Queue<String> normal = new LinkedList<>();
            addPatient("Arjun", 5, emergency, normal);
            addPatient("Mia", 9, emergency, normal);
            addPatient("Leo", 7, emergency, normal);
            addPatient("Sara", 10, emergency, normal);

            while (!emergency.isEmpty()) {
                System.out.println("Emergency case-> " + emergency.poll());
            }
            while (!normal.isEmpty()) {
                System.out.println("Treating -> " + normal.poll());
            }
        }

        public static void addPatient(String name, int severity,
                               Queue<String> emergency, Queue<String> normal) {
            if (severity > 8) {
                emergency.add(name);
            } else {
                normal.add(name);
            }
        }
    }