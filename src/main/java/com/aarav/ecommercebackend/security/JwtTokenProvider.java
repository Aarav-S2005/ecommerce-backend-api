package com.aarav.ecommercebackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    public String generateJwtToken(Authentication auth) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        UUID userId = principal.getId();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 900000); // 15 mins

        return Jwts.builder()
                .subject(userId.toString()) // UUID as the subject
                .claim("email", principal.getEmail())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getKey())
                .compact();
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public UUID getUserIdFromJwt(String token) {
        Claims claims = Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return UUID.fromString(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) getKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
