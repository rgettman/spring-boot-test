package org.example.springboottest.harness.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record PromotionStatusRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("The unique employee ID")
        int employeeId,

        @JsonProperty(required = true)
        @JsonPropertyDescription("The employee's desired title to be promoted to")
        String desiredTitle
) { }
