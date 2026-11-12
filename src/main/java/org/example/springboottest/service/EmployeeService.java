package org.example.springboottest.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.example.springboottest.dto.EmployeeCreateDTO;
import org.example.springboottest.dto.EmployeeDTO;
import org.example.springboottest.dto.EmployeePatchDTO;
import org.example.springboottest.entity.Employee;
import org.example.springboottest.exception.ResourceNotFoundException;
import org.example.springboottest.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final StringRedisTemplate redisTemplate;

    private static final Function<Employee, EmployeeDTO> MAPPER =
            emp -> new EmployeeDTO(emp.getId(), emp.getFirstName(), emp.getLastName(), emp.getPosition(), emp.getAge(), emp.getSalary());
    private static final String EMPLOYEE_KEY = "employees";
    private static final String ALL_EMPLOYEES_KEY = "allEmployees";
    // A counter that is part of every sort index key.  Bumping it makes all
    // existing indexes unreachable at once, without scanning for keys.
    private static final String INDEX_VERSION_KEY = EMPLOYEE_KEY + ":index:version";

    /**
     * Constructor-inject the Repository object.
     * @param employeeRepository Injected by Spring!
     * @param redisTemplate Injected by Spring!
     */
    public EmployeeService(EmployeeRepository employeeRepository, StringRedisTemplate redisTemplate) {
        this.employeeRepository = employeeRepository;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Use the repository to retrieve the data.  Map Entity objects to DTO
     * objects.
     * Use @Transactional(readOnly = true) for read-only DB
     * operations.
     * Use @Cacheable with a key along with @EnableCaching on Main.
     * Configure what cache to use with properties and dependencies.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = ALL_EMPLOYEES_KEY)
    public List<EmployeeDTO> getAllEmployees() {
        log.info("Getting all employees from the repository.");
        return employeeRepository.findAllEmployees().stream()
                .map(MAPPER)
                .toList();
    }

    /**
     * Use injected RedisTemplate
     * Read the index version BEFORE touching the index or the database.  If a
     * write commits while this method is rebuilding an index, the version moves
     * on, and the index built here lands under the old version key, which no
     * later request reads.  It simply expires.
     */
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getEmployeesPaginated(int page, int size, String sortBy, String direction) {
        log.info("Getting paginated employees for page {} and size {}.", page, size);

        String indexKey = String.format(EMPLOYEE_KEY + ":index:v%s:sortBy:%s:dir:%s",
                currentIndexVersion(), sortBy, direction);

        boolean hasKey = redisTemplate.hasKey(indexKey);
        if (!hasKey) {
            populateZSetIndex(indexKey, sortBy, direction);
        }
        long start = (long) (page - 1) * size;
        long end = start + size - 1;

        Set<String> idStrings = redisTemplate.opsForZSet().range(indexKey, start, end);
        long totalElements = redisTemplate.opsForZSet().zCard(indexKey);

        if (idStrings == null || idStrings.isEmpty()) {
            return Page.empty(PageRequest.of(page - 1, size));
        }

        List<Integer> ids = idStrings.stream().map(Integer::valueOf).toList();
        List<Employee> employees = employeeRepository.findAllByIdIn(ids);

        Map<Integer, EmployeeDTO> dtoMap = employees.stream()
                .collect(Collectors.toMap(Employee::getId, MAPPER));

        List<EmployeeDTO> content = ids.stream()
                .map(dtoMap::get)
                .filter(Objects::nonNull)
                .toList();

        return new PageImpl<>(content, PageRequest.of(page - 1, size), totalElements);
    }

    /**
     * Helper to safely fill the ZSet index from the repository contents.
     */
    private synchronized void populateZSetIndex(String indexKey, String sortBy, String direction) {
        if (redisTemplate.hasKey(indexKey)) {
            return;
        }
        log.info("Rebuilding Redis ZSET Employee Index from Database for key: {}", indexKey);
        List<Employee> all = employeeRepository.findAllEmployeesSorted(sortBy, direction);
        int sequence = 0;
        for (Employee emp : all) {
            redisTemplate.opsForZSet().add(indexKey, String.valueOf(emp.getId()), sequence);
            sequence++;
        }

        redisTemplate.expire(indexKey, Duration.ofMinutes(15));
    }

    /**
     * The service layer is responsible for throwing ResourceNotFoundException.
     * Use @Transactional at the service layer.  Only use it on public methods,
     * so that the proxy will intercept the method call.  Also, do not call
     * a @Transactional method from within the same class, because that will
     * bypass the proxy.
     * Pass a key in @Cacheable for one particular entry.
     * For the key to work properly, the "-parameters" flag must be passed in,
     * either via the Maven Compiler plugin, or IntelliJ compiler flags.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = EMPLOYEE_KEY, key = "#id")
    public EmployeeDTO getEmployeeById(int id) {
        log.info("Getting employee with id {} from the repository.", id);
        return employeeRepository.getEmployeeById(id)
                .map(MAPPER)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));
    }

    /**
     * Create and return the new ID.  If a DB error occurs, the DB layer
     * translates the SQLException into e.g., a DataIntegrityViolationException.
     * The SpringProxyInterceptor, which has already proxied the Service object,
     * catches such RuntimeExceptions and rolls back the transaction.
     * Use @CacheEvict to invalidate the allUsers cache, all entries.
     */
    @Transactional
    @CacheEvict(value = ALL_EMPLOYEES_KEY, allEntries = true)
    public int createEmployee(EmployeeCreateDTO createDTO) {
        log.info("Creating employee in the repository.");

        invalidateSortingIndexes();
        return employeeRepository.createEmployee(createDTO.getFirstName(), createDTO.getLastName(),
                createDTO.getPosition(), createDTO.getAge(), createDTO.getSalary());
    }

    /**
     * Fields other than PK fields are optional.  If null, don't update that field.
     * Use @Caching to supply multiple @CacheEvict annotations.  The "#id" syntax
     * refers to the parameter id.  Could have "#employee.id" if a parameter
     * "employee" had the property "id".
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ALL_EMPLOYEES_KEY, allEntries = true),
            @CacheEvict(value = EMPLOYEE_KEY, key = "#id")
    })
    public EmployeeDTO updateEmployee(int id, EmployeePatchDTO patchDTO) {
        log.info("Updating employee with id {} in the repository.", id);
        Employee existing = employeeRepository.getEmployeeById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (patchDTO.getFirstName() != null) {
            existing.setFirstName(patchDTO.getFirstName());
        }
        if (patchDTO.getLastName() != null) {
            existing.setLastName(patchDTO.getLastName());
        }
        if (patchDTO.getPosition() != null) {
            existing.setPosition(patchDTO.getPosition());
        }
        if (patchDTO.getAge() != null) {
            existing.setAge(patchDTO.getAge());
        }
        if (patchDTO.getSalary() != null) {
            existing.setSalary(patchDTO.getSalary());
        }
        invalidateSortingIndexes();
        employeeRepository.save(existing);
        return MAPPER.apply(existing);
    }

    /**
     * The current version of the sort indexes, "0" if none has been set yet.
     */
    private String currentIndexVersion() {
        String version = redisTemplate.opsForValue().get(INDEX_VERSION_KEY);
        return version == null ? "0" : version;
    }

    /**
     * Invalidate every sort index by bumping the version, but only AFTER the
     * current transaction commits.  Invalidating before the commit would let a
     * concurrent reader rebuild the index from the old, still-committed data
     * and keep it for the whole TTL.  Call this from any new write path.
     * The old version's keys are not deleted; they expire on their own TTL.
     */
    private void invalidateSortingIndexes() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    bumpIndexVersion();
                }
            });
        }
        else {
            // No transaction, so there is nothing to wait for.
            bumpIndexVersion();
        }
    }

    private void bumpIndexVersion() {
        try {
            redisTemplate.opsForValue().increment(INDEX_VERSION_KEY);
        }
        catch (RuntimeException e) {
            // The data is already committed, so don't fail the request.  The
            // indexes will be stale until their TTL expires.
            log.error("Could not bump the sort index version; indexes may be stale until they expire.", e);
        }
    }
}
