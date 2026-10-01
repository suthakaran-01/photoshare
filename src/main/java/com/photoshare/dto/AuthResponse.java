package com.photoshare.dto;

import com.photoshare.entity.Role;

public record AuthResponse(String token, Role role, String name) {}