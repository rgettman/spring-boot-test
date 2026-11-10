package org.example.springboottest.dto;

/**
 * Outgoing DTO is record for immutability.
 */
public record EmployeeDTO(int id, String firstName, String lastName, String position, int age, double salary) {
}
