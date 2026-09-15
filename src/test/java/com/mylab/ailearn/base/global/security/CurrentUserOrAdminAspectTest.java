package com.mylab.ailearn.base.global.security;

import com.mylab.ailearn.base.global.configs.CurrentUserResolver;
import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CurrentUserOrAdminAspectTest {

    private final CurrentUserResolver currentUserResolver = mock(CurrentUserResolver.class);
    private final CurrentUserOrAdminAspect aspect = new CurrentUserOrAdminAspect(currentUserResolver);
    private final JoinPoint joinPoint = mock(JoinPoint.class);
    private final CurrentUserOrAdmin rule = mock(CurrentUserOrAdmin.class);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void currentUserCanAccessOwnResource() {
        authenticateAs("ROLE_STUDENT");
        when(rule.idArgumentIndex()).thenReturn(0);
        when(joinPoint.getArgs()).thenReturn(new Object[]{7L});
        when(currentUserResolver.currentUserId()).thenReturn(7L);

        assertThatCode(() -> aspect.verifyAccess(joinPoint, rule)).doesNotThrowAnyException();
    }

    @Test
    void currentUserCannotAccessAnotherUsersResource() {
        authenticateAs("ROLE_STUDENT");
        when(rule.idArgumentIndex()).thenReturn(0);
        when(joinPoint.getArgs()).thenReturn(new Object[]{8L});
        when(currentUserResolver.currentUserId()).thenReturn(7L);

        assertThatThrownBy(() -> aspect.verifyAccess(joinPoint, rule))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void adminCanAccessWithoutAnOwnerLookup() {
        authenticateAs("ROLE_ADMIN");

        assertThatCode(() -> aspect.verifyAccess(joinPoint, rule)).doesNotThrowAnyException();
        verifyNoInteractions(currentUserResolver, joinPoint, rule);
    }

    private void authenticateAs(String authority) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com",
                "n/a",
                List.of(new SimpleGrantedAuthority(authority)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
