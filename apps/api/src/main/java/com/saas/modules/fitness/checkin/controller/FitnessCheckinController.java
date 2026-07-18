package com.saas.modules.fitness.checkin.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.core.tenant.RequiresModule;
import com.saas.modules.fitness.FitnessModule;
import com.saas.modules.fitness.checkin.dto.CheckinRequest;
import com.saas.modules.fitness.checkin.dto.CheckinResponse;
import com.saas.modules.fitness.checkin.service.FitnessCheckinService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/fitness/checkins")
@RequiresModule(FitnessModule.NAME)
@RequiredArgsConstructor
public class FitnessCheckinController {

    private final FitnessCheckinService fitnessCheckinService;

    @PostMapping
    public CheckinResponse checkin(@Valid @RequestBody CheckinRequest request) {
        return fitnessCheckinService.checkin(request.token());
    }
}
