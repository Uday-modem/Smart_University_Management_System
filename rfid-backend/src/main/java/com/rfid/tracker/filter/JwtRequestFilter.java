package com.rfid.tracker.filter;

import com.rfid.tracker.service.MyUserDetailsService;
import com.rfid.tracker.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        String role = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                // ✅ CRITICAL FIX: Extract the role directly from the token
                role = jwtUtil.getClaimFromToken(jwt, "role");
            } catch (Exception e) {
                System.out.println("❌ JWT Token validation error: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                // ✅ CRITICAL FIX: Use the role from the token if available, otherwise fallback to userDetails
                // This ensures that if the DB load doesn't set it right, the token definitely does.
                List<SimpleGrantedAuthority> authorities;
                
                if (role != null && !role.isEmpty()) {
                    // Ensure role has no ROLE_ prefix unless your config expects it (Standard "STUDENT" is fine here)
                    authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
                } else {
                    authorities = (List<SimpleGrantedAuthority>) userDetails.getAuthorities();
                }

                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                System.out.println("✅ Authenticated User: " + username + " | Role: " + authorities);
            }
        }
        chain.doFilter(request, response);
    }
}
