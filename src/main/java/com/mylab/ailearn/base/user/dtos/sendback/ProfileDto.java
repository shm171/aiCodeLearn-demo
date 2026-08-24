package com.mylab.ailearn.base.user.dtos.sendback;

import com.mylab.ailearn.base.global.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProfileDto {

    private final Long userId;
    private final String username;
    private final UserRole role;
    private final LocalDateTime createdAt;
}
