package com.iquanwai.platon.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.io.File;

/**
 * Created by justin on 17/8/5.
 */
@Component
public class ClearImageCacheJob {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Scheduled(cron="${clearImageCacheJob.cron}")
    public void work(){
        logger.info("clear temp file job start");
        File directory = ImageIO.getCacheDirectory();
        File[] files = directory.listFiles();
        if(files!=null){
            for(File file:files){
                file.delete();
            }
        }

        logger.info("clear temp file job end");
    }
}
