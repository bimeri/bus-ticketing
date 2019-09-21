package net.gowaka.gowaka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Base64;
import java.util.Date;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/21/19 3:19 PM <br/>
 */
public class TestUtils {


    public static String createToken(String userId, String email, String fullName, String secretKey, String... roles) throws JsonProcessingException {
        Claims claims = Jwts.claims().setSubject(email);

        claims.put("auth", new ObjectMapper().writeValueAsString(roles));
        claims.put("id", userId);
        claims.put("fullName", fullName);
        claims.put("appName", "GoWaka");
        claims.put("grant_type", "user_profile");

        Date now = new Date();
        long expiredMillis = (now.getTime() + 100000);
        Date validity = new Date(expiredMillis);
        String key = Base64.getEncoder().encodeToString(secretKey.getBytes());
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, key)
                .compact();
    }
}
