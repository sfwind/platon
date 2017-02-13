package com.iquanwai.platon.biz.util.page;

/**
 * Created by nethunder on 2017/2/3.
 */
public interface Pageable {
    /**
     *
     * @param page
     * @return {@link #getPage()}
     */
    void setPage(Page page);
    Page getPage();
}