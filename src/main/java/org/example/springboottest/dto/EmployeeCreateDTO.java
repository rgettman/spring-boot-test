package org.example.springboottest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Create a separate incoming patch DTO for POST requests to create an
 * employee.  It will have different validation rules for creating an employee
 * than a PATCH will have for updating an employee.
 * Have unknown fields create an error by placing this in application.properties:
 * spring.jackson.deserialization.fail-on-unknown-properties=true
 * Could override on a per-class basis with @JsonIgnoreProperties(ignoreUnknown = true).
 */
public class EmployeeCreateDTO {
    private String firstName;
    private String lastName;
    private String position;
    private int age;
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
