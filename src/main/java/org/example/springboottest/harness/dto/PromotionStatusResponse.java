package org.example.springboottest.harness.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PromotionStatusResponse(
        @JsonProperty(required = true)
        @JsonPropertyDescription("The unique employee ID")
        int employeeId,

        @JsonProperty(required = true)
        @JsonPropertyDescription("The employee's desired title to be promoted to")
        String desiredTitle,

        @JsonProperty(required = true)
        @JsonPropertyDescription("Whether the promotion request was approved for a promotion")
        boolean approved,

        @JsonProperty(required = true)
        @JsonPropertyDescription("The reason why the employee was or was not approved for a promotion")
        String reason
) {
}
