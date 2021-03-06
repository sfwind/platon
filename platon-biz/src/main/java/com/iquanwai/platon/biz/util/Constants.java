package com.iquanwai.platon.biz.util;

/**
 * Created by nethunder on 2016/12/21.
 */
public interface Constants {

    interface Status {
        String OK = "1";
        String FAIL = "0";
    }

    interface AccountError {
        Integer TIME_OUT = 100001;
    }

    interface VoteType {
        int CHALLENGE = 1;
        int APPLICATION = 2;
        int SUBJECT = 3;
    }

    interface CommentModule {
        int CHALLENGE = 1;
        int APPLICATION = 2;
        int SUBJECT = 3;
        int KNOWLEDGE = 4;
    }

    interface CommentType {
        int STUDENT = 1;
    }

    interface Module {
        int CHALLENGE = 1;
        int APPLICATION = 2;
        int SUBJECT = 3;
    }

    interface ViewInfo {
        interface Module {
            int CHALLENGE = 1;
            int APPLICATION = 2;
            int SUBJECT = 3;
        }

        interface EventType {
            int PC_SUBMIT = 1;
            int MOBILE_SUBMIT = 2;
            int PC_SHOW = 3;
            int MOBILE_SHOW = 4;
        }
    }

    interface Device {
        int PC = 1;
        int MOBILE = 2;
    }

    /** 请求发送平台 */
    interface PlatForm {
        /** PC 网页 */
        int PC = 1;
        /** 手机网页 */
        int WE_MOBILE = 2;
        /** 微信小程序 */
        int WE_MINI = 3;
    }

    interface LabelArticleModule {
        int CHALLENGE = 1;
        int APPLICATION = 2;
        int SUBJECT = 3;
    }

    interface ValidCode {
        int MOBILE_VALID = 1;
    }

    interface WEIXIN_MESSAGE_TYPE {
        int TEXT = 1;
        int IMAGE = 2;
        int VOICE = 3;
    }

    interface RISE_MEMBER {
        int FREE = 0;
        int MEMBERSHIP = 1;
        int COURSE_USER = 2;
        int MONTHLY_CAMP = 3;
    }

    interface HTTP_TIMEOUT {
        int CONNECTION_TIMEOUT = 3;
        int READ_TIMEOUT = 60;
    }

    interface CERTIFICATE {
        interface TYPE {
            int CLASS_LEADER = 1;
            int GROUP_LEADER = 2;
            int SUPERB_MEMBER = 3;
            int SUPERB_GROUP = 4;
            int ORDINARY = 5;
            int ASST_COACH = 6;
            int CLASS_NORMAL = 7;
        }

        interface NAME {
            String CLASS_LEADER = "优秀班长";
            String GROUP_LEADER = "优秀组长";
            String SUPERB_MEMBER = "优秀学员";
            String SUPERB_GROUP = "优秀团队";
            String ORDINARY = "结课证书";
            String ASST_COACH = "优秀助教";
            String CLASS_NORMAL = "优秀班委";
        }
    }

    int DISCUSS_PAGE_SIZE = 100;

    interface Project{
        int CORE_PROJECT = 1;
        int BUSINESS_THOUGHT_PROJECT = 2;
    }

    String TEMP_IMAGE_PATH = "/data/static/images/";

}
