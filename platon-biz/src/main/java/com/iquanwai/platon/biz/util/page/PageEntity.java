package com.iquanwai.platon.biz.util.page;

import java.io.Serializable;

/**
 * Created by nethunder on 2017/2/3.
 */
public class PageEntity implements Pageable, Serializable {
    private Page page;


    public void setPage(Page page) {
        this.page = page;
    }

    public Page getPage() {
        return this.page;
    }
}