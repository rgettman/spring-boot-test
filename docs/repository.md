# Repository

Repositories act as sources of data for a Spring Boot application.  These classes are
annotated with `@Repository`.  They are the layer that handles access to persistent
data, often called the data access layer or DAO (Data Access Object) layer.  It
translates low-level, checked database exceptions, e.g., `SQLException`, to Spring
unchecked exceptions, e.g., `DataAccessException`.

Repositories don't have to access databases specifically, and when they do access
databases, it doesn't have to be with JPA or Hibernate, although they often do.
Either way, its operations work with Entity objects, not DTOs (data transfer objects).

This application supports 2 different repositories: an in-memory repository and a
database repository, unified by a common interface.

## EmployeeRepository

This interface represents the common functionality for keeping track of Employees.  It
defines the following methods:

- `List<Employee> findAllEmployees();` - Returns a `List` of all `Employee`s.
- `List<Employee> findAllByIdIn(Collection<Integer> ids);` - Returns a `List` of all
`Employee`s whose IDs are in the given collection.
- `List<Employee> findAllEmployeesSorted(String sortBy, String sortDir);` - Returns a
`List` of all `Employee`s sorted by the given field in the given direction.
- `Optional<Employee> getEmployeeById(int id);` - Return one `Employee` with the given ID.
- `int createEmployee(String firstName, String lastName, String position, int age, double salary);` -
Create a new `Employee` with the given attributes, and returns its new ID.
- `Employee save(Employee employee);` - Update an existing `Employee`.

Exactly one profile of `inmemory` or `postgres` must be active, or else no repository
bean will be created, causing startup of the entire application to fail.

## InMemoryEmployeeRepository

This class implements `EmployeeRepository`, storing a list of Employees in-memory.
It has the `@Repository` annotation and `@Profile("inmemory")`, which means that
Spring will only create this bean if the "inmemory" profile is active.

It is created with 4 already existing "employees" and supplies in-memory operations.

There are no transactions or thread safety with this implementation.  Because this is
purely in-memory, changes are lost if the application is restarted.

## PostgresEmployeeRepository

This interface extends `EmployeeRepository`, but it stores employees in a Postgres
database.  It has the `@Repository` annotation and `@Profile("postgres")`, which means
that Spring will only create this bean if the "postgres" profile is active.

It relies on JPA (Jakarta Persistence API), by utilizing the Spring Boot dependency
`spring-boot-starter-data-jpa`.  Spring Data JPA supplies the interface, and this
dependency's default implementation is Hibernate.

This interface also extends `JpaRepository<Employee, Integer>`, with the type arguments
representing the type of entity object and the type of the primary key for the entity
object.  That means that this class inherits a lot of methods that supply common
functionality for accessing entities in the database.  Supply the query used for a
repository method with the `@Query(JPQL query here)` annotation, e.g.,
`@Query("SELECT e FROM Employee e ORDER BY e.id")`.  This is necessary for the
`findAllEmployees` method, because Spring Data would look for an "employees" property,
which would not be found.

It does not enforce transactional boundaries, which are left to the
[Service layer](service.md).  

### Employee Entity

The `Employee` entity class is annotated with `@Entity`, which tells JPA that this
class's objects are represented by rows in the database.  It is also annotated with
`@Table(name = "employee", schema = "demo")` to control where in the database is the
appropriate table.  Without `@Table`, the schema searched will be the default schema
of the database connection, and the table name matches the class name.  Multi-word
table names, written in `CamelCase` get converted by Spring Boot to lowercase names
separated by underscores, e.g. `TopEmployee` defaults to the table name `top_employee`.

The field for the primary key is annotated with `@Id`.  Because this field is
auto-generated, it also has the `@GeneratedValue(strategy = GenerationType.IDENTITY)`
annotation, which tells JPA that there is an identity column for a new row in the table.

Other fields are annotated with the `@Column` annotation, which has optional elements
that define other characteristics of the column, such as actual column name,
nullability, length, whether it's a unique column, etc.  Because of the naming strategy,
most of the `@Column` annotations are redundant, but they are provided as examples.

### Employee Table

This is the SQL for creating the "employee" table in the "demo" schema in a Postgres
database.

```sql
CREATE SCHEMA IF NOT EXISTS demo;

CREATE TABLE IF NOT EXISTS demo.employee
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY,
    first_name text,
    last_name text,
    position text,
    age integer,
    salary double precision,
    CONSTRAINT employee_pkey PRIMARY KEY (id)
)
```