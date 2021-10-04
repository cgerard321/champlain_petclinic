package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.utils.exceptions.NotFoundException;
import com.petclinic.vets.utils.http.GlobalControllerExceptionHandler;
import com.petclinic.vets.utils.http.HttpErrorInfo;
import com.petclinic.vets.utils.http.ServiceUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;


/**
 * Simple Http Utils tests
 *
 * @author Christian
 */
public class UtilsHttpTest {

    final ServiceUtil su = new ServiceUtil("7002");
    @Test
    @DisplayName("Service Utils Test")
    void serviceUtilsTest(){

        assertNotNull(su.getServiceAddress());
    }
    @Test
    @DisplayName("Http Error Info test")
    void httpErrorInfoUtilsTest(){
        HttpErrorInfo heiEmpty =new HttpErrorInfo();
        assertEquals(heiEmpty.getMessage(),null);
        assertEquals(heiEmpty.getPath(),null);
        assertEquals(heiEmpty.getHttpStatus(),null);
        assertThat(heiEmpty.getTimestamp()).isNull();
        HttpErrorInfo hei = new HttpErrorInfo(UNPROCESSABLE_ENTITY,"/","Invalid item");
        assertEquals(hei.getMessage(),"Invalid item");
        assertEquals(hei.getPath(),"/");
        assertEquals(hei.getHttpStatus(),UNPROCESSABLE_ENTITY);
        assertThat(hei.getTimestamp()).isNotNull();
    }
}
