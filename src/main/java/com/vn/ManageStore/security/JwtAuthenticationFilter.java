package com.vn.ManageStore.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vn.ManageStore.service.TokenBlacklistService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Skip JWT filter for public auth endpoints
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        logger.info("=== JwtAuthenticationFilter START: {} {} ===", method, requestPath);
        
        if (requestPath.startsWith("/api/auth/")) {
            logger.info("✓ SKIPPING JWT validation for auth endpoint: {} {}", method, requestPath);
            filterChain.doFilter(request, response);
            logger.info("=== JwtAuthenticationFilter END (after skip): {} {} ===", method, requestPath);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            logger.info("Token extracted from Authorization header");
        } else {
            logger.info("No Bearer token found in Authorization header");
        }

        if (token == null) {
            logger.debug("No Authorization header present for request: {}", requestPath);
        } else if (!jwtUtil.validate(token)) {
            logger.debug("JWT validation failed for request: {}", requestPath);
        } else if (tokenBlacklistService.isBlacklisted(token)) {
            logger.debug("JWT is blacklisted for request: {}", requestPath);
        } else {
            // token valid and not blacklisted
            try {
                String username = jwtUtil.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.debug("Set SecurityContext for user: {}", username);
                }
            } catch (Exception ex) {
                logger.warn("Failed to set authentication from JWT: {}", ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
        logger.info("=== JwtAuthenticationFilter END: {} {} ===", method, requestPath);
    }
}
