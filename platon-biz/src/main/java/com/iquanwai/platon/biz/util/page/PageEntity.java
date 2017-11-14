package com.iquanwai.platon.biz.util.page;

import java.io.Serializable;

/**
 * Created by nethunder on 2017/2/3.
 */
public class PageEntity implements Pageable, Serializable {
    private static final long serialVersionUID = 8038613688974589757L;
    private Page page;


    @Override
    public void setPage(Page page) {
        this.page = page;
    }

    @Override
    public Page getPage() {
        return this.page;
    }
}