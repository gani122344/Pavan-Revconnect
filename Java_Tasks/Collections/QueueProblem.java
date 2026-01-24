package Java.Collections;
import java.util.*;

public class QueueProblem {
    public static void main(String[] args) {
        Queue<Patient> queue = new LinkedList<>();
        queue.add(new Patient("Arjun", 5));
        queue.add(new Patient("Mia", 9));
        queue.add(new Patient("Leo", 7));
        queue.add(new Patient("Sara", 10));

        List<Patient> emergency = new ArrayList<>();
        Queue<Patient> normal = new LinkedList<>();
        while (!queue.isEmpty()) {
            Patient p = queue.poll();
            if (p.severity > 8) {
                emergency.add(p);
            } else {
                normal.add(p);
            }
        }
        for (Patient p : emergency) {
            System.out.println("Emergency case -> " + p.name);
        }
        while (!normal.isEmpty()) {
            System.out.println("Treating -> " + normal.poll().name);
        }
    }
}
class Patient {
    String name;
    int severity;
    Patient(String name, int severity) {
        this.name = name;
        this.severity = severity;
    }
}
