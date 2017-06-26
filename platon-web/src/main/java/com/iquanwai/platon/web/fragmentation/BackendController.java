package com.iquanwai.platon.web.fragmentation;

import com.iquanwai.platon.biz.domain.log.OperationLogService;
import com.iquanwai.platon.biz.domain.weixin.oauth.OAuthService;
import com.iquanwai.platon.biz.po.common.OperationLog;
import com.iquanwai.platon.web.fragmentation.dto.ErrorLogDto;
import com.iquanwai.platon.web.fragmentation.dto.MarkDto;
import com.iquanwai.platon.web.resolver.LoginUser;
import com.iquanwai.platon.web.resolver.LoginUserService;
import com.iquanwai.platon.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by justin on 16/10/8.
 */
@RestController
@RequestMapping("/rise/b")
public class BackendController {
    @Autowired
    private OperationLogService operationLogService;
    @Autowired
    private OAuthService oAuthService;

    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @RequestMapping(value = "/log", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> log(@RequestBody ErrorLogDto errorLogDto){
        String data = errorLogDto.getResult();
        if(data.length()>900){
            data = data.substring(0, 900);
        }
        String cookieStr= errorLogDto.getCookie();

        String openid = oAuthService.openId(getAccessTokenFromCookie(cookieStr));
        OperationLog operationLog = OperationLog.create().openid(openid)
                .module("记录前端bug")
                .function("bug")
                .action("记录RISE bug")
                .memo("url:"+errorLogDto.getUrl()+";data:"+data);
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/mark", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> mark(LoginUser loginUser, @RequestBody MarkDto markDto) {
        OperationLog operationLog = OperationLog.create().openid(loginUser == null ? null : loginUser.getOpenId())
                .module(markDto.getModule())
                .function(markDto.getFunction())
                .action(markDto.getAction())
                .memo(markDto.getMemo());
        operationLogService.log(operationLog);
        return WebUtils.success();
    }

    @RequestMapping(value = "/login/users", method = RequestMethod.GET)
    public ResponseEntity<Map<String, Object>> loginUsersList(@RequestParam(value = "openid") String openid) {
        List<LoginUser> allUser = LoginUserService.getAllUsers();
        LOGGER.info("openid:{},users:{}", openid, allUser.size());
        List<LoginUser> list = allUser.stream().filter(item -> item.getOpenId().equals(openid)).collect(Collectors.toList());
        return WebUtils.result(list);
    }


    private static String getAccessTokenFromCookie(String cookieStr){
        String[] cookies = cookieStr.split(";");
        String accessToken ="";
        for(String cookie:cookies){
            if(cookie.startsWith(OAuthService.ACCESS_TOKEN_COOKIE_NAME+"=")){
                accessToken = cookie.substring(OAuthService.ACCESS_TOKEN_COOKIE_NAME.length()+1);
                break;
            }
        }
        return accessToken;
    }


}
