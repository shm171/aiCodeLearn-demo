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


/** 从 SecurityContext 取登录邮箱，查库换出真实用户 ID。 */
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
