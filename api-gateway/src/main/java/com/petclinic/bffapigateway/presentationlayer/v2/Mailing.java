package com.petclinic.bffapigateway.presentationlayer.v2;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v2/gateway/owners")
@Validated
@CrossOrigin(origins = "http://localhost:3000, http://localhost:80")
public class Mailing {

}
