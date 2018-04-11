package com.iquanwai.platon.biz.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 三十文
 */
@Component
public class ExecutorUtil {

    private static ExecutorService executorService;
    private static final int THREAD_POOL_SIZE = 5;

    private Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    public void init() {
        logger.info("Starting Thread Pool，Pool Size: {}", THREAD_POOL_SIZE);
        // 线程处理时间长，且不是特别常用，使用 fixed pool
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        logger.info("Thread Pool Starting Success.");
    }

    @PreDestroy
    public void destroy() {
        logger.info("destroying thread pool");
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * 线程池中添加任务
     */
    public static void submit(Runnable runnable) {
        executorService.submit(runnable);
    }

}
