package com.rfid.tracker.config;

import com.rfid.tracker.filter.JwtRequestFilter;

import com.rfid.tracker.service.MyUserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


/**

* Security Configuration with Enhanced CORS Support

*

* ✅ CORS already configured for React (port 5173)

* ✅ No need for separate CorsConfig.java

* ✅ Mind map endpoints allowed (no auth required for public access)

* ✅ Study assistant endpoints protected (requires STUDENT or ADMIN)

*/

@Configuration

@EnableWebSecurity

public class SecurityConfig {

    @Autowired

    private MyUserDetailsService myUserDetailsService;

    @Autowired

    private JwtRequestFilter jwtRequestFilter;

    // ✅ General password encoder for Spring Security

    @Bean

    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();

    }

    // ✅ Explicit BCrypt bean for AuthService (bCryptPasswordEncoder field)

    @Bean

    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();

    }

    @Bean

    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {

        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder());

        return authBuilder.build();
        
    }

    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(authz -> authz

                // ✅ HARDWARE/RFID/BIOMETRIC - ESP32 (NO AUTH REQUIRED)

                .requestMatchers("/api/attendance/rfid/**").permitAll()
                .requestMatchers("/api/attendance/fingerprint/**").permitAll()

                // ✅ NEW: LOGGING ENDPOINTS FOR STAGING LOGIC (Added for 7-period rule)

                .requestMatchers("/api/attendance/log/**").permitAll()
                .requestMatchers("/api/attendance/biometric/**").permitAll()

                .requestMatchers("/api/staff/rfid/**").permitAll()

                .requestMatchers("/api/students/rfid/**").permitAll()

                // ✅ ADDED: VERIFICATION CODE ENDPOINT (FOR STUDENT ATTENDANCE MARKING)

                .requestMatchers("/api/attendance/verify-code").permitAll()

                // ✅ AUTHENTICATION ENDPOINTS

                .requestMatchers("/api/auth/**").permitAll()

                .requestMatchers("/api/login").permitAll()

                .requestMatchers("/login").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ✅ PUBLIC ENDPOINTS

                .requestMatchers("/api/sections/**").permitAll()

                .requestMatchers("/api/sections/branches").permitAll()
                .requestMatchers("/api/hardware/**").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/staff/by-branch/**").permitAll()

                // ✅ TEST ENDPOINT (TEMPORARY ALLOW)

                .requestMatchers("/api/staff/test/trigger-late-check").permitAll()

                // ✅ MIND MAP ENDPOINTS (PUBLIC - NO AUTH REQUIRED) - FOR STUDY ASSISTANT

                .requestMatchers("/api/mindmap/**").permitAll()

                .requestMatchers(HttpMethod.POST, "/api/mindmap/generate-from-youtube").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/mindmap/health").permitAll()

                .requestMatchers(HttpMethod.GET, "/api/mindmap/test/**").permitAll()

                // ✅ ACTUATOR (Monitoring)

                .requestMatchers("/actuator/**").permitAll()

                .requestMatchers("/error").permitAll()

                // ✅ ATTENDANCE ENDPOINTS (student & admin)

                .requestMatchers("/api/attendance/student/**").hasAnyAuthority("STUDENT", "ADMIN")

                .requestMatchers("/api/attendance/view/**").hasAnyAuthority("STUDENT", "ADMIN")

                .requestMatchers("/api/attendance/month/**").hasAnyAuthority("STUDENT", "ADMIN")

                .requestMatchers("/api/attendance/mark").hasAuthority("ADMIN")

                // ✅ STUDY ASSISTANT ENDPOINTS (PROTECTED - Requires STUDENT or ADMIN)

                .requestMatchers("/api/study-assistant/**").hasAnyAuthority("STUDENT", "ADMIN")

                // ✅ ADMIN ENDPOINTS (JWT required)

                .requestMatchers("/api/staff/**").hasAuthority("ADMIN")

                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                // ✅ STUDENT ENDPOINTS (JWT required)

                .requestMatchers("/api/students/**").hasAnyAuthority("STUDENT", "ADMIN")

                // ✅ ALL OTHER ENDPOINTS REQUIRE AUTH

                .anyRequest().authenticated()

            )

            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

    /**

     * ✅ ENHANCED CORS CONFIGURATION

     *

     * Already configured in SecurityConfig - no need for separate CorsConfig.java

     * This provides:

     * - React frontend (localhost:5173) can call Java backend

     * - Credentials (auth headers, cookies) sent with requests

     * - All HTTP methods supported (GET, POST, PUT, DELETE, OPTIONS, PATCH)

     * - Proper CORS headers in responses

     */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Allow React frontend

        configuration.setAllowedOriginPatterns(List.of(
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://localhost:3000",
        "https://*.vercel.app",
        "https://*.ngrok-free.dev"
    ));
        // ✅ Allow all HTTP methods

        configuration.setAllowedMethods(Arrays.asList(

            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"

        ));

        // ✅ Allow all headers (including Authorization)

        configuration.setAllowedHeaders(Arrays.asList("*"));

        // ✅ CRITICAL: Allow credentials (auth tokens, cookies)

        configuration.setAllowCredentials(true);

        // ✅ Cache CORS validation

        configuration.setMaxAge(3600L);

        // Register configuration for all endpoints

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;

    }

}
