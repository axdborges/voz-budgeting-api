package com.axdborges.voz.budgeting.infrastructure.http.response;

import org.springframework.boot.info.BuildProperties;

import java.time.Instant;

public record VersionResponse(String name, String version, Instant buildTime) {

    public static VersionResponse from(BuildProperties buildProperties) {
        return new VersionResponse(buildProperties.getName(), buildProperties.getVersion(), buildProperties.getTime());
    }
}
