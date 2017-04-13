package com.iquanwai.platon.biz.util.zk;

import lombok.Data;

/**
 * Created by justin on 17/4/13.
 */
@Data
public class ConfigNode {
    private String value;
    private long c_time;
    private long m_time;

}
