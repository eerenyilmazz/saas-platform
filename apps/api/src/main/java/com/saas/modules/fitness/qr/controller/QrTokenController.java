package com.saas.modules.fitness.qr.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saas.core.tenant.RequiresModule;
import com.saas.modules.fitness.FitnessModule;
import com.saas.modules.fitness.qr.dto.QrTokenResponse;
import com.saas.modules.fitness.qr.service.QrTokenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/fitness/members/{memberId}/qr-token")
@RequiresModule(FitnessModule.NAME)
@RequiredArgsConstructor
public class QrTokenController {

    private final QrTokenService qrTokenService;

    @PostMapping
    public QrTokenResponse issue(@PathVariable UUID memberId) {
        return new QrTokenResponse(qrTokenService.issue(memberId));
    }
}
