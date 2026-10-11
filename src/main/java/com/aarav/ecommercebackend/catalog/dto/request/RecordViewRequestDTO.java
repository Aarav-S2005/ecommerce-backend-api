package com.aarav.ecommercebackend.catalog.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordViewRequestDTO {

    @NotNull(message = "Duration in milliseconds is required")
    @Min(value = 0, message = "Duration cannot be negative")
    private Long durationMs;
}
