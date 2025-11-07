package com.team.app.controller;

import com.team.app.model.Job;
import com.team.app.service.KeywordService;
import com.team.app.util.Logger;
import com.team.app.worker.JobQueue;
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
 * - POST /jobs/create - Create a new job with semantic matching
 * - GET /jobs/{id} - View job details
 * - POST /jobs/{id}/delete - Delete a job
 */
@WebServlet(name = "JobServlet", urlPatterns = {"/jobs", "/jobs/*"})
public class JobServlet extends HttpServlet {
    
    private final KeywordService keywordService;
    
    public JobServlet() {
        this.keywordService = new KeywordService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        Logger.info("[JobServlet] Forwarding to dashboard view");
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "";
        
        if ("/create".equals(path)) {
            // Get keyword from request
            String keyword = request.getParameter("keyword");
            Logger.info("═══════════════════════════════════════════════════════");
            Logger.info("📥 [JobServlet] Nhận request tạo job mới");
            Logger.info("   Keyword: " + keyword);
            
            if (keyword == null || keyword.trim().isEmpty()) {
                Logger.warn("   ❌ Keyword rỗng, từ chối request");
                request.setAttribute("error", "Vui lòng nhập từ khóa");
                request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
                return;
            }
            
            Logger.info("   🔄 Bắt đầu xử lý keyword (ứng dụng không cần đăng nhập)...");
            
            try {
                // Process keyword: get embedding, find similar job, or create new
                // Jobs are now system-wide, no user_id needed
                Job job = keywordService.processKeyword(keyword);
                
                Logger.info("   ✅ Job được tạo/tìm thấy:");
                Logger.info("      - Job ID: " + job.getId());
                Logger.info("      - Status: " + job.getStatus());
                Logger.info("      - Keyword: " + job.getKeyword());
                
                // If job is QUEUED (newly created), submit to queue
                if ("QUEUED".equals(job.getStatus())) {
                    Logger.info("   📤 Submit job vào JobQueue để xử lý background");
                    JobQueue.getInstance().submit(job.getId());
                    Logger.info("      ✅ Job ID " + job.getId() + " đã được thêm vào queue");
                } else {
                    Logger.info("   ℹ️  Job status: " + job.getStatus() + " - không cần submit vào queue");
                }
                
                Logger.info("   ✅ Hoàn tất xử lý, redirect về dashboard");
                Logger.info("═══════════════════════════════════════════════════════");
                
                // Redirect to jobs list or dashboard
                response.sendRedirect(request.getContextPath() + "/dashboard");
                
            } catch (IOException e) {
                Logger.error("   ❌ Lỗi khi gọi API embedding", e);
                request.setAttribute("error", "Lỗi khi gọi API embedding: " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
            } catch (Exception e) {
                Logger.error("   ❌ Lỗi khi xử lý từ khóa", e);
                request.setAttribute("error", "Lỗi khi xử lý từ khóa: " + e.getMessage());
                request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }
}



