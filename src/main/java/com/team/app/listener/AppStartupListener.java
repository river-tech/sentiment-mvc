package com.team.app.listener;

import com.team.app.dao.JobArticleDAO;
import com.team.app.dao.JobDAO;
import com.team.app.service.CrawlService;
import com.team.app.service.SentimentService;
import com.team.app.util.Logger;
import com.team.app.worker.JobQueue;
import com.team.app.worker.WorkerThread;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Application lifecycle listener that launches background worker threads.
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    private final List<WorkerThread> workers = new ArrayList<>();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Logger.info("[AppStartupListener] Initializing sentiment worker threads");

        int workerCount = Math.max(1, Runtime.getRuntime().availableProcessors() / 4);
        for (int i = 0; i < workerCount; i++) {
            WorkerThread worker = new WorkerThread(
                    JobQueue.getInstance(),
                    new CrawlService(),
                    new SentimentService(),
                    new JobDAO(),
                    new JobArticleDAO());
            worker.setName("SentimentWorker-" + (i + 1));
            worker.start();
            workers.add(worker);
        }

        Logger.info("[AppStartupListener] Started " + workers.size() + " worker thread(s)");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Logger.info("[AppStartupListener] Shutting down worker threads");
        for (WorkerThread worker : workers) {
            worker.shutdown();
        }
        workers.clear();
    }
}


