package com.saas.modules.fitness.checkin.service;

import com.saas.modules.fitness.checkin.dto.CheckinResponse;

public interface FitnessCheckinService {

    CheckinResponse checkin(String rawToken);
}
