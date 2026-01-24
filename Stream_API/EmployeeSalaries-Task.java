import java.util.*;
import java.util.stream.*;

public class EmployeeSalaries {
    public static void main(String[] args){
        List<String> employees = List.of(
                "Alice:60000",
                "Bob:45000",
                "Charlie:75000",
                "David:60000",
                "Eva:90000"
        );
       List<Integer> annualSalaries = employees.stream().map(e -> e.split(":")[1]).map(Integer::parseInt).filter(salary -> salary>50000).map(salary -> salary*12).distinct().sorted(Comparator.reverseOrder()).skip(1).limit(2).collect(Collectors.toList());
       long count=annualSalaries.size();
       System.out.println("Annual Salaries for Processing: "+annualSalaries);
       System.out.println("Count:" + count);



    }
}
