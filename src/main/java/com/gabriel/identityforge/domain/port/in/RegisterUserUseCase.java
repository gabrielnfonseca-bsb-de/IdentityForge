package com.gabriel.identityforge.domain.port.in;

import com.gabriel.identityforge.application.dto.request.RegisterRequest;
import com.gabriel.identityforge.application.dto.response.RegisterResponse;

public interface RegisterUserUseCase {
    RegisterResponse register(RegisterRequest request);
}
