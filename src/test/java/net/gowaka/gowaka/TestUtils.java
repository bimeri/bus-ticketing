package net.gowaka.gowaka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

/**
 * Author: Edward Tanko <br/>
 * Date: 9/21/19 3:19 PM <br/>
 */
public class TestUtils {


    public static String createToken(String userId, String email, String fullName, String encodedPrivateKey, String... roles) throws JsonProcessingException {

        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(encodedPrivateKey));
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        Claims claims = Jwts.claims().setSubject(email);

        claims.put("auth", new ObjectMapper().writeValueAsString(roles));
        claims.put("id", userId);
        claims.put("fullName", fullName);
        claims.put("appName", "GoWaka");
        claims.put("grant_type", "user_profile");

        Date now = new Date();
        long expiredMillis = (now.getTime() + 100000);
        Date validity = new Date(expiredMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }
}
