package com.tasks.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    public String generateToken(UserDetails userDetails) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        List<String> roles = new ArrayList<>();
        if(userDetails.getAuthorities() != null) {
            userDetails.getAuthorities().forEach((authority) -> {
                roles.add(authority.getAuthority());
            });
        }
        
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("authorities", roles)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, Base64.getEncoder().encodeToString(jwtSecret.getBytes())).compact();
    }

    public UsernamePasswordAuthenticationToken getUser(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(Base64.getEncoder().encodeToString(jwtSecret.getBytes()))
                    .parseClaimsJws(token)
                    .getBody();
            List<SimpleGrantedAuthority> roles = new ArrayList<>();
            ((List<String>) claims.get("authorities")).forEach((authority) -> {
                roles.add(new SimpleGrantedAuthority(authority));
            });
            return new UsernamePasswordAuthenticationToken(claims.getSubject(), null, roles);
        } catch(Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;
    }

    public void validateToken(String authToken) {
        Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
    }
    
}
