package com.petclinic.bffapigateway.dtos.Environment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * <h1>Environment metadata for the running api-gateway instance.</h1>
 *
 * <p>
 * This DTO is exposed through a fully anonymous endpoint so that a build can be fingerprinted
 * from the outside. It allows verifying which deployment (local, staging, prod) a given frontend
 * build is talking to without any authentication.
 * </p>
 *
 * <p>
 * <strong>Never place secrets in this response.</strong> It is intentionally reachable by anonymous
 * users so any value returned here is public.
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnvironmentInfoResponseDTO {

    /**
     * The application name, useful to confirm which service answered the request.
     */
    private String application;

    /**
     * The logical environment name, ex: local, staging, prod. Driven by the active spring profile.
     */
    private String environment;

    /**
     * The raw active spring profiles.
     */
    private String activeProfiles;

    /**
     * The server port the gateway is listening on.
     */
    private int port;

    /**
     * The hostname of the running instance.
     */
    private String hostName;

    /**
     * A unique build/commit identifier. This is what proves a deployed build is or is not
     * running a given change.
     */
    private String buildVersion;

    /**
     * A human readable label for the build, ex: the git short sha or a docker tag.
     */
    private String buildLabel;

    /**
     * The build timestamp, ISO-8601. Useful to see how stale a deployment is.
     */
    private String buildTime;

    /**
     * The moment the gateway actually started serving traffic, ISO-8601.
     */
    private String startedAt;

    /**
     * How long the gateway has been up, ISO-8601 duration.
     */
    private String uptime;

    /**
     * The java version the gateway runs on.
     */
    private String javaVersion;

    /**
     * The configured frontend origins allowed to call this gateway.
     */
    private String allowedFrontendOrigins;

    /**
     * The git commit the build was made from, when available.
     */
    private String gitCommit;

    /**
     * The git branch the build was made from, when available.
     */
    private String gitBranch;

    /**
     * Server side timestamp, ISO-8601. Lets the frontend detect clock skew.
     */
    private String serverTime;

    /**
     * Arbitrary extra key/value pairs for environment specific markers.
     */
    private Map<String, String> markers;
}
