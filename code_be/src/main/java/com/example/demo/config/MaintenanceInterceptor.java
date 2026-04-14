package com.example.demo.config;

import com.example.demo.entity.SystemConfig;
import com.example.demo.repository.SystemConfigRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor

public class MaintenanceInterceptor implements HandlerInterceptor {
    private final SystemConfigRepository systemConfigRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        //MAINTENANCE_MODE từ database
        boolean isMaintenance = systemConfigRepository.findById("MAINTENANCE_MODE")
                .map(SystemConfig::getValue)
                .map(Boolean::parseBoolean)
                .orElse(false);
        if (isMaintenance) {
            //thông tin đăng nhập
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            //quyền ADMIN
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));

            String uri = request.getRequestURI();

            if (!isAdmin &&
                    !uri.startsWith("/admin") &&
                    !uri.startsWith("/login") &&
                    !uri.startsWith("/css") &&
                    !uri.startsWith("/js") &&
                    !uri.startsWith("/images") &&
                    !uri.equals("/maintenance")) {
                // Chuyển hướng về trang thông báo bảo trì
                response.sendRedirect("/maintenance");
                return false; //Chặn Controller
            }
        }
        return true; //không ở chế độ bảo trì hoặc là Admin
    }
}