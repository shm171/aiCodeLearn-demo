package com.mylab.ailearn.base.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "email is blank")
    @Email(message = "not valid email format")
    private String email;

    @NotBlank(message = "password is blank")
    @Size(min = 8, max = 16, message = "length must be in 8 to 16")
    private String password;
}
