package com.saas.modules.fitness.qr.service;

import java.util.UUID;

public interface QrTokenService {

    String issue(UUID memberId);

    UUID verify(String rawToken);
}
