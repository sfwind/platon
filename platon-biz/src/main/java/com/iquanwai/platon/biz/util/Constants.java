package com.iquanwai.platon.biz.util;

/**
 * Created by nethunder on 2016/12/21.
 */
public interface Constants {
    interface Status{
        String OK = "1";
        String FAIL = "0";
    }
    interface AccountError{
        Integer TIME_OUT = 100001;
    }
    interface PracticeType{
        int CHALLENGE = 21;
        int APPLICATION = 11;
        int SUBJECT = 1; // 小课论坛
    }
    interface VoteType{
        int CHALLENGE = 1;
        int APPLICATION = 2;
        int SUBJECT = 3;
    }

    interface PictureType {
        int HOMEWORK = 1;
        int CHALLENGE = 2;
        int APPLICATION = 3;
        int SUBJECT = 4;
    }

    interface CommentModule {
        int CHALLENGE = 1;
        int APPLICATION = 2;
        int SUBJECT = 3;
        int KNOWLEDGE = 4;
    }

    interface CommentType{
        int STUDENT  = 1;
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
    interface Device{
        int PC = 1;
        int MOBILE = 2;
    }
    interface LabelArticleModule{
        int CHALLENGE = 1;
        int APPLICATION = 2;
        int SUBJECT = 3;
    }
    interface ValidCode{
        int MOBILE_VALID = 1;
    }

    interface WEIXIN_MESSAGE_TYPE{
        int TEXT = 1;
        int IMAGE = 2;
        int VOICE = 3;
    }

    interface RISE_MEMBER{
        int FREE = 0;
        int MEMBERSHIP = 1;
        int COURSE_USER = 2;
    }

    interface HTTP_TIMEOUT{
        int CONNECTION_TIMEOUT = 3;
        int READ_TIMEOUT = 60;
    }

    int DISCUSS_PAGE_SIZE = 100;
}
