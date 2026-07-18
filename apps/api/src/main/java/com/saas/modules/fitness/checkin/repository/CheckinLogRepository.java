package com.saas.modules.fitness.checkin.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saas.modules.fitness.checkin.entity.CheckinLog;

public interface CheckinLogRepository extends JpaRepository<CheckinLog, UUID> {
}
