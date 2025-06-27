package org.yearup.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders; // Needed to decode the Base64 secret
import io.jsonwebtoken.security.Keys; // Needed to create Key from bytes
import io.jsonwebtoken.ExpiredJwtException; // For specific exception handling
import io.jsonwebtoken.MalformedJwtException; // For specific exception handling
import io.jsonwebtoken.UnsupportedJwtException; // For specific exception handling
import io.jsonwebtoken.security.SecurityException; // For specific exception handling

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key; // java.security.Key
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class TokenProvider implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";

    // This will hold the secret string value from application.properties
    private final String secretString;
    private final long tokenTimeout;

    // This will hold the actual java.security.Key object used for signing/verification
    private Key key;

    // Constructor to inject properties from application.properties
    public TokenProvider(
            @Value("${jwt.secret}") String secretString, // Inject the secret string
            @Value("${jwt.token-timeout-seconds}") long tokenTimeoutSeconds)
    {
        this.secretString = secretString;
        this.tokenTimeout = tokenTimeoutSeconds * 1000; // Convert to milliseconds
    }

    @Override
    public void afterPropertiesSet() {
        // IMPORTANT: Decode the Base64 secret string from properties into bytes,
        // then create the Key object from those bytes.
        // This ensures the key is consistent with what's configured.
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secretString);
            this.key = Keys.hmacShaKeyFor(keyBytes); // Create the key from the decoded bytes
            logger.info("JWT secret key initialized from application.properties.");
        } catch (IllegalArgumentException e) {
            // This catches errors if the secretString is not a valid Base64 string
            logger.error("Invalid JWT secret in application.properties. It must be a valid Base64 string. Error: {}", e.getMessage());
            // Fallback to a generated key if configured secret is invalid, for robustness in dev.
            // In production, you might want to throw an exception and prevent startup.
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            logger.warn("Using a randomly generated JWT secret key as fallback due to invalid configured secret.");
        }
    }

    public String createToken(Authentication authentication) { // Removed rememberMe if not used
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenTimeout);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512) // Use the 'key' initialized from properties
                .setExpiration(validity)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key) // Use the 'key' initialized from properties
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken); // Use the 'key' initialized from properties
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            logger.info("Invalid JWT signature.", e); // Log the specific type of exception
        } catch (ExpiredJwtException e) {
            logger.info("Expired JWT token.", e);
        } catch (UnsupportedJwtException e) {
            logger.info("Unsupported JWT token.", e);
        } catch (IllegalArgumentException e) {
            logger.info("JWT token compact of handler are invalid.", e);
        }
        return false;
    }
}