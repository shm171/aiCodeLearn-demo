package com.mylab.ailearn.base.global.configs;

/** 返回当前登录用户 ID：core 模块的数据归属 ID 必须来自已认证身份，绝不由请求参数传入。 */
public interface CurrentUserResolver {

    /**
     * 返回当前登录用户的 ID。
     *
     * @return 用户 ID，未登录时抛 401 异常
     */
    Long currentUserId();
}
