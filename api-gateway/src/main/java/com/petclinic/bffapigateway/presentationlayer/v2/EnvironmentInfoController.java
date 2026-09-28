package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.dtos.Environment.EnvironmentInfoResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.SecuredEndpoint;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <h1>Exposes deployment metadata so a build can be identified without logging in.</h1>
 *
 * <p>
 * Every endpoint here is annotated {@link Roles#ANONYMOUS}, which sets the {@code whitelisted}
 * exchange attribute inside {@code JwtTokenFilter} and {@code CsrfFilter}. That means no JWT, no
 * roles and no CSRF token are required, so this can be hit straight from a browser or curl.
 * </p>
 *
 * <p>
 * The intent is to prove which environment a frontend is actually talking to. A build served from
 * the deployed environment will return {@code 404} for these paths while it runs the old image,
 * which is exactly the proof that the change has not reached production.
 * </p>
 *
 * <p><strong>Security note:</strong> this intentionally returns only non sensitive build
 * information. Do not add connection strings, tokens or host credentials to it.</p>
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Environment")
@RequestMapping("/api/v2/gateway/environment")
public class EnvironmentInfoController {

    private final Environment springEnvironment;

    private final ObjectProvider<GitProperties> gitPropertiesProvider;

    /**
     * The raw active spring profiles.
     */
    private String activeProfiles;

    /**
     * The build metadata below are optional. They are only populated when the
     * build injects them, ex: through springBoot.buildInfo. When they are
     * missing they simply report {@code not-set} rather than failing, so this
     * endpoint works no matter how the jar was built.
     */
    @Value("${build.version:not-set}")
    private String buildVersion;

    @Value("${build.label:not-set}")
    private String buildLabel;

    @Value("${build.time:not-set}")
    private String buildTime;

    /**
     * Comma separated list of frontend origins allowed to call the gateway.
     */
    @Value("${frontend.url:}")
    private String frontendOrigin;

    private final Instant startedAt = Instant.now();

    @PostConstruct
    private void captureActiveProfiles() {
        this.activeProfiles = String.join(",", springEnvironment.getActiveProfiles());
    }

    @SecuredEndpoint(allowedRoles = {Roles.ANONYMOUS})
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Get the environment metadata of this running gateway",
            description = "Public, unauthenticated endpoint used to fingerprint which environment "
                    + "a client is talking to. Returns non sensitive build information only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Environment metadata returned"),
            @ApiResponse(responseCode = "404", description = "This build does not contain the environment endpoint")
    })
    public Mono<ResponseEntity<EnvironmentInfoResponseDTO>> getEnvironmentInfo() {
        return Mono.fromSupplier(this::buildResponse)
                .map(ResponseEntity::ok)
                .doOnError(error -> log.error("Could not build the environment info response", error));
    }

    private EnvironmentInfoResponseDTO buildResponse() {
        return EnvironmentInfoResponseDTO.builder()
                .application("petclinic-api-gateway")
                .environment(resolveEnvironmentName())
                .activeProfiles(activeProfiles)
                .port(resolvePort())
                .hostName(resolveHostName())
                .buildVersion(buildVersion)
                .buildLabel(buildLabel)
                .buildTime(buildTime)
                .startedAt(startedAt.toString())
                .uptime(Duration.between(startedAt, Instant.now()).toString())
                .javaVersion(System.getProperty("java.version"))
                .allowedFrontendOrigins(frontendOrigin)
                .gitCommit(safeGitInfo(GitProperties::getCommitId))
                .gitBranch(safeGitInfo(GitProperties::getBranch))
                .serverTime(Instant.now().toString())
                .markers(buildMarkers())
                .build();
    }

    /**
     * The environment is derived from the active spring profile so no extra
     * configuration is required. {@code prod} is the only production profile in
     * this project, everything else is treated as a local or staging run.
     */
    private String resolveEnvironmentName() {
        for (String profile : springEnvironment.getActiveProfiles()) {
            if (profile.equalsIgnoreCase("prod")) {
                return "prod";
            }
            if (profile.equalsIgnoreCase("staging")) {
                return "staging";
            }
        }
        return "local";
    }

    private Map<String, String> buildMarkers() {
        Map<String, String> markers = new LinkedHashMap<>();
        markers.put("anonymousEndpoint", "true");
        markers.put("apiVersion", "v2");
        markers.put("profileCount", String.valueOf(springEnvironment.getActiveProfiles().length));
        return markers;
    }

    /**
     * {@link GitProperties} is only present when the git commit id plugin generated git.properties.
     * Reading through this helper keeps the endpoint working when the build ran outside git.
     */
    private String safeGitInfo(java.util.function.Function<GitProperties, String> extractor) {
        GitProperties gitProperties = gitPropertiesProvider.getIfAvailable();
        if (gitProperties == null) {
            return "unavailable";
        }
        try {
            String value = extractor.apply(gitProperties);
            return value == null || value.isBlank() ? "unavailable" : value;
        } catch (Exception e) {
            log.debug("Git metadata not available", e);
            return "unavailable";
        }
    }

    private int resolvePort() {
        String port = springEnvironment.getProperty("server.port");
        if (port == null) {
            return -1;
        }
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String resolveHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.debug("Could not resolve the host name", e);
            return "unknown";
        }
    }
}
