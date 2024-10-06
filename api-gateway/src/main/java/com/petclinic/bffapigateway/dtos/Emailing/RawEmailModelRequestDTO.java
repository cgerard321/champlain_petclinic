package com.petclinic.bffapigateway.dtos.Emailing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RawEmailModelRequestDTO {
    private String EmailToSendTo;
    private String EmailTitle;
    private String Body;

}
