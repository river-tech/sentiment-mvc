package com.team.app.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JobServlet - Controller for handling job-related requests
 * 
 * Handles:
 * - GET /jobs - List all jobs
 * - POST /jobs/create - Create a new job
 * - GET /jobs/{id} - View job details
 * - POST /jobs/{id}/delete - Delete a job
 * 
 * TODO: Implement job CRUD operations
 * TODO: Forward to jobs.jsp view
 * TODO: Handle form submissions for job creation
 */
@WebServlet(name = "JobServlet", urlPatterns = {"/jobs", "/jobs/*"})
public class JobServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/jobs.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "";
        if ("/create".equals(path)) {
            // Demo: set dummy sentiment summary, forward to dashboard
            request.setAttribute("job", new SentimentSummary(60, 25, 15));
            request.setAttribute("articles", java.util.Collections.emptyList());
            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/jobs");
        }
    }
}



