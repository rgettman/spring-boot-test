package org.example.springboottest.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.example.springboottest.entity.Employee;

public interface EmployeeRepository {

    List<Employee> findAllEmployees();

    List<Employee> findAllByIdIn(Collection<Integer> ids);

    List<Employee> findAllEmployeesSorted(String sortBy, String sortDir);

    Optional<Employee> getEmployeeById(int id);

    int createEmployee(String firstName, String lastName, String position, int age, double salary);

    Employee save(Employee employee);
}
