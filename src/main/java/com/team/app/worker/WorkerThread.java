package com.team.app.worker;

import com.team.app.dao.JobDAO;
import com.team.app.dao.JobArticleDAO;
import com.team.app.model.Job;
import com.team.app.model.JobArticle;
import com.team.app.service.CrawlService;
import com.team.app.service.SentimentService;
import com.team.app.util.HttpClientUtil;
import com.team.app.util.Logger;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * WorkerThread - Background thread for processing sentiment analysis jobs.
 */
public class WorkerThread extends Thread {
    
    private final JobQueue queue;
    private final CrawlService crawlService;
    private final SentimentService sentimentService;
    private final JobDAO jobDAO;
    private final JobArticleDAO articleDAO;
    private volatile boolean running = true;

    public WorkerThread(JobQueue queue, CrawlService crawlService, SentimentService sentimentService,
                        JobDAO jobDAO, JobArticleDAO articleDAO) {
        this.queue = queue != null ? queue : JobQueue.getInstance();
        this.crawlService = crawlService != null ? crawlService : new CrawlService();
        this.sentimentService = sentimentService != null ? sentimentService : new SentimentService();
        this.jobDAO = jobDAO != null ? jobDAO : new JobDAO();
        this.articleDAO = articleDAO != null ? articleDAO : new JobArticleDAO();
        setDaemon(true);
    }
    
    @Override
    public void run() {
        Logger.info("[WorkerThread] Worker thread started: " + getName());
        while (running) {
            Long jobId = null;
            try {
                jobId = queue.take();
                if (jobId == null) {
                    continue;
                }

                try {
                    jobDAO.updateStatus(jobId, "RUNNING", 10);
                } catch (RuntimeException statusEx) {
                    Logger.error("[WorkerThread] Failed to set job RUNNING with progress, fallback without progress", statusEx);
                    try {
                        jobDAO.updateStatus(jobId, "RUNNING");
                    } catch (RuntimeException fallbackEx) {
                        Logger.error("[WorkerThread] Fallback updateStatus without progress also failed", fallbackEx);
                    }
                }
                Job job = jobDAO.findById(jobId);
                if (job == null) {
                    Logger.error("[WorkerThread] Job not found: " + jobId);
                    jobDAO.updateStatus(jobId, "FAILED", 0);
                    continue;
                }
                
                Logger.info("[WorkerThread] Worker started job: " + job.getKeyword() + " (#" + jobId + ")");

                List<JobArticle> articles = crawlService.fetchArticles(job.getKeyword());
                Logger.info("[WorkerThread] Fetched " + articles.size() + " articles for: " + job.getKeyword());
                for (int i = 0; i < articles.size(); i++) {
                    JobArticle article = articles.get(i);
                    Logger.info(String.format("[WorkerThread] └ Article #%d | title=\"%s\" | url=%s", i + 1,
                            safe(article.getTitle()), safe(article.getUrl())));
                }

                // Clear previous data to avoid duplicates on reprocessing
                articleDAO.deleteByJobId(jobId);

                if (articles.isEmpty()) {
                    Logger.warn("[WorkerThread] No articles found for job " + jobId + ". Defaulting to neutral");
                    jobDAO.updateSentiment(jobId, 0.0, 0.0, 100.0);
                    jobDAO.updateStatus(jobId, "DONE", 100);
                    continue;
                }

                for (JobArticle article : articles) {
                    article.setJobId(jobId);
                }

                Map<String, Double> sentiment = sentimentService.analyze(articles);
                Logger.info(String.format("[WorkerThread] Sentiment result -> POS: %.2f%% NEG: %.2f%% NEU: %.2f%%",
                        sentiment.get("positive"), sentiment.get("negative"), sentiment.get("neutral")));

                for (JobArticle article : articles) {
                    articleDAO.insert(jobId, article);
                    Logger.debug(String.format("[WorkerThread] Saved article title=\"%s\"", safe(article.getTitle())));
                }
                Logger.info("[WorkerThread] Persisted " + articles.size() + " articles for job " + jobId);

                jobDAO.updateSentiment(jobId, sentiment);
                jobDAO.updateStatus(jobId, "DONE", 100);

                ensureEmbedding(jobId, job);

                Logger.info("[WorkerThread] ✅ Job " + job.getKeyword() + " completed (" + articles.size() + " articles)");
            } catch (InterruptedException e) {
                if (!running) {
                    Logger.info("[WorkerThread] Received interrupt signal, shutting down");
                    Thread.currentThread().interrupt();
                    break;
                }
                Logger.warn("[WorkerThread] Interrupted while waiting for jobs: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Logger.error("[WorkerThread] WorkerThread failed" + (jobId != null ? " for job " + jobId : ""), e);
                if (jobId != null) {
                    try {
                        jobDAO.updateStatus(jobId, "FAILED", 0);
                    } catch (Exception updateEx) {
                        Logger.error("[WorkerThread] Failed to update job status to FAILED for job " + jobId, updateEx);
                        try {
                            jobDAO.updateStatus(jobId, "FAILED");
                        } catch (Exception fallbackEx) {
                            Logger.error("[WorkerThread] Fallback failed when marking job as FAILED for job " + jobId, fallbackEx);
                        }
                    }
                }
            }
        }
        Logger.info("[WorkerThread] Worker thread stopped: " + getName());
    }
    
    private void ensureEmbedding(Long jobId, Job job) {
        try {
            if (job.getEmbedding() != null && job.getEmbedding().length > 0) {
                jobDAO.updateEmbedding(jobId, job.getEmbedding());
                Logger.info("[WorkerThread] Embedding refreshed for job ID: " + jobId);
                return;
            }

            Logger.warn("[WorkerThread] Embedding missing for job ID: " + jobId + ", regenerating...");
            double[] embedding = HttpClientUtil.getEmbedding(job.getKeyword());
            jobDAO.updateEmbedding(jobId, embedding);
            Logger.info("[WorkerThread] Embedding regenerated for job ID: " + jobId);
        } catch (IOException e) {
            Logger.error("[WorkerThread] Failed to regenerate embedding for job " + jobId, e);
        }
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\n", " ").replaceAll("\r", " ").trim();
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}

