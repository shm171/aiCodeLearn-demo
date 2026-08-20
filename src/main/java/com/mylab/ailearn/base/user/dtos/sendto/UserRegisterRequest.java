package com.mylab.ailearn.base.user.dtos.sendto;

import com.mylab.ailearn.base.global.enums.RegistrationRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {

    @NotBlank(message = "email is blank")
    @Email(message = "not valid email format")
    private String email;

    @NotBlank
    @Size(min = 8, max = 16, message = "length must be in 8 to 16")
    private String password;

    @NotBlank(message = "username is blank")
    @Size(max = 50, message = "username length must not exceed 50")
    private String username;

    @NotNull(message = "role is required")
    private RegistrationRole role;
}
