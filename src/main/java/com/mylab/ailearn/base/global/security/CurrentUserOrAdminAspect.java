package com.mylab.ailearn.base.global.security;

import com.mylab.ailearn.base.global.configs.CurrentUserResolver;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

/** 将用户资源的所有权校验集中在控制器边界，避免各接口重复并遗漏权限判断。 */
@Aspect
@Component
@RequiredArgsConstructor
public class CurrentUserOrAdminAspect {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private final CurrentUserResolver currentUserResolver;

    @Before("@annotation(rule)")
    public void verifyAccess(JoinPoint joinPoint, CurrentUserOrAdmin rule) {
        if (isAdmin()) {
            return;
        }

        Object[] arguments = joinPoint.getArgs();
        int index = rule.idArgumentIndex();
        if (index < 0 || index >= arguments.length || !(arguments[index] instanceof Number requestedId)) {
            throw new IllegalStateException("@CurrentUserOrAdmin must reference a numeric user ID argument");
        }

        if (!Objects.equals(currentUserResolver.currentUserId(), requestedId.longValue())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问其他用户的数据");
        }
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> ADMIN_AUTHORITY.equals(authority.getAuthority()));
    }
}
