package com.saas.core.auth.service;

import com.saas.core.auth.entity.User;

public interface JwtService {

    String issueAccessToken(User user);

    AccessTokenClaims parseAccessToken(String token);
}
