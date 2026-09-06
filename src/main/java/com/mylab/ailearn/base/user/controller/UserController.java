package com.mylab.ailearn.base.user.controller;

import com.mylab.ailearn.base.global.configs.OpenApiConfig;
import com.mylab.ailearn.base.user.dtos.sendback.ProfileDto;
import com.mylab.ailearn.base.user.dtos.sendback.UserDto;
import com.mylab.ailearn.base.user.dtos.sendto.ProfileUpdateRequest;
import com.mylab.ailearn.base.user.dtos.sendto.UserRegisterRequest;
import com.mylab.ailearn.base.user.dtos.sendto.UserUpdateRequest;
import com.mylab.ailearn.base.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户与档案", description = "用户注册、账号和 Profile 接口")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "注册用户", description = "公开接口，只允许注册 STUDENT 或 TEACHER")
    public ResponseEntity<UserDto> registerUser(
            @Valid @RequestBody UserRegisterRequest userRegisterRequest,
            UriComponentsBuilder uriBuilder
    ) {
        UserDto user = userService.register(userRegisterRequest);
        URI location = uriBuilder.path("/user/{id}").buildAndExpand(user.getId()).toUri();

        return ResponseEntity.created(location).body(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询账号", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<UserDto> getUser(@PathVariable long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/{id}/profile")
    @Operation(summary = "查询用户档案", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<ProfileDto> getProfile(@PathVariable long id) {
        return ResponseEntity.ok(userService.getProfileByUserId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改账号信息", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<UserDto> updateUser(
            @PathVariable long id,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest
    ) {
        return ResponseEntity.ok(userService.updateUser(id, userUpdateRequest));
    }

    @PutMapping("/{id}/profile")
    @Operation(summary = "修改档案用户名", description = "普通档案接口不能修改角色",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<ProfileDto> updateProfile(
            @PathVariable long id,
            @Valid @RequestBody ProfileUpdateRequest profileUpdateRequest
    ) {
        return ResponseEntity.ok(userService.updateProfile(id, profileUpdateRequest));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    public ResponseEntity<Void> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
