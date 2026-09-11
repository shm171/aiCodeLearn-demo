package com.mylab.ailearn.base.global.configs;

import com.mylab.ailearn.base.entity.AppUser;
import com.mylab.ailearn.base.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * 从 Spring Security 上下文解析当前登录用户的真实 ID。
 *
 * <p>JwtAuthenticationFilter 校验 Token 后会把以 email 为用户名的 UserDetails
 * 写入 SecurityContext；这里再按 email 查库取回稳定的用户 ID，
 * 保证 core 模块拿到的 ownerUserId 一定属于真实登录用户。</p>
 */
@Component
@RequiredArgsConstructor
public class CurrentUserResolverImpl implements CurrentUserResolver {

    private final UserRepository userRepository;

    @Override
    public Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "未登录或凭证无效");
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .map(AppUser::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录用户不存在"));
    }
}
