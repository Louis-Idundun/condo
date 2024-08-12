package com.condo.condo.dtos;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;


public record PropertyDto(
        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,
        @NotBlank(message = "Address is required")
        @Size(max = 200, message = "Address must not exceed 200 characters")
        String address,
        @NotNull(message = "Room count is required")
        @Min(value = 1, message = "Room count must be at least 1")
        Integer roomCount
) {
}
