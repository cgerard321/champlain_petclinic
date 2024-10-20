package com.petclinic.products.domainclientlayer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequestModel {
    private String EmailToSendTo;
    private String EmailTitle;
    private String TemplateName;
    private String Header;
    private String Body;
    private String Footer;
    private String CorrespondantName;
    private String SenderName;
}
