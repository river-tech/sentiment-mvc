package com.team.app.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * JobQueue - Queue manager for background job processing
 * 
 * TODO: Implement queue management
 * TODO: Start worker threads
 * TODO: Submit jobs to queue
 * TODO: Monitor queue status
 */
public class JobQueue {
    
    private BlockingQueue<Integer> jobQueue;
    private WorkerThread[] workers;
    private static final int NUM_WORKERS = 3; // Number of worker threads
    
    // TODO: Initialize queue and workers in constructor
    
    public JobQueue() {
        this.jobQueue = new LinkedBlockingQueue<>();
        this.workers = new WorkerThread[NUM_WORKERS];
        
        // TODO: Start worker threads
        for (int i = 0; i < NUM_WORKERS; i++) {
            workers[i] = new WorkerThread(jobQueue);
            workers[i].start();
        }
    }
    
    /**
     * Submit a job to the queue for processing
     * TODO: Add job ID to queue
     */
    public void submitJob(int jobId) {
        // TODO: Add jobId to queue
        // jobQueue.offer(jobId);
    }
    
    /**
     * Get queue size
     */
    public int getQueueSize() {
        return jobQueue.size();
    }
    
    /**
     * Shutdown all workers
     * TODO: Stop all worker threads gracefully
     */
    public void shutdown() {
        // TODO: Call shutdown() on all workers
    }
}

