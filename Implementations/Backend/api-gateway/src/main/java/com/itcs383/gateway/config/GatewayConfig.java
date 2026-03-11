package com.itcs383.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Gateway configuration – provides the default {@link KeyResolver} required by
 * the {@code RequestRateLimiter} filter declared in application.yml.
 *
 * <p>Without a {@code KeyResolver} bean Spring Cloud Gateway falls back to
 * {@code PrincipalNameKeyResolver}, which returns an empty key for
 * unauthenticated requests and causes <strong>403 FORBIDDEN (EMPTY_KEY)</strong>
 * to be returned to the caller.
 *
 * <p>This implementation rates-limits by remote IP address, which works for
 * all requests regardless of authentication state.
 */
@Configuration
public class GatewayConfig {

    /**
     * IP-address-based key resolver used by every {@code RequestRateLimiter}
     * filter in the gateway routes.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(ip);
        };
    }
}
