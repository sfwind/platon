package com.iquanwai.platon.biz.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by justin on 17/9/1.
 */
@Component
public class ThreadPool {

    private static ThreadPoolExecutor POOL;

    private static Logger logger = LoggerFactory.getLogger(ThreadPool.class);

    private final static int MAX_SIZE = 30;
    private final static int INIT_SIZE = 10;
    private final static int IDLE_TIME = 1;
    private final static int MAX_QUEUE_SIZE = 2000;

    public static void execute(Thread thread, String threadName) {
        if(thread==null){
            logger.error("thread is null, return at once");
            return;
        }
        thread.setName(threadName);

        if(!POOL.isTerminating() || !POOL.isTerminated()){
            POOL.execute(thread);
        }else{
            logger.error("pool is terminating, refuse to execute thread any more");
        }
    }

    @PreDestroy
    public void destroy(){
        logger.info("thread pool is destroying");
        if(POOL!=null){
            POOL.shutdown();
            try {
                if (!POOL.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.info("thread pool is terminate now");
                    // pool didn't terminate after the first try
                    POOL.shutdownNow();
                }

                if (!POOL.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.error("thread pool is hanging, leave it alone");
                    // pool didn't terminate after the second try
                }
            } catch (InterruptedException ex) {
                POOL.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @PostConstruct
    public void init(){
        logger.info("thread pool is init");
        POOL = new ThreadPoolExecutor(INIT_SIZE, MAX_SIZE,
                IDLE_TIME, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(MAX_QUEUE_SIZE, true),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }


    public static ThreadPoolExecutor createSingleThreadExecutor(){
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
