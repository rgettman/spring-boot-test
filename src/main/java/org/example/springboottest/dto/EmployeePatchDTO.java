package org.example.springboottest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Create a separate incoming patch DTO for PATCH requests to update an
 * employee.  It will have different validation rules for updating an employee
 * than a POST (or PUT) will have for creating an employee.  Use nullable types.
 * Have unknown fields create an error by placing this in application.properties:
 * spring.jackson.deserialization.fail-on-unknown-properties=true
 * Could override on a per-class basis with @JsonIgnoreProperties(ignoreUnknown = true).
 */
public class EmployeePatchDTO {
    private String firstName;
    private String lastName;
    private String position;
    private Integer age;
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
