package com.photoshare.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PublishRequest(@NotEmpty List<Long> photoIds) {}