package com.saas.modules.fitness.checkin.service.impl;

import org.springframework.stereotype.Service;

import com.saas.core.exception.BusinessRuleException;
import com.saas.core.i18n.Messages;
import com.saas.core.membership.entity.MembershipStatus;
import com.saas.core.membership.repository.MemberRepository;
import com.saas.core.membership.repository.MembershipRepository;
import com.saas.modules.fitness.checkin.dto.CheckinResponse;
import com.saas.modules.fitness.checkin.entity.CheckinLog;
import com.saas.modules.fitness.checkin.repository.CheckinLogRepository;
import com.saas.modules.fitness.checkin.service.FitnessCheckinService;
import com.saas.modules.fitness.qr.service.QrTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FitnessCheckinServiceImpl implements FitnessCheckinService {

    private final QrTokenService qrTokenService;
    private final MemberRepository memberRepository;
    private final MembershipRepository membershipRepository;
    private final CheckinLogRepository checkinLogRepository;
    private final Messages messages;

    @Override
    public CheckinResponse checkin(String rawToken) {
        var memberId = qrTokenService.verify(rawToken);

        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessRuleException("FITNESS_QR_INVALID", messages.get("FITNESS_QR_INVALID")));

        var activeMembership = membershipRepository.findByMemberIdAndStatus(memberId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new BusinessRuleException(
                        "FITNESS_MEMBERSHIP_INACTIVE", messages.get("FITNESS_MEMBERSHIP_INACTIVE")));

        var checkinLog = checkinLogRepository.save(
                new CheckinLog(member.getTenantId(), memberId, activeMembership.getId()));
        return new CheckinResponse(memberId, checkinLog.getCheckedInAt());
    }
}
