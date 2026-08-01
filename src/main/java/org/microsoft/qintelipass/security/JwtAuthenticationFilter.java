package org.microsoft.qintelipass.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.microsoft.qintelipass.ITrafficStatService;
import org.microsoft.qintelipass.configs.AdminProperties;
import org.microsoft.qintelipass.enums.UserRole;
import org.microsoft.qintelipass.entity.User;
import org.microsoft.qintelipass.services.UserService;
import org.microsoft.qintelipass.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ITrafficStatService trafficStatService;
    private final AdminProperties adminProperties;

    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService,
                                   ITrafficStatService trafficStatService,
                                   AdminProperties adminProperties) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.trafficStatService = trafficStatService;
        this.adminProperties = adminProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable FilterChain filterChain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith(JwtUtil.BEARER_PREFIX)) {
            String jwt = authorizationHeader.substring(JwtUtil.BEARER_PREFIX.length());

            try {
                if (!jwtUtil.validateToken(jwt)) {
                    log.debug("JWT token validation failed (expired or invalid)");
                } else {
                    Long userId = null;
                    try {
                        userId = jwtUtil.extractUserId(jwt);
                    } catch (Exception e) {
                        log.debug("Could not extract user ID from token, will try to find by username");
                    }

                    String username = jwtUtil.extractUsername(jwt);
                    User user = null;

                    if (userId != null) {
                        user = userService.getUserById(userId);
                    }

                    if (user == null && username != null) {
                        user = userService.findByUsername(username);
                    }

                    if (user == null) {
                        log.warn("JWT token valid but user not found: userId={}, username={}", userId, username);
                    } else {
                        UserRole role = adminProperties.isAdmin(user.getPhone())
                                ? UserRole.ADMIN
                                : UserRole.USER;

                        AuthenticatedUser authenticatedUser = AuthenticatedUser.builder()
                                .userId(user.getId())
                                .username(user.getName())
                                .password(user.getPasswordHash())
                                .role(role)
                                .build();

                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities());
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        log.debug("User authenticated: userId={}, username={}", user.getId(), user.getName());

                        try {
                            trafficStatService.recordTraffic(user.getId());
                        } catch (Exception trafficEx) {
                            log.warn("Failed to record traffic for userId={}: {}", user.getId(), trafficEx.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("JWT token processing failed: {}", e.getMessage());
            }
        }

        if (filterChain != null) {
            filterChain.doFilter(request, response);
        }
    }
}
