package com.petclinic.vets.presentationlayer;

import com.petclinic.vets.utils.http.ServiceUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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
}
