package com.mylab.ailearn.base.user.dtos.sendto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @Email(message = "not valid email format")
    private String email;

    @Size(min = 8, max = 16, message = "length must be in 8 to 16")
    private String password;
}
