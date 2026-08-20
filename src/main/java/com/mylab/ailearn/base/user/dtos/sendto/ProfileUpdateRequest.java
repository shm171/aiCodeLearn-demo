package com.mylab.ailearn.base.user.dtos.sendto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileUpdateRequest {

    @NotBlank(message = "username is blank")
    @Size(max = 50, message = "username length must not exceed 50")
    private String username;
}
