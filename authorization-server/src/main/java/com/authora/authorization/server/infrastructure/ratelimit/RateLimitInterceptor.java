package com.authora.authorization.server.infrastructure.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method)) {
            return true;
        }

        String path = request.getRequestURI();
        if (!path.equals("/sign-in") && !path.equals("/sign-up")) {
            return true;
        }

        String ip = resolveClientIp(request);
        String endpoint = path.replace("/", "");

        if (!rateLimitService.isAllowed(ip, endpoint)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("text/html;charset=UTF-8");

            // Redirect back to the page with an error param so FreeMarker can show it
            String clientId = request.getParameter("clientId");
            String redirectUrl = path + "?error=ratelimit";
            if (clientId != null && !clientId.isBlank()) {
                redirectUrl += "&client_id=" + clientId;
            }
            response.sendRedirect(redirectUrl);
            return false;
        }

        return true;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}
