package org.example.springboottest.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import org.example.springboottest.entity.Employee;

/**
 * If there are multiple implementations, choose one with @Primary,
 * or @Qualifier(beanName) on the injection parameter, or put a different
 * profile on each, e.g. @Profile("local") / @Profile("prod").
 */
@Repository
@Profile("inmemory")
public class InMemoryEmployeeRepository implements EmployeeRepository {
    private final List<Employee> employees;
    private int maxEmployeeId;

    public InMemoryEmployeeRepository() {
        Employee elmer = new Employee();
        elmer.setId(1);
        elmer.setFirstName("Elmer");
        elmer.setLastName("Fudd");
        elmer.setAge(38);
        elmer.setPosition("hunter");
        elmer.setSalary(500.0);

        Employee bugs = new Employee();
        bugs.setId(2);
        bugs.setFirstName("Bugs");
        bugs.setLastName("Bunny");
        bugs.setAge(29);
        bugs.setPosition("wabbit");
        bugs.setSalary(1500.0);

        Employee suzie = new Employee();
        suzie.setId(3);
        suzie.setFirstName("Suzie");
        suzie.setLastName("Queue");
        suzie.setAge(40);
        suzie.setPosition("Principal Software Engineer");
        suzie.setSalary(1100.0);

        Employee robert = new Employee();
        robert.setId(4);
        robert.setFirstName("Robert");
        robert.setLastName("Stack");
        robert.setAge(43);
        robert.setPosition("Senior Software Engineer");
        robert.setSalary(1000.0);

        employees = new ArrayList<>(Arrays.asList(elmer, bugs, suzie, robert));
        maxEmployeeId = 4;
    }

    @Override
    public List<Employee> findAllEmployees() {
        return employees;
    }

    @Override
    public Optional<Employee> getEmployeeById(int id) {
        return employees.stream()
                .filter(e -> e.getId() == id)
                .findFirst();
    }

    @Override
    public int createEmployee(String firstName, String lastName, String position, int age, double salary) {
        int newEmployeeId = maxEmployeeId + 1;
        maxEmployeeId = newEmployeeId;

        Employee created = new Employee();
        created.setId(newEmployeeId);
        created.setFirstName(firstName);
        created.setLastName(lastName);
        created.setPosition(position);
        created.setAge(age);
        created.setSalary(salary);
        employees.add(created);

        return newEmployeeId;
    }

    // Already modified in the service.  Just return it back.
    @Override
    public Employee save(Employee employee) {
        return employee;
    }

    @Override
    public List<Employee> findAllByIdIn(Collection<Integer> ids) {
        return employees.stream()
                .filter(e -> ids.contains(e.getId()))
                .toList();
    }

    @Override
    public List<Employee> findAllEmployeesSorted(String sortBy, String sortDir) {
        Comparator<Employee> comparator;
        switch (sortBy) {
        case "age" -> comparator = Comparator.comparing(Employee::getAge);
        case "salary" -> comparator = Comparator.comparing(Employee::getSalary);
        case "lastName" -> comparator = Comparator.comparing(Employee::getLastName);
        default -> comparator = Comparator.comparing(Employee::getLastName);
        }
        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        return employees.stream()
                .sorted(comparator)
                .toList();
    }
}
