package com.iquanwai.platon.web.resolver;

import com.iquanwai.platon.biz.po.common.Callback;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by 三十文
 */
public interface UnionUserService {
    /** 根据请求获取 callback 数据 */
    Callback getCallbackByRequest(HttpServletRequest request);

    /** 根据请求获取用户，如果用户不存在会请求 confucius 生成 */
    UnionUser getUnionUserByCallback(Callback callback);

    /** 获取当前所在平台 */
    UnionUser.Platform getPlatformType(HttpServletRequest request);

    /** 根据 UnionId 刷新用户缓存 */
    void updateUserByUnionId(String unionId);

    /** 查看当前请求是否是资源请求 */
    boolean isDocumentRequest(HttpServletRequest request);

    /** 删除当前用户缓存，用户登出系统 */
    void logout(String state);

    /** 返回当前缓存的所有用户信息 */
    List<UnionUser> getAllLoginUsers();
}
