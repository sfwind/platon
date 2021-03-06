package com.iquanwai.platon.biz.util.page;

/**
 * Created by nethunder on 2017/2/3.
 */

import com.google.gson.Gson;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel("分页model")
public class Page implements Serializable {
    private static final long serialVersionUID = -4312323165564562319L;

    @ApiModelProperty("当前页面序号")
    private int page = 1;

    @ApiModelProperty("每页的条数")
    private int pageSize = 5;

    @ApiModelProperty("总数")
    private int total = -1;

    public int getPage() {
        return page;
    }

    /**
     * 当前页码, 1-based
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * 每页记录数
     */
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPrevPage() {
        return this.isFirstPage() ? this.getPage() : this.getPage() - 1;
    }

    public int getNextPage() {
        return this.isLastPage() ? this.getPage() : this.getPage() + 1;
    }

    public boolean isFirstPage() {
        return (1 == this.getPage());
    }

    public boolean isLastPage() {
        if(-1 == this.getPageCount()){
            return false;
        }

        return (this.getPageCount() < 1 || this.getPageCount() <= this.getPage());
    }

    /**
     * 页数, 根据total和pageSize计算
     * -1: 未知
     * @return
     */
    public int getPageCount() {
        if(-1 == total){
            return -1;
        }

        if (total < 1) {
            return 0;
        }

        if (pageSize < 1) {
            return 1;
        }

        return (0 == total % pageSize) ? total / pageSize : total / pageSize
                + 1;
    }

    /**
     *
     * mysql offset, 0-based, 根据page和pageSize计算
     *
     * @return
     */
    public int getOffset() {
        if (page < 1) {
            return 0;
        }

        return (page - 1) * pageSize;
    }

    /**
     * mysql limit, 0-based, 根据page和pageSize计算
     *
     * @return
     */
    public int getLimit() {
        return pageSize;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + page;
        result = prime * result + pageSize;
        result = prime * result + total;

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        Page other = (Page) obj;
        if (page != other.page){
            return false;
        }
        if (pageSize != other.pageSize){
            return false;
        }
        return total == other.total;

    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}