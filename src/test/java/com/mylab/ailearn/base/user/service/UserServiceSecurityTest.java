package com.mylab.ailearn.base.user.service;

import com.mylab.ailearn.base.entity.AppUser;
import com.mylab.ailearn.base.repository.ProfileRepository;
import com.mylab.ailearn.base.repository.UserRepository;
import com.mylab.ailearn.base.user.dtos.sendto.UserUpdateRequest;
import com.mylab.ailearn.base.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceSecurityTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserService service = new UserService(
            userRepository,
            mock(ProfileRepository.class),
            mock(UserMapper.class),
            passwordEncoder);

    @Test
    void sensitiveAccountUpdateRejectsIncorrectCurrentPassword() {
        AppUser user = new AppUser();
        user.setId(7L);
        user.setEmail("old@example.com");
        user.setPasswordHash("stored-hash");
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "stored-hash")).thenReturn(false);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setCurrentPassword("wrong-password");
        request.setEmail("new@example.com");

        assertThatThrownBy(() -> service.updateUser(7L, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException statusException = (ResponseStatusException) exception;
                    org.assertj.core.api.Assertions.assertThat(statusException.getStatusCode().value())
                            .isEqualTo(403);
                });
        verify(userRepository, never()).save(user);
    }
}
