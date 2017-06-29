package com.iquanwai.platon.biz.repository.elasticsearch;

import lombok.Data;

/**
 * Created by nethunder on 2017/6/28.
 */
@Data
public class DocValue<T> {
    T data;
    Integer id;
}
