package com.petclinic.vet.configuration;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;

@Configuration
public class R2DBCConfiguration extends AbstractR2dbcConfiguration {
        @Bean
        public H2ConnectionFactory connectionFactory() {
            return new H2ConnectionFactory(
                    H2ConnectionConfiguration.builder()
                            .url("mem:vet-db")
                            .username("sa")
                            .build());
        }
    }

