package com.petclinic.bffapigateway.utils;
import com.petclinic.bffapigateway.exceptions.InvalidInputException;
import lombok.Generated;


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
