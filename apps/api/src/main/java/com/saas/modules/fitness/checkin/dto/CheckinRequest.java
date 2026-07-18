package com.saas.modules.fitness.checkin.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckinRequest(@NotBlank String token) {
}
