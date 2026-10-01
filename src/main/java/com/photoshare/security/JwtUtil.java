package com.photoshare.security;

import com.photoshare.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generate(String email, Role role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }
    
    public String generateGalleryAccess(String galleryToken) {
        return Jwts.builder()
                .subject("gallery:" + galleryToken)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000))
                .signWith(key)
                .compact();
    }

    public boolean isValidGalleryAccess(String accessToken, String galleryToken) {
        try {
            String subject = parse(accessToken).getSubject();
            return ("gallery:" + galleryToken).equals(subject);
        } catch (Exception e) {
            return false;
        }
    }
}