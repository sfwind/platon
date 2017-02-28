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
    }
    interface VoteType{
        int CHALLENGE = 1;
        int APPLICATION = 2;
    }

    interface PictureType {
        int HOMEWORK = 1;
        int CHALLENGE = 2;
        int APPLICATION = 3;
    }

    interface CommentModule {
        int CHALLENGE = 1;
        int APPLICATION = 2;
    }

    interface CommentType{
        int STUDENT  = 1;
    }
}
