package com.team.app.controller;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String context = request.getContextPath();
        String path = request.getRequestURI().substring(context.length());

        // Allow public endpoints
        boolean isPublic = path.equals("/")
                || path.startsWith("/login")
                || path.startsWith("/register")
                || path.startsWith("/assets/")
                || path.startsWith("/health/");

        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("user") != null);

        if (!loggedIn && !isPublic) {
            response.sendRedirect(context + "/login");
            return;
        }

        // If logged in and visiting root or login/register, go to dashboard
        if (loggedIn && (path.equals("/") || path.startsWith("/login") || path.startsWith("/register"))) {
            response.sendRedirect(context + "/dashboard");
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() { }
}


