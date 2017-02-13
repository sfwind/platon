package com.iquanwai.platon.web.fragmentation.controller;

import com.iquanwai.platon.biz.domain.fragmentation.cache.CacheService;
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
@RequestMapping("/fragment/cache")
public class CacheController {
    @Autowired
    private CacheService cacheService;

    @RequestMapping("/reload")
    public ResponseEntity<Map<String, Object>> reload(){
        cacheService.reload();
        return WebUtils.success();
    }
}
