package com.saas.modules.fitness.qr;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.fitness.qr")
@Getter
@Setter
public class FitnessQrProperties {

    private Duration tokenTtl = Duration.ofSeconds(30);
}
