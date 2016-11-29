package com.iquanwai.platon.biz.domain.weixin.data;

import java.util.Date;

/**
 * Created by justin on 16/8/15.
 */
public interface DataService {
    String USER_SUMMARY_URL = "https://api.weixin.qq.com/datacube/getusersummary?access_token={access_token}";

    String USER_CUMULATE_URL = "https://api.weixin.qq.com/datacube/getusercumulate?access_token={access_token}";

    void getSummaryData(Date from, Date to);

    void getCumulateData(Date from, Date to);
}
