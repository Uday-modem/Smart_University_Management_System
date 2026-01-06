package com.rfid.tracker.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final String SECRET_STRING = "YourVeryLongAndSuperSecretKeyForHS256SignatureAlgorithmThatIsSecure";
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    // ===== TOKEN EXTRACTION METHODS =====

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ===== VALIDATION METHODS =====

    // Old method - with UserDetails (backward compatible)
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // ✅ NEW: Single parameter validation (for AuthController - line 200)
    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            System.out.println("❌ Token validation failed: " + e.getMessage());
            return false;
        }
    }

    // ===== TOKEN GENERATION METHODS =====

    // ✅ NEW: 5-parameter method for student login (for AuthController - line 98)
    /**
     * Generate JWT token with full student details
     * Used by: AuthController.studentLogin()
     * Parameters:
     *   - email: Student email
     *   - studentId: Student ID
     *   - role: User role (STUDENT)
     *   - name: Student name
     *   - registrationNumber: Student registration number
     */
    public String generateToken(String email, String studentId, String role, String name, String registrationNumber) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("studentId", studentId);
        claims.put("name", name);
        claims.put("registrationNumber", registrationNumber);
        return createToken(claims, email);
    }

    // Original method - 2 parameters (backward compatible)
    /**
     * Generate JWT token with basic info
     * Used by: Legacy code, admin login
     */
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, email);
    }

    // ===== PRIVATE: TOKEN CREATION =====

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24)) // 24 hours
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // ===== UTILITY METHODS =====

    public String getEmailFromToken(String token) {
        return extractUsername(token);
    }

    public String getClaimFromToken(String token, String claimName) {
        Claims claims = extractAllClaims(token);
        return claims.get(claimName, String.class);
    }

    public Object getObjectClaimFromToken(String token, String claimName) {
        Claims claims = extractAllClaims(token);
        return claims.get(claimName);
    }
}