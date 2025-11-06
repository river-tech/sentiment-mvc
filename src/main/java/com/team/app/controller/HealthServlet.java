package com.team.app.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;

@WebServlet(name = "HealthServlet", urlPatterns = {"/health/db"})
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        DataSource ds = (DataSource) getServletContext().getAttribute("dataSource");

        if (ds == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"status\":\"error\",\"message\":\"DataSource not initialized\"}");
            return;
        }

        try (Connection con = ds.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT 1")) {
            if (rs.next()) {
                int v = rs.getInt(1);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write("{\"status\":\"ok\",\"select1\":" + v + "}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"status\":\"error\",\"message\":\"SELECT 1 returned no rows\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String msg = e.getMessage() == null ? "error" : e.getMessage().replace("\"", "'");
            out.write("{\"status\":\"error\",\"message\":\"" + msg + "\"}");
        }
    }
}


