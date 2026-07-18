package com.saas.core.auth.service;

import com.saas.core.auth.entity.User;

public interface RefreshTokenService {

    String issue(User user);

    User validateAndConsume(String refreshToken);

    void revoke(String refreshToken);
}
