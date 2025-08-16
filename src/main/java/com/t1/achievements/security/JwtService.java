package com.t1.achievements.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {
    private final Key key;
    private final long expirationSec;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expirationSec) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationSec = expirationSec;
    }

    public String generateToken(UserDetails user, String role, Map<String, Object> extra) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getUsername())
                .addClaims(extra == null ? Map.of() : extra)
                .claim("role", role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(expirationSec)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}

