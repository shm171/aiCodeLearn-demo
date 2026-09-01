package com.mylab.ailearn.base.global.configs;

/**
 * 获取当前登录用户的 ID。
 *
 * <p>由 base 模块实现，core 模块依赖此接口获取 ownerUserId。
 *
 * <p>TODO: 正式实现应从 JWT 解析 userId 并注入 SecurityContext。
 *
  **/
public interface CurrentUserResolver {

    /**
     * 返回当前登录用户的 ID。
     *
     * @return 用户 ID，未登录时抛异常
     */
    Long currentUserId();
}
