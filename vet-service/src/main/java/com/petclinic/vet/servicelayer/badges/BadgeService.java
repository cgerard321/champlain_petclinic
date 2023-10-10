package com.petclinic.vet.servicelayer.badges;

import reactor.core.publisher.Mono;

public interface BadgeService {
    Mono<BadgeResponseDTO> getBadgeByVetId(String vetId);
}
