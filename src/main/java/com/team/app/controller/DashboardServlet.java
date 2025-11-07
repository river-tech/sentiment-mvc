package com.team.app.controller;

import com.team.app.dao.JobDAO;
import com.team.app.dao.JobArticleDAO;
import com.team.app.model.Job;
import com.team.app.model.JobArticle;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
    
    private JobDAO jobDAO;
    private JobArticleDAO jobArticleDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        // Lazy initialization - only create when needed
        // This ensures DatabaseConfig has been initialized first
    }
    
    private JobDAO getJobDAO() {
        if (jobDAO == null) {
            jobDAO = new JobDAO();
        }
        return jobDAO;
    }
    
    private JobArticleDAO getJobArticleDAO() {
        if (jobArticleDAO == null) {
            jobArticleDAO = new JobArticleDAO();
        }
        return jobArticleDAO;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        try {
            // Load latest job (system-wide, no user filtering)
            List<Job> jobs = getJobDAO().findAll();
            
            if (!jobs.isEmpty()) {
                // Get the most recent job
                Job latestJob = jobs.get(0);
                request.setAttribute("job", latestJob);
                
                // Load articles for this job
                List<JobArticle> articles = getJobArticleDAO().findByJobId(latestJob.getId().intValue());
                request.setAttribute("articles", articles);
            } else {
                // No jobs yet - set empty data
                request.setAttribute("job", null);
                request.setAttribute("articles", java.util.Collections.emptyList());
            }
        } catch (Exception e) {
            // Log error and set empty data to prevent 500 error
            e.printStackTrace();
            request.setAttribute("job", null);
            request.setAttribute("articles", java.util.Collections.emptyList());
            request.setAttribute("error", "Lỗi khi tải dữ liệu: " + e.getMessage());
        }
        
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }
}


