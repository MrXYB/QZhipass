package org.microsoft.qintelipass.services.user;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.microsoft.qintelipass.exceptions.UnauthorizedException;
import org.microsoft.qintelipass.services.UserService;
import org.microsoft.qintelipass.services.auth.AuthTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class CurrentUserService {
    private static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final AuthTokenService authTokenService;
    private final UserService userService;

    @Value("${app.dev-user-id:}")
    private String devUserId;

    public CurrentUserService(AuthTokenService authTokenService, UserService userService) {
        this.authTokenService = authTokenService;
        this.userService = userService;
    }

    public Long requireUserId(HttpServletRequest request) {
        if (StringUtils.hasText(devUserId)) {
            try {
                return Long.parseLong(devUserId.trim());
            } catch (NumberFormatException exception) {
                throw new IllegalStateException("app.dev-user-id must be a numeric user id.");
            }
        }

        String token = resolveToken(request)
                .orElseThrow(() -> new UnauthorizedException("Missing access token."));
        Long userId = authTokenService.resolveUserId(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid or expired access token."));

        if (userService.isUserDeactivated(userId)) {
            throw new UnauthorizedException("Your account has been cancelled.");
        }
        if (userService.isUserFrozen(userId)) {
            throw new UnauthorizedException(
                    "\u60a8\u7684\u8d26\u6237\u5df2\u51bb\u7ed3\uff0c\u8bf7\u8054\u7cfb\u7ba1\u7406\u5458");
        }
        return userId;
    }

    private Optional<String> resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return Optional.of(authorization.substring(7).trim());
        }

        String tokenHeader = request.getHeader("X-Access-Token");
        if (StringUtils.hasText(tokenHeader)) {
            return Optional.of(tokenHeader.trim());
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                    return Optional.of(cookie.getValue().trim());
                }
            }
        }

        return Optional.empty();
    }
}