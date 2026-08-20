package com.mylab.ailearn.base.security.controller;

import com.mylab.ailearn.base.security.dto.LoginRequest;
import com.mylab.ailearn.base.security.dto.TokenDto;
import com.mylab.ailearn.base.security.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
@Tag(name = "登录认证", description = "邮箱密码登录和 JWT 校验接口")
public class LoginController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping
    @Operation(summary = "用户登录", description = "使用邮箱和密码换取 JWT")
    public ResponseEntity<TokenDto> login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        String token = jwtService.generateToken(authentication.getName());
        return ResponseEntity.ok(new TokenDto(token));
    }

    @PostMapping("/validate")
    @Operation(summary = "校验 JWT", description = "在 Authorization 请求头中传入 Bearer Token")
    public boolean validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        if (!authorization.startsWith(BEARER_PREFIX)) {
            return false;
        }
        return jwtService.validateToken(authorization.substring(BEARER_PREFIX.length()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Void> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
