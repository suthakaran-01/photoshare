package com.photoshare.dto;

import jakarta.validation.constraints.Pattern;

public record PinRequest(@Pattern(regexp = "\\d{6}", message = "must be a 6-digit PIN") String pin) {}