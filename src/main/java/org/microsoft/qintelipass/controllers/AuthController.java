package org.microsoft.qintelipass.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.microsoft.qintelipass.CredentialManager;
import org.microsoft.qintelipass.ILoginStrategy;
import org.microsoft.qintelipass.IRegisterable;
import org.microsoft.qintelipass.LoginStrategyFactory;
import org.microsoft.qintelipass.configs.AdminProperties;
import org.microsoft.qintelipass.dtos.UserDTO;
import org.microsoft.qintelipass.entity.User;
import org.microsoft.qintelipass.dtos.request.LoginRequest;
import org.microsoft.qintelipass.dtos.request.RegisterRequest;
import org.microsoft.qintelipass.dtos.response.ResponseBody;
import org.microsoft.qintelipass.services.chat.ConversationService;
import org.microsoft.qintelipass.services.auth.SmsServiceImpl;
import org.microsoft.qintelipass.services.user.UserDetailsServiceImpl;
import org.microsoft.qintelipass.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("api/v1/auth/portal")
public class AuthController {
    private final LoginStrategyFactory factory;
    private final SmsServiceImpl smsService;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final CredentialManager credentialManager;
    private static final String COOKIE_ROOT = "/";
    private static final String PASSWORD_LOGIN_TYPE = "password";
    private static final int MAX_PASSWORD_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(30);
    private final Map<String, AttemptInfo> passwordAttemptCache = new ConcurrentHashMap<>();

    @Autowired
    private IRegisterable registerService;
    private final AdminProperties adminProperties;

    @Autowired
    public AuthController(LoginStrategyFactory factory, SmsServiceImpl smsService, JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService, CredentialManager credentialManager, ConversationService conversationService, AdminProperties adminProperties) {
        this.factory = factory;
        this.smsService = smsService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.credentialManager = credentialManager;
        this.adminProperties = adminProperties;
    }

    @CrossOrigin
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest formData, HttpServletResponse httpResponse) {
        log.info("User response: {}", formData);
        try {
            String loginType = formData.getLoginType();
            Map<String, Object> params = formData.getCredential();
            ILoginStrategy strategy = factory.getStrategy(loginType);

            // 密码登录的失败次数限制
            boolean isPasswordLogin = PASSWORD_LOGIN_TYPE.equals(loginType);
            String phone = isPasswordLogin && params != null ? (String) params.get("phone") : null;
            if (isPasswordLogin && phone != null && isAccountLocked(phone)) {
                long retryAfter = getRemainingLockMinutes(phone);
                log.warn("Account locked due to too many failed password attempts: {}", phone);
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "密码错误次数过多，账户已被锁定，请" + retryAfter + "分钟后再试",
                                "locked", true,
                                "retry_after_minutes", retryAfter
                        ));
            }

            ResponseBody<User> response = strategy.authenticate(params);
            User user = response.getPayload();
            if (response.isSuccess() && user != null) {
                if (isPasswordLogin && phone != null) {
                    clearFailedAttempts(phone);
                }
                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getName());
                String token = jwtUtil.generateToken(userDetails);
                ResponseCookie auth = ResponseCookie.from("access_token", token)
                        .httpOnly(true)
                        .sameSite("Lax")
                        .path(COOKIE_ROOT)
                        .maxAge(Duration.ofDays(7))
                        .build();
                httpResponse.addHeader(HttpHeaders.SET_COOKIE, auth.toString());
                String role = adminProperties.isAdmin(user.getPhone()) ? "ADMIN" : "USER";

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "access_token", token,
                                "role", role,
                                "data", UserDTO.fromUser(user)
                        )
                );
            }

            // 密码登录失败，记录失败次数
            if (isPasswordLogin && phone != null) {
                int remaining = recordFailedAttempt(phone);
                if (remaining == 0) {
                    return ResponseEntity
                            .badRequest()
                            .body(Map.of(
                                    "success", false,
                                    "message", "密码错误次数过多，账户已被锁定" + LOCK_DURATION.toMinutes() + "分钟",
                                    "locked", true,
                                    "retry_after_minutes", LOCK_DURATION.toMinutes()
                            ));
                }
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "密码错误，还剩" + remaining + "次尝试机会",
                                "attempts_remaining", remaining
                        ));
            }
            return ResponseEntity.badRequest().body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 判断账号是否因密码错误次数过多被锁定
     */
    private boolean isAccountLocked(String phone) {
        AttemptInfo info = passwordAttemptCache.get(phone);
        if (info == null || info.lockedUntil <= 0) {
            return false;
        }
        if (System.currentTimeMillis() >= info.lockedUntil) {
            // 锁定已过期，清理缓存
            passwordAttemptCache.remove(phone);
            return false;
        }
        return true;
    }

    /**
     * 获取剩余锁定时间（分钟，向上取整）
     */
    private long getRemainingLockMinutes(String phone) {
        AttemptInfo info = passwordAttemptCache.get(phone);
        if (info == null || info.lockedUntil <= 0) {
            return 0;
        }
        long remainingMillis = info.lockedUntil - System.currentTimeMillis();
        return Math.max(1, (remainingMillis + 60_000 - 1) / 60_000);
    }

    /**
     * 记录一次密码登录失败，返回剩余尝试次数；返回 0 表示已触发锁定
     */
    private int recordFailedAttempt(String phone) {
        AttemptInfo info = passwordAttemptCache.computeIfAbsent(phone, k -> new AttemptInfo());
        synchronized (info) {
            info.count++;
            if (info.count >= MAX_PASSWORD_ATTEMPTS) {
                info.lockedUntil = System.currentTimeMillis() + LOCK_DURATION.toMillis();
                log.warn("Password attempt limit reached for phone: {}, locked for {} minutes", phone, LOCK_DURATION.toMinutes());
                return 0;
            }
            return MAX_PASSWORD_ATTEMPTS - info.count;
        }
    }

    /**
     * 登录成功后清除失败记录
     */
    private void clearFailedAttempts(String phone) {
        passwordAttemptCache.remove(phone);
    }

    /**
     * 密码登录失败次数追踪信息
     */
    private static class AttemptInfo {
        int count = 0;
        long lockedUntil = 0; // epoch millis，0 表示未锁定
    }

    @PostMapping("/send_code")
    public ResponseEntity<?> sendCode(@RequestBody Map<String, String> payload) {
        // 兼容大小写参数
        String phone = payload.getOrDefault("phone", payload.get("Phone"));
        if (phone == null || phone.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "手机号不能为空"));
        }

        // 校验手机号格式：必须是11位且以1开头
        if (!phone.matches("^1\\d{10}$")) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("success", false, "message", "请输入正确的手机号码"));
        }

        // 检查60秒冷却时间
        if (smsService.isInCooldown(phone)) {
            long remaining = smsService.getCooldownRemaining(phone);
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "请" + remaining + "秒后再获取验证码",
                            "cooldown", remaining
                    ));
        }

        smsService.sendSmsCode(phone);
        log.info("Sent sms code to phone: {}", phone);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "验证码已发送，5分钟内有效"
        ));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse httpResponse, @RequestHeader("Authorization") String token) {
        if (!credentialManager.checkIfLogin(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not Logged in.");
        }
        Cookie userIdCookie = new Cookie("user_id", "");
        Cookie auth = new Cookie("access_token", "");
        userIdCookie.setPath("/");
        userIdCookie.setMaxAge(0);
        auth.setMaxAge(0);
        auth.setPath("/");
        httpResponse.addCookie(userIdCookie);
        httpResponse.addCookie(auth);

        return ResponseEntity.ok(Map.of("success", true, "message", "OK"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest payload) {
        User registered;
        try {
            registered = registerService.register(payload, payload.getPassword());
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
        Map<String, Object> responseBody = new HashMap<>();

        if (registered != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(registered.getName());
            String token = jwtUtil.generateToken(userDetails);
            responseBody.put("success", true);
            responseBody.put("data", registered);
            responseBody.put("token", token);

            return ResponseEntity.created(ServletUriComponentsBuilder
                            .fromCurrentRequest()
                            .path("/{id}")
                            .buildAndExpand(registered.getId())
                            .toUri())
                    .body(responseBody);
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", "Information is not completed, cloud not register."
                    ));

        }
    }
}