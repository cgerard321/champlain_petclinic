package com.petclinic.bffapigateway.utils;
/**
 @author Kamilah Hatteea & Brandon Levis : Vet-Service
  * Worked together with (Code with Friends) on IntelliJ IDEA
  * <p>
  * User: @Kamilah Hatteea
  * Date: 2022-09-22
  * Ticket: feat(VVS-CPC-554): edit veterinarian
  * User: Brandon Levis
  * Date: 202
  * Ticket: feat(VVS-CPC-553): add veterinarian
 */

import com.petclinic.bffapigateway.dtos.VetDTO;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import lombok.Generated;
import org.springframework.beans.BeanUtils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class VetsEntityDtoUtil {

    @Generated
    public VetsEntityDtoUtil(){}

    public static String verifyId(String id) {
        try {
            Integer.parseInt(id);
        }
        catch(NumberFormatException e) {
            throw new InvalidInputException("This id is not valid");
        }
        return id;
    }
}
