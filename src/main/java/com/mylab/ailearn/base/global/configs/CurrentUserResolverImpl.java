package com.mylab.ailearn.base.global.configs;

import org.springframework.stereotype.Component;

/**
 * 开发期占位实现，保证编译通过。
 * TODO: 由队友替换为正式实现（从 JWT 解析 userId）。
 */
@Component
public class CurrentUserResolverImpl implements CurrentUserResolver {

    @Override
    public Long currentUserId() {
        
        return 1L;
    }
}
