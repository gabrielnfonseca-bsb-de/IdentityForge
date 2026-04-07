package com.gabriel.identityforge.interfaces.rest;

import com.gabriel.identityforge.application.dto.request.RefreshTokenRequest;
import com.gabriel.identityforge.application.dto.request.RegisterRequest;
import com.gabriel.identityforge.application.dto.response.LoginResponse;
import com.gabriel.identityforge.application.dto.request.LoginRequest;
import com.gabriel.identityforge.application.dto.response.RegisterResponse;
import com.gabriel.identityforge.application.dto.response.TokenResponse;
import com.gabriel.identityforge.domain.port.in.LoginUserUseCase;
import com.gabriel.identityforge.domain.port.in.RefreshTokenUseCase;
import com.gabriel.identityforge.domain.port.in.RegisterUserUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUserUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final RegisterUserUseCase registerUserUseCase;

    public AuthController(
            LoginUserUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            RegisterUserUseCase registerUserUseCase
    ) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(registerUserUseCase.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginUseCase.login(request));
    }
}
