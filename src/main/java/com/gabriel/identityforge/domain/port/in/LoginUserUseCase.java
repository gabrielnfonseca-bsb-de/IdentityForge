package com.gabriel.identityforge.domain.port.in;

import com.gabriel.identityforge.application.dto.request.LoginRequest;
import com.gabriel.identityforge.application.dto.response.LoginResponse;

public interface LoginUserUseCase {
    LoginResponse execute(LoginRequest request);
}
