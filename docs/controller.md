# Controller

A Controller in Spring Boot is the first time a project's code sees a user's HTTP
request.  It doesn't handle the business logic of the endpoints, but it passes off
the business logic to a [Service](service.md) object and handles the response back
to the user.

A class annotated with `@RestController` is a Controller.  Usually there are many
Controllers, but in the project, there is just one.  This annotation handles the job
of 2 annotations:

- `@Controller` - Spring looks for classes with this annotation to handle HTTP
requests and send back responses.
- `@ResponseBody` - Use the return value of methods as the response (instead of
resolving it to an HTML template page).

## Employees

The `EmployeeController` class has the annotations `@RestController` and
`@RequestMapping("/emp")`.  At the class level the `@RequestMapping` annotation
provides a path prefix that can be expanded upon by methods appending to that path.
Without this class-level annotation, many methods handling requests would need to
repeat the same prefix with each method, e.g. `@GetMapping("/emp/{id}")`,
`@PostMapping("/emp")`, etc.

This class has a constructor that takes Service objects that run the business logic of
the application.  But how do the Service objects get passed in to this constructor?
There is no `@Autowired` annotation.  If a class has only one constructor, like this
class does, the Spring Framework will inject beans/services that match the constructor's
parameters.

## Endpoints

Most endpoints utilize the `EmployeeService` for executing the business logic.  The
`/emp/paginated` endpoint runs validation on its query parameters.  The POST and PATCH
endpoints validate their JSON bodies with Bean Validation (see below).

Methods with some form of `*Mapping` annotation define the HTTP method (GET, POST,
etc.) and the path to the endpoint (appended to any class-level path).
- `@GetMapping("/{id}")` - HTTP GET method with a path variable that maps to a method
parameter.
- `@PostMapping` - HTTP POST method.  No path argument means it uses the root ("/") or
inherits the exact path specified for the entire class.  The POST body here is expected
in JSON.
- `@PatchMapping("/{id}")` - HTTP PATCH method.  The PATCH body here is expected in
JSON.
- There are other HTTP methods not covered here.

Parameters to these methods have their own annotations:
- `@RequestParam(value = "queryParamName", defaultValue = "defaultValue")` - Reads
from a query parameter `?queryParamName=value` or a form element in the request body.
- `@PathVariable("id")` - Reads from a path variable defined with braces, e.g.,
`@GetMapping("/{id}")`.
- `@RequestBody` - Expects JSON in the body that corresponds to the object type of the
parameter, e.g., `EmployeeCreateDTO`.  This only converts the JSON to the object.  It
doesn't check that the values make sense.
- `@Valid` - Used with `@RequestBody`.  After the JSON is converted, Spring checks the
Bean Validation constraints on the DTO's fields (e.g., `@NotBlank`, `@Positive`) before
the method body runs.  A violation throws `MethodArgumentNotValidException`, which the
[Global Exception Handler](error.md) turns into a `400 Bad Request` listing every
violated field.  This needs the `spring-boot-starter-validation` dependency.

The below endpoints are supported.  If an error occurs, then the error is handled by
the [Global Exception Handler](error.md).

- GET `/emp` - List all employees.
- POST `/emp` - Create a new employee.  The POST body is JSON.  If it's malformed, or
it breaks a validation rule, it results in a `400 Bad Request` error.  All five fields
are required: `firstName`, `lastName`, and `position` must not be blank, and `age` and
`salary` must be greater than 0 (an omitted number would otherwise silently be 0).
A violation of the rules above responds with a message listing each problem, e.g.,
`firstName is required; salary must be greater than 0`.  A good example:
```json
{
    "firstName": "Sam",
    "lastName": "Spade",
    "position": "crimefighter",
    "age": 32,
    "salary": 220000
}
```
Responds with `201 Created`.  In the Location header of the response is a URI of the
new resource, including the new employee ID, e.g., `/emp/5`.  The response body is
JSON with the new employee's ID:
```json
{"id": 5}
```
- PATCH `/emp/{id}` - Updates an existing employee, specified by the Employee ID.  The
PATCH body is JSON.  Returns `404 Not Found` if employee _n_ does not exist.  If
it's malformed, or it breaks a validation rule, it results in
a `400 Bad Request` error.  A good example:
  ```json
  {
    "salary": 230000
  }
  ```
  Each attribute is optional and only those supplied attributes are updated.  Any
of these attributes is accepted: `firstName`, `lastName`, `position`, `age`, and/or
`salary`.  Any unknown property results in a `400 Bad Request` response.  A supplied
value is validated: `firstName`, `lastName`, and `position` must not be blank, and `age`
and `salary` must be greater than 0.  An omitted attribute is not validated, because
most constraints treat `null` as valid (`@NotBlank` would reject it, so the DTO uses
`@Pattern` instead).  On success, it responds with `200 OK` and the full JSON for the
updated employee.
- GET `/emp/{id}` - Return an employee by employee ID.  Returns `404 Not Found` if
the employee ID does not exist.
- GET `/emp/paginated` - Lists employees one page at a time.  Query parameters:
    - `page=n` - Page number _n_, starting with 1.  Defaults to 1.  If less than 1,
then 1 is used.
    - `size=n` - Number of employees per page is _n_.  Defaults to 10.  If less than
1, then 1 is used.  If greater than 100, then 100 is used.
    - `sortBy=field` - Sort by `field`: `lastName` (default), `age`, or `salary`.  If
the sort field isn't one of those fields, it results in a `400 Bad Request` response.
    - `sortDir=dir` - Sort ascending `asc` (default), or descending `desc`.  Anything
other than `desc` (case-insensitive) is treated as `asc`.
- GET `/emp/promotion` - Determines if a specific employee can be promoted to a
specific title.  The configured LLM must be available for this to work properly.
Query parameter, which is required:
    - `prompt=prompt` - `Is Employee 2 approved for promotion to Staff Software Engineer?`
  Runs the prompt through the `PromotionHarnessService`, which sends it to a local LLM
with a tool that determines the result.  Responds with the LLM's response, e.g.:
  `Unfortunately, Employee 2's promotion request to Staff Software Engineer has not been approved. The reason provided is that there is no budget for a promotion and a raise.`