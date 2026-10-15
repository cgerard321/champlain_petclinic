package com.petclinic.products.datalayer.ratings;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface ReviewSettingsRepository extends R2dbcRepository<ReviewSettings, Long> {
}
