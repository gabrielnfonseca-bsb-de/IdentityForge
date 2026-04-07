package com.gabriel.identityforge.infrastructure.security;

import com.gabriel.identityforge.domain.port.out.TokenProviderPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class JwtProvider implements TokenProviderPort {

    private final SecretKey secretKey;

    public JwtProvider(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(UUID userId, List<String> roles) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("roles", roles)
                .claim("type", "access") // 🔥 importante
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 15)) // 15 min
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("type", "refresh") // 🔥 importante
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)) // 7 dias
                .signWith(secretKey)
                .compact();
    }

    @Override
    public UUID extractUserId(String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    @Override
    public List<String> extractRoles(String token) {
        Claims claims = parseToken(token);
        List<String> roles = claims.get("roles", List.class);
        return roles != null ? roles : List.of();
    }

    // 🔥 MÉTODO CENTRALIZADO (evita duplicação)
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}

