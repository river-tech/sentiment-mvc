package com.team.app.controller;

import com.team.app.model.Job;
import com.team.app.model.JobArticle;
import com.team.app.dao.JobDAO;
import com.team.app.dao.JobArticleDAO;
import com.team.app.service.KeywordService;
import com.team.app.util.Logger;
import com.team.app.worker.JobQueue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;

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
        String path = request.getPathInfo();
        if ("/status".equals(path)) {
            handleStatus(request, response);
            return;
        }
        Logger.info("[JobServlet] Forwarding to dashboard view");
        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String path = request.getPathInfo();
        if (path == null) path = "";
        
        if ("/create".equals(path)) {
            Logger.info("═══════════════════════════════════════════════════════");
            Logger.info("📥 [JobServlet] Nhận request tạo job mới");
            Logger.info("   Method: " + request.getMethod());
            Logger.info("   Content-Type: " + request.getContentType());
            Logger.info("   Content-Length: " + request.getContentLength());
            
            // Debug: log all parameters
            java.util.Enumeration<String> paramNames = request.getParameterNames();
            Logger.info("   Parameters:");
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                String paramValue = request.getParameter(paramName);
                Logger.info("      - " + paramName + " = " + paramValue);
            }
            
            // Get keyword from request
            String keyword = request.getParameter("keyword");
            Logger.info("   Keyword (from getParameter): " + keyword);
            
            // Try reading from request body if parameter is null
            if (keyword == null || keyword.trim().isEmpty()) {
                try {
                    java.io.BufferedReader reader = request.getReader();
                    StringBuilder body = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        body.append(line);
                    }
                    Logger.info("   Request body: " + body.toString());
                    
                    // Try parsing URL-encoded body
                    if (body.length() > 0) {
                        String bodyStr = body.toString();
                        String[] pairs = bodyStr.split("&");
                        for (String pair : pairs) {
                            String[] keyValue = pair.split("=", 2);
                            if (keyValue.length == 2 && "keyword".equals(keyValue[0])) {
                                try {
                                    keyword = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                                    Logger.info("   Keyword (from body): " + keyword);
                                } catch (java.io.UnsupportedEncodingException e) {
                                    Logger.error("   Failed to decode keyword", e);
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.error("   Error reading request body", e);
                }
            }
            
            if (keyword == null || keyword.trim().isEmpty()) {
                Logger.warn("   ❌ Keyword rỗng, từ chối request");
                if (isAjax(request)) {
                    Map<String, Object> errorPayload = new HashMap<>();
                    errorPayload.put("error", "Vui lòng nhập từ khóa");
                    writeJson(response, errorPayload);
                } else {
                    request.setAttribute("error", "Vui lòng nhập từ khóa");
                    request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
                }
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
                    Logger.info("   📋 Đây là job tương tự (similar) - articles sẽ được lấy từ DB");
                }

                // If AJAX request, return JSON instead of redirect
                if (isAjax(request)) {
                    Map<String, Object> payload = new HashMap<>();
                    // Convert Long to String to avoid precision loss in JavaScript
                    payload.put("jobId", String.valueOf(job.getId()));
                    payload.put("status", job.getStatus());
                    payload.put("keyword", job.getKeyword());
                    Logger.info("   📤 AJAX response: jobId=" + job.getId() + ", status=" + job.getStatus());
                    writeJson(response, payload);
                    return;
                }

                Logger.info("   ✅ Hoàn tất xử lý, redirect về dashboard");
                Logger.info("═══════════════════════════════════════════════════════");
                response.sendRedirect(request.getContextPath() + "/dashboard");
                
            } catch (IOException e) {
                Logger.error("   ❌ Lỗi khi gọi API embedding", e);
                if (isAjax(request)) {
                    Map<String, Object> errorPayload = new HashMap<>();
                    errorPayload.put("error", "Lỗi khi gọi API embedding: " + e.getMessage());
                    writeJson(response, errorPayload);
                } else {
                    request.setAttribute("error", "Lỗi khi gọi API embedding: " + e.getMessage());
                    request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
                }
            } catch (Exception e) {
                Logger.error("   ❌ Lỗi khi xử lý từ khóa", e);
                if (isAjax(request)) {
                    Map<String, Object> errorPayload = new HashMap<>();
                    errorPayload.put("error", "Lỗi khi xử lý từ khóa: " + e.getMessage());
                    writeJson(response, errorPayload);
                } else {
                    request.setAttribute("error", "Lỗi khi xử lý từ khóa: " + e.getMessage());
                    request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
                }
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    private boolean isAjax(HttpServletRequest request) {
        String xhr = request.getHeader("X-Requested-With");
        String ajaxParam = request.getParameter("ajax");
        return (xhr != null && xhr.equalsIgnoreCase("XMLHttpRequest")) || (ajaxParam != null && ajaxParam.equals("1"));
    }

    private void writeJson(HttpServletResponse response, Object payload) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        Gson gson = new Gson();
        try (PrintWriter out = response.getWriter()) {
            out.write(gson.toJson(payload));
            out.flush();
        }
    }

    private void handleStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idParam = request.getParameter("id");
        Map<String, Object> payload = new HashMap<>();
        
        Logger.info("[JobServlet] handleStatus called with id param: '" + idParam + "'");
        
        if (idParam == null || idParam.trim().isEmpty()) {
            Logger.warn("[JobServlet] Missing or empty id parameter");
            payload.put("error", "Missing id parameter");
            writeJson(response, payload);
            return;
        }
        
        try {
            long jobId = Long.parseLong(idParam.trim());
            Logger.info("[JobServlet] Status check for jobId: " + jobId);
            JobDAO jobDAO = new JobDAO();
            JobArticleDAO articleDAO = new JobArticleDAO();
            Job job = jobDAO.findById(jobId);
            if (job == null) {
                Logger.warn("[JobServlet] Job not found: " + jobId);
                payload.put("error", "Job not found");
                writeJson(response, payload);
                return;
            }
            // Convert Long to String to avoid precision loss in JavaScript
            payload.put("jobId", String.valueOf(job.getId()));
            payload.put("status", job.getStatus());
            payload.put("positive", job.getPositive());
            payload.put("negative", job.getNegative());
            payload.put("neutral", job.getNeutral());
            List<JobArticle> articles = articleDAO.findByJobId((int) jobId);
            payload.put("articles", articles);
            Logger.info("[JobServlet] Status response: jobId=" + jobId + ", status=" + job.getStatus() 
                    + ", articles=" + (articles != null ? articles.size() : 0));
            writeJson(response, payload);
        } catch (NumberFormatException e) {
            Logger.error("[JobServlet] Invalid jobId: " + idParam, e);
            payload.put("error", "Invalid id");
            writeJson(response, payload);
        } catch (Exception e) {
            Logger.error("[JobServlet] Error in handleStatus for id: " + idParam, e);
            payload.put("error", "Server error: " + e.getMessage());
            writeJson(response, payload);
        }
    }
}



