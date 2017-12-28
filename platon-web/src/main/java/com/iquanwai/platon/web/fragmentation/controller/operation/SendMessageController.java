package com.iquanwai.platon.web.fragmentation.controller.operation;

import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.account.AccountService;
import com.iquanwai.platon.biz.domain.weixin.customer.CustomerMessageService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.biz.po.common.Profile;
import com.iquanwai.platon.biz.util.Constants;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.util.WebUtils;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/rise/operation")
public class SendMessageController {

    @Autowired
    private CustomerMessageService customerMessageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private OperationLogService operationLogService;

    @RequestMapping("/send/message")
    public ResponseEntity<Map<String,Object>> sendMessage(LoginUser loginUser){
        Asserts.notNull(loginUser,"登录用户不能为空");

        OperationLog operationLog = OperationLog.create().module("礼品卡管理").function("发送模板消息").action("发送成功领取消息");
        operationLogService.log(operationLog);
        Profile profile = accountService.getProfile(loginUser.getOpenId());

        if(profile==null){
            return WebUtils.error("找不到该用户");
        }

        String templeateMsg = "你好{NickName},欢迎来到圈外商学院！\n 你已成功领取商学院体验卡！扫码加小Y，回复\"体验\",让他带你开启7天线上学习之旅吧！";
        String nickName = profile.getNickname();

        customerMessageService.sendCustomerMessage(loginUser.getOpenId(),templeateMsg.replace("{NickName}",nickName), Constants.WEIXIN_MESSAGE_TYPE.TEXT);
        //customerMessageService.sendCustomerMessage(loginUser.getOpenId(),"",Constants.WEIXIN_MESSAGE_TYPE.IMAGE);
        return WebUtils.success();
    }
}
