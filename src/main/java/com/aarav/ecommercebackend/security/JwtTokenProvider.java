package com.aarav.ecommercebackend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    public String generateJwtToken(Authentication auth){
        String email = auth.getName();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 900000);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getKey())
                .compact();
    }

    private Key getKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getEmailFromJwt(String token) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            System.out.println("Cannot get username from token: " + e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith((SecretKey) getKey())
                    .build()
                    .parse(token);
            return true;
        } catch (Exception e){
            System.out.println("Invalid token");
            System.out.println(e.getMessage());
            return false;
        }
    }
}
