package com.iquanwai.platon.web.fragmentation.dto;

import com.iquanwai.platon.biz.po.NotifyMessage;
import lombok.Data;

import java.util.List;

/**
 * Created by justin on 17/3/6.
 */
@Data
public class NotifyMessageDto {
    private Boolean end;
    private List<NotifyMessage> notifyMessageList;
}
