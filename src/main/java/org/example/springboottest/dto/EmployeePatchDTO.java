package org.example.springboottest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Create a separate incoming patch DTO for PATCH requests to update an
 * employee.  It will have different validation rules for updating an employee
 * than a POST (or PUT) will have for creating an employee.  Use nullable types.
 * Have unknown fields create an error by placing this in application.properties:
 * spring.jackson.deserialization.fail-on-unknown-properties=true
 * Could override on a per-class basis with @JsonIgnoreProperties(ignoreUnknown = true).
 */
public class EmployeePatchDTO {
    // Every field is optional in a PATCH.  Most constraints treat null as
    // valid, so these only reject a value that was actually supplied.  (@NotBlank
    // would not work here, because it rejects null.)
    @Pattern(regexp = ".*\\S.*", message = "firstName must not be blank")
    private String firstName;

    @Pattern(regexp = ".*\\S.*", message = "lastName must not be blank")
    private String lastName;

    @Pattern(regexp = ".*\\S.*", message = "position must not be blank")
    private String position;

    @Positive(message = "age must be greater than 0")
    private Integer age;

    @Positive(message = "salary must be greater than 0")
    private Double salary;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }
}
