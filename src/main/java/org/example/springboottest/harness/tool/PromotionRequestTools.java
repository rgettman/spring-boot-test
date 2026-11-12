package org.example.springboottest.harness.tool;

import java.util.List;
import java.util.Random;

import org.example.springboottest.harness.dto.PromotionStatusRequest;
import org.example.springboottest.harness.dto.PromotionStatusResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class PromotionRequestTools {

    private static final Logger log = LoggerFactory.getLogger(PromotionRequestTools.class);

    private static final List<String> REJECTED_REASONS = List.of(
            "There is no budget for a promotion and a raise.",
            "The employee has not demonstrated the capabilities necessary for the promotion.",
            "The employee is being quiet-fired."
    );
    private static final Random RANDOM = new Random();

    /**
     * A deterministic method of performing the business logic.  This is called
     * BY THE MODEL.  A good description helps the model determine that it
     * needs to call this tool.
     * @param request Constructed, hopefully correctly, by the model.
     * @return A PromotionStatusResponse.
     */
    @Tool(description = "Queries the internal promotion request records to determine whether an employee's promotion request is approved")
    public PromotionStatusResponse checkPromotionStatus(PromotionStatusRequest request) {
        log.trace("BEGIN checkPromotionStatus");

        // Validation is a guardrail.
        if (request.desiredTitle() == null || request.desiredTitle().isEmpty()) {
            return new PromotionStatusResponse(request.employeeId(), request.desiredTitle(), false, "Desired title is missing or blank!");
        }

        log.info("Checking promotion status for employee ID {}", request.employeeId());

        // Business logic here.  This is just a mock-up.  The real check would
        // involve connecting to another system or database.
        try {
            boolean approved = RANDOM.nextBoolean();
            String reason;
            if (approved) {
                reason = "The employee demonstrates the capabilities necessary for the promotion.";
            }
            else {
                reason = REJECTED_REASONS.get(RANDOM.nextInt(REJECTED_REASONS.size()));
            }
            return new PromotionStatusResponse(request.employeeId(), request.desiredTitle(), approved, reason);
        }
        catch (Exception e) {
            log.error("Harness error checking promotion status for employee ID {}", request.employeeId(), e);
            return new PromotionStatusResponse(request.employeeId(), request.desiredTitle(), false, e.getMessage());
        }
    }
}
