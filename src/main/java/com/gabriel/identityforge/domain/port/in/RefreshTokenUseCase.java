package com.gabriel.identityforge.domain.port.in;

import com.gabriel.identityforge.application.dto.request.RefreshTokenRequest;
import com.gabriel.identityforge.application.dto.response.TokenResponse;

public interface RefreshTokenUseCase {
    TokenResponse refresh(RefreshTokenRequest request);
}
