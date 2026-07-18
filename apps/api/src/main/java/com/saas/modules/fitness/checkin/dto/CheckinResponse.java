package com.saas.modules.fitness.checkin.dto;

import java.time.Instant;
import java.util.UUID;

public record CheckinResponse(UUID memberId, Instant checkedInAt) {
}
