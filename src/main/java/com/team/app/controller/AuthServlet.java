package com.team.app.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AuthServlet - Controller for authentication (login/register)
 * 
 * Handles:
 * - GET /login - Show login page
 * - POST /login - Process login
 * - GET /register - Show registration page
 * - POST /register - Process registration
 * - POST /logout - Logout user
 * 
 * TODO: Implement login/register logic
 * TODO: Validate user credentials
 * TODO: Manage session
 * TODO: Forward to login.jsp or register.jsp
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        
        if ("/login".equals(path)) {
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        } else if ("/register".equals(path)) {
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        } else if ("/logout".equals(path)) {
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getServletPath();
        
        if ("/login".equals(path)) {
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            // TODO: real validation via UserService
            if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
                request.getSession(true).setAttribute("user", new SessionUser(email));
                response.sendRedirect(request.getContextPath() + "/dashboard");
            } else {
                request.setAttribute("error", "Thông tin đăng nhập không hợp lệ");
                request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            }
        } else if ("/register".equals(path)) {
            // TODO: Persist user
            response.sendRedirect(request.getContextPath() + "/login");
        } else if ("/logout".equals(path)) {
            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}



