package com.example.demo.filter;

import java.io.IOException;
import java.util.Optional;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

@WebFilter(urlPatterns = {"/*"})
public class AuthenticationFilter implements Filter {

    private final AuthService authService;
    private final UserRepository userRepo;

    public AuthenticationFilter(AuthService authService, UserRepository userRepo) {
        this.authService = authService;
        this.userRepo = userRepo;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        

        // ✅ PUBLIC APIs (no auth required)
        if (uri.startsWith("/api/auth/login") ||
            uri.startsWith("/api/users/register")|| uri.startsWith("/api/payment/verify")) {

            chain.doFilter(request, response);
            return;
        }

        // 🔥 GET TOKEN FROM COOKIE
        String token = null;

        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("authToken".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        // ❌ TOKEN MISSING OR INVALID
        if (token == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\":\"Token missing\"}");
            return;
        }

        if (!authService.validateToken(token)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\":\"Invalid token\"}");
            return;
        }

        // 🔥 EXTRACT USER
        String username = authService.extractUsername(token);
        

        Optional<User> userOpt = userRepo.findByUsername(username);

        if (userOpt.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\":\"User not found\"}");
            return;
        }

        User user = userOpt.get();

        // 🔥 ADMIN CHECK
        if (uri.startsWith("/admin/") && user.getRole() != Role.ADMIN) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.getWriter().write("{\"error\":\"Access denied\"}");
            return;
        }

        // 🔥 PASS USER TO CONTROLLERS (optional but useful)
        req.setAttribute("authenticatedUser", user);

        chain.doFilter(request, response);
    }
}