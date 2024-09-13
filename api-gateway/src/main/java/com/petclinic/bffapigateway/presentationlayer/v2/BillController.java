package com.petclinic.bffapigateway.presentationlayer.v2;

import com.petclinic.bffapigateway.domainclientlayer.BillServiceClient;
import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import com.petclinic.bffapigateway.utils.Security.Annotations.IsUserSpecific;
import com.petclinic.bffapigateway.utils.Security.Variables.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/bills")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class BillController {
    private final BillServiceClient billService;

    @IsUserSpecific(idToMatch = {"customerId"}, bypassRoles = {Roles.ADMIN})
    @GetMapping(value = "/customer/{customerId}", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BillResponseDTO> getBillsByOwnerId(final @PathVariable String customerId)
    {
        return billService.getBillsByOwnerId(customerId);
    }
}
