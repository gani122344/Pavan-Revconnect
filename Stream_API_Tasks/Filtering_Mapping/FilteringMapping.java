import java.util.*;
import java.util.stream.Collectors;

public class FilteringMapping {

  static class Employee {
    String name;
    double salary;

    public Employee(String name, double salary) {
      this.name = name;
      this.salary = salary;
    }
  }

  public static void main(String[] args) {
    List<Employee> employees = Arrays.asList(
        new Employee("Alice", 80000),
        new Employee("Bob", 60000),
        new Employee("Charlie", 90000),
        new Employee("David", 75000),
        new Employee("Eve", 120000));
    List<String> result = employees.stream()
        .filter(e -> e.salary > 75000)
        .map(e -> e.name)
        .sorted()
        .collect(Collectors.toList());

    System.out.println("Employees with salary > 75,000 (Sorted): " + result);
  }
}
