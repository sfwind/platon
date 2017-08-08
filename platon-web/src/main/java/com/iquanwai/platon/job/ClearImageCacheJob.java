package com.iquanwai.platon.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by justin on 17/8/5.
 */
@Component
public class ClearImageCacheJob {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron="${clearImageCacheJob.cron}")
    public void work(){
        String path = System.getProperty("java.io.tmpdir");
        File directory = new File(path);
        File[] files = directory.listFiles();
        if(files!=null){
            for(File file:files){
                file.delete();
            }
        }
    }
}
