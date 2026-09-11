package com.mylab.ailearn.base.global.configs;

/**
 * 获取当前登录用户的 ID。
 *
 * <p>由 base 模块实现，core 模块依赖此接口获取 ownerUserId，
 * 保证「数据归属 ID 一律来自已认证身份，绝不由请求参数传入」。</p>
 */
public interface CurrentUserResolver {

    /**
     * 返回当前登录用户的 ID。
     *
     * @return 用户 ID，未登录时抛 401 异常
     */
    Long currentUserId();
}
