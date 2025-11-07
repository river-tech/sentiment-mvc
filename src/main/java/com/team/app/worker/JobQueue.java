package com.team.app.worker;

import com.team.app.util.Logger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * JobQueue - Simple singleton queue for background sentiment jobs.
 */
public class JobQueue {

    private static final JobQueue INSTANCE = new JobQueue();

    private final BlockingQueue<Long> queue = new LinkedBlockingQueue<>();

    private JobQueue() {}

    public static JobQueue getInstance() {
        return INSTANCE;
    }

    public void submit(Long jobId) {
        if (jobId == null) {
            Logger.warn("[JobQueue] Attempted to submit null jobId");
            return;
        }
        queue.offer(jobId);
        Logger.info("[JobQueue] Submitted job ID: " + jobId);
    }

    public Long take() throws InterruptedException {
        Long jobId = queue.take();
        Logger.info("[JobQueue] Dequeued job ID: " + jobId);
        return jobId;
    }

    public int size() {
        return queue.size();
    }
}

