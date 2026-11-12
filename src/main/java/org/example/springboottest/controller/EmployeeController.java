package org.example.springboottest.controller;

import java.net.URI;
import java.util.List;
import java.util.Set;

import jakarta.validation.Valid;
import org.example.springboottest.dto.EmployeeCreateDTO;
import org.example.springboottest.dto.EmployeeDTO;
import org.example.springboottest.dto.EmployeePatchDTO;
import org.example.springboottest.service.EmployeeService;
import org.example.springboottest.service.PromotionHarnessService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/emp")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final PromotionHarnessService promotionHarnessService;

    /**
     * Constructor injection means this does NOT need the @Autowired annotation
     * if there is only one constructor, which is typical here.
     * Lombok could automate this constructor with @RequiredArgsConstructor on
     * the class.
     * If EmployeeService is an interface, and there are multiple implementations
     * that all have @Service/@Component/etc., NoUniqueBeanDefinitionException,
     * unless one of them has @Primary, or at the injection site, the variable
     * name is named after the implementation name or the parameter has the
     * annotation @Qualifier("nameOfClass"), the "bean name" with first letter
     * lowercase: SpecificImplementation -> specificImplementation.  The name
     * could also match the name given in @Component("name").
     * @param employeeService Injected by Spring!
     * @param promotionHarnessService Injected by Spring!
     */
    public EmployeeController(EmployeeService employeeService, PromotionHarnessService promotionHarnessService) {
        this.employeeService = employeeService;
        this.promotionHarnessService = promotionHarnessService;
    }

    /**
     * Send empty string and trailing slash as possibilities, so that the URLs
     * "/emp" AND "/emp/" (with no trailing ID) go here.
     */
    @GetMapping(value = {"", "/"})
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    /**
     * Use @RequestParam to read the given param name and use default values.
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<EmployeeDTO>> getEmployeesPaginated(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "lastName") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        // Validate and constrain.
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 1;
        }
        else if (size > 100) {
            size = 100;
        }

        if (!Set.of("lastName", "age", "salary").contains(sortBy)) {
            throw new IllegalArgumentException("sortBy must be one of the values: lastName, age, salary.");
        }
        String direction = "desc".equalsIgnoreCase(sortDir) ? "desc" : "asc";

        Page<EmployeeDTO> result = employeeService.getEmployeesPaginated(page, size, sortBy, direction);
        return ResponseEntity.ok(result);
    }

    /**
     * The path on @GetMapping may have a leading slash, but it's ok not to
     * have a leading slash.
     * You MUST match the name supplied in @PathVariable to the placeholder
     * supplied in @GetMapping, or Spring will throw an IllegalArgumentException
     * that the name for the argument is not specified!
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployee(@PathVariable("id") int id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @GetMapping("/promotion")
    public ResponseEntity<String> getEmployeePromotionRequestStatus(@RequestParam String prompt) {
        return ResponseEntity.ok(promotionHarnessService.execEmployeePromotionStatusRequestTask(prompt));
    }

    /**
     * The @RequestBody annotation takes its values from the request body but
     * NOT the query parameters.  The @RequestParam annotation takes its values
     * from either the query parameters or the request body. @Valid runs the
     * Bean Validation constraints on the DTO after Jackson converts the JSON.
     * A violation throws MethodArgumentNotValidException before this method
     * body runs.
     */
    @PostMapping
    public ResponseEntity<String> createEmployee(@Valid @RequestBody EmployeeCreateDTO createDTO) {
        int id = employeeService.createEmployee(createDTO);

        // Use ServletUriComponentsBuilder to construct a URI.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body("{\"id\": " + id + "}");
    }

    // TODO: Bulk insert.

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(@PathVariable("id") int id, @Valid @RequestBody EmployeePatchDTO patchDTO) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, patchDTO));
    }
}
