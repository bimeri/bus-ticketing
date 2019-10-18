package net.gowaka.gowaka.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import net.gowaka.gowaka.exception.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Edward Tanko <br/>
 * Date: 5/29/19 8:58 PM <br/>
 */

@Component
public class JwtTokenProvider {

  @Value("${security.jwt.token.publicKey:secret-key}")
  private String publicKeyEncoded;

  private PublicKey publicKey;

  @Value("${security.jwt.token.expireLength:3600000}")
  private long validityInMilliseconds = 3600000;

  private Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

  @PostConstruct
  protected void init() {
    X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyEncoded));
    try {
      KeyFactory keyFactory = KeyFactory.getInstance("RSA");
      publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (InvalidKeySpecException e) {
      e.printStackTrace();
    }
  }

  public Authentication getAuthentication(String token) {
    UserDetails userDetails = getUserDetails(token);
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public String resolveToken(HttpServletRequest req) {
    String bearerToken = req.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      throw new AuthorizationException("Expired or invalid JWT token");
    }
  }

  public UserDetailsImpl getUserDetails(String token) {
    Claims claims = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token).getBody();

    String username = claims.getSubject();
    String userId = claims.get("id").toString();
    String userFullName = claims.get("fullName").toString();
    String auth = claims.get("auth").toString();
    List<String> privileges;

    try {
      privileges = new ObjectMapper().readValue(auth, new TypeReference<List<String>>(){});
    } catch (IOException e) {
      throw new AuthorizationException("Expired or invalid JWT token");
    }

    logger.info("{}",privileges);

    List<GrantedAuthority> appGrantedAuthorities = privileges.stream()
            .map(privilege -> new AppGrantedAuthority("ROLE_"+privilege))
            .collect(Collectors.toList());

    UserDetailsImpl user = new UserDetailsImpl();
    user.setId(userId);
    user.setUsername(username);
    user.setFullName(userFullName);
    user.setAuthorities(appGrantedAuthorities);

    logger.info("{}",user);

    return user;
  }


}