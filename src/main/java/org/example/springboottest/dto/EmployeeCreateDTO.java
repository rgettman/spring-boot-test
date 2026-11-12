package org.example.springboottest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Create a separate incoming patch DTO for POST requests to create an
 * employee.  It will have different validation rules for creating an employee
 * than a PATCH will have for updating an employee.
 * Have unknown fields create an error by placing this in application.properties:
 * spring.jackson.deserialization.fail-on-unknown-properties=true
 * Could override on a per-class basis with @JsonIgnoreProperties(ignoreUnknown = true).
 */
public class EmployeeCreateDTO {
    // Bean Validation constraints.  They are only checked when the controller
    // parameter has @Valid.  Jackson cannot catch a missing field for a
    // primitive (it silently uses 0), so @Positive also rejects an omitted
    // age or salary.
    @NotBlank(message = "firstName is required")
    private String firstName;

    @NotBlank(message = "lastName is required")
    private String lastName;

    @NotBlank(message = "position is required")
    private String position;

    @Positive(message = "age must be greater than 0")
    private int age;

    @Positive(message = "salary must be greater than 0")
    private double salary;

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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }
}
