package com.photoshare.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record EventRequest(@NotBlank String name, LocalDate eventDate) {}