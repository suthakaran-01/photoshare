package com.photoshare.dto;

import com.photoshare.entity.User;

public record MemberResponse(Long id, String name, String email) {
    public static MemberResponse from(User u) {
        return new MemberResponse(u.getId(), u.getName(), u.getEmail());
    }
}