package org.example.springboottest.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.example.springboottest.entity.Employee;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * If there are multiple implementations, choose one with @Primary,
 * or @Qualifier(beanName) on the injection parameter, or put a different
 * profile on each, e.g. @Profile("local") / @Profile("prod").
 * Assumes that a database has been configured with the "demo" schema and an
 * "employee" table with columns corresponding to the fields of this class.
 */
@Repository
@Profile("postgres")
public interface PostgresEmployeeRepository extends EmployeeRepository, JpaRepository<Employee, Integer> {
    /**
     * JPA won't recognize "findAllEmployees" by default.
     * Give it its own JPQL query instead.
     */
    @Query("SELECT e FROM Employee e ORDER BY e.id")
    @Override
    List<Employee> findAllEmployees();

    @Override
    Optional<Employee> getEmployeeById(int id);

    /**
     * Retrofit createEmployee to use JPA's built-in save() method.
     * @return The new employee's ID.
     */
    @Override
    default int createEmployee(String firstName, String lastName, String position, int age, double salary) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setPosition(position);
        employee.setAge(age);
        employee.setSalary(salary);

        // JPA provides "save(entity)" already!
        employee = save(employee);
        return employee.getId();
    }

    @Override
    public Employee save(Employee employee);

    @Override
    default List<Employee> findAllByIdIn(Collection<Integer> ids) {
        return findAllById(ids);
    }

    @Override
    default List<Employee> findAllEmployeesSorted(String sortBy, String sortDir) {
        Sort sort = "desc".equalsIgnoreCase(sortDir) ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        // Inherited from JpaRepository!
        return findAll(sort);
    }
}
