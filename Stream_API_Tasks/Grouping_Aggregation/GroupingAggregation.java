import java.util.*;
import java.util.stream.Collectors;

public class GroupingAggregation {

  static class Employee {
    String name;
    String department;
    double salary;

    public Employee(String name, String department, double salary) {
      this.name = name;
      this.department = department;
      this.salary = salary;
    }

    public String getDepartment() {
      return department;
    }

    public double getSalary() {
      return salary;
    }
  }

  public static void main(String[] args) {
    List<Employee> employees = Arrays.asList(
        new Employee("Alice", "IT", 80000),
        new Employee("Bob", "HR", 60000),
        new Employee("Charlie", "Finance", 90000),
        new Employee("David", "IT", 75000),
        new Employee("Eve", "HR", 120000),
        new Employee("Frank", "Finance", 50000));

        // Calculate average salary
    Map<String, Double> avgSalaryByDept = employees.stream()
        .collect(Collectors.groupingBy(
            Employee::getDepartment,
            Collectors.averagingDouble(Employee::getSalary)));

    System.out.println("Average Salary per Department:");
    avgSalaryByDept.forEach((dept, avg) -> System.out.println(dept + ": " + avg));
  }
}
