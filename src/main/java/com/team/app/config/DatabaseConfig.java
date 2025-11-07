package com.team.app.config;

import com.team.app.util.Logger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.InputStream;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * DatabaseConfig - HikariCP connection pool configuration
 * 
 * TODO: Load database properties from application.properties
 * TODO: Initialize HikariCP DataSource
 * TODO: Store DataSource in servlet context
 * TODO: Cleanup connection pool on shutdown
 */
@WebListener
public class DatabaseConfig implements ServletContextListener {
    
    private static HikariDataSource dataSource;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Logger.info("═══════════════════════════════════════════════════════");
        Logger.info("🔧 [DatabaseConfig] Khởi tạo database connection...");
        Logger.info("   Working directory: " + System.getProperty("user.dir"));
        Logger.info("   Log file: " + com.team.app.util.Logger.getLogFilePath());
        try {
            // Load properties from application.properties
            Logger.info("   📄 Đang load application.properties...");
            Properties props = new Properties();
            try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
                if (is == null) {
                    throw new IllegalStateException("application.properties not found in classpath");
                }
                props.load(is);
            }

            // Configure HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            String driver = props.getProperty("db.driver");
            if (driver != null && !driver.isEmpty()) {
                config.setDriverClassName(driver);
            }
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximum", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimum.idle", "5")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connection.timeout", "30000")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idle.timeout", "600000")));
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.max.lifetime", "1800000")));

            // Initialize DataSource
            Logger.info("   🔌 Đang khởi tạo HikariCP DataSource...");
            dataSource = new HikariDataSource(config);
            Logger.info("   ✅ DataSource đã được khởi tạo thành công");

            // Store in servlet context
            sce.getServletContext().setAttribute("dataSource", dataSource);
            Logger.info("   ✅ DataSource đã được lưu vào servlet context");
            Logger.info("═══════════════════════════════════════════════════════");
            
        } catch (Exception e) {
            Logger.error("   ❌ Lỗi khi khởi tạo database connection", e);
            throw new RuntimeException("Failed to initialize database connection", e);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO: Close DataSource on shutdown
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    /**
     * Get DataSource instance
     */
    public static DataSource getDataSource() {
        return dataSource;
    }
}

