package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.common.file.PictureService;
import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
import com.iquanwai.platon.job.RiseMemberJob;
import com.iquanwai.platon.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by justin on 17/1/1.
 */
@RestController
@RequestMapping("/rise/cache")
public class CacheController {
    @Autowired
    private CacheService cacheService;
    @Autowired
    private PictureService pictureService;
    @Autowired
    private RiseMemberJob riseMemberJob;

    @RequestMapping("/reload")
    public ResponseEntity<Map<String, Object>> reload(){
        cacheService.reload();
        pictureService.reloadModule();
        return WebUtils.success();
    }

    @RequestMapping("/reload/member")
    public ResponseEntity<Map<String, Object>> reloadMember(){
        Integer count = riseMemberJob.refreshStatus();
        return WebUtils.result(count);
    }
}
