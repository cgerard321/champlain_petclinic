package com.petclinic.vet.businesslayer.badges;

import com.petclinic.vet.presentationlayer.badges.BadgeResponseDTO;

import reactor.core.publisher.Mono;

public interface BadgeService {
    Mono<BadgeResponseDTO> getBadgeByVetId(String vetId);
}
