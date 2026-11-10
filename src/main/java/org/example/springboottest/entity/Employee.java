package org.example.springboottest.entity;

import jakarta.persistence.*;

/**
 * In a typical JPA setup, this would have @Entity for sure, and possibly
 * the annotation @Table(name = "employees").
 * Could use Lombok to generate getters and setters, because it's not a record.
 * E.g. @Getter and @Setter
 * This class CANNOT be final when using Hibernate, because Hibernate proxies
 * classes annotated with @Entity with a proxy subclass.  No final fields or
 * methods are allowed for the same reason.
 * Must be Serializable unless configuring RedisCacheManager to use JSON.
 */
@Entity
@Table(name = "employee", schema = "demo")
public class Employee {
    /**
     * Typically @Id to mark a primary key column.
     * Could have @GeneratedValue(strategy = GenerationType.IDENTITY)
     * Non-pk fields could have @Column(unique = true)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /**
     * Could have @Column(name = "first_name") if the column name differs.
     * Else the column name is assumed to the field name.
     */
    @Column(name = "first_name")
    private String firstName;

    /**
     * E.g. @Column(name = "last_name").
     */
    @Column(name = "last_name")
    private String lastName;

    /**
     * Could have @Column(nullable = false, length=50)
     */
    @Column(name = "position")
    private String position;

    /**
     * E.g. @Column(name = "age")
     */
    @Column(name = "age")
    private int age;

    /**
     * E.g. @Column(name = "salary")
     */
    @Column(name = "salary")
    private double salary;

    // Must have a no-args constructor, public or protected.  Default is fine.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

