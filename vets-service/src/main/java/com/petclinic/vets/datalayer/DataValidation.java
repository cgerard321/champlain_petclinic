package com.petclinic.vets.datalayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vets.utils.exceptions.InvalidInputException;
import com.petclinic.vets.utils.exceptions.NotFoundException;
import com.petclinic.vets.utils.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.style.ToStringCreator;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataValidation {


    private static final Logger LOG = LoggerFactory.getLogger(Vet.class);



    public static String verifyFirstName(String firstName){
        String confirmedValue = "";
        try {
            firstName = firstName.replaceAll("( |\\d)", "");
            Pattern p = Pattern.compile("^(a-z| |,|.|-)+");
            Matcher m = p.matcher(firstName);
            boolean b = m.matches();
            if(b) {
                confirmedValue = firstName.trim();
            }
        }
        catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
        return confirmedValue;
    }


    public static String verifyLastName(String lastName){
        String confirmedValue = "";
        try {
            lastName = lastName.replaceAll("( |\\d)", "");
            Pattern p = Pattern.compile("^(a-z| |,|.|-)+");
            Matcher m = p.matcher(lastName);
            boolean b = m.matches();
            if(b) {
                confirmedValue = lastName.trim();
            }
        }
        catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
        return confirmedValue;
    }

    public static String verifyPhoneNumber(String phoneNumber){
        String confirmedValue = "";
        try {
            phoneNumber = phoneNumber.replaceAll("( |#|\\D)", "");
            Pattern p = Pattern.compile("^(\\d){4}$");
            Matcher m = p.matcher(phoneNumber);
            boolean b = m.matches();
            if(b) {
                confirmedValue = phoneNumber.trim();
            }
        }
        catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
        return "(514)-634-8276 #"+confirmedValue;
    }

    public static String verifyWorkday(String workday){
        String confirmedValue = "";
        try {
            workday = workday.replaceAll("( )", "");
            workday = workday.replaceAll("(,)", ", ");
            Pattern p = Pattern.compile("((\\bMonday\\b|\\bTuesday\\b|\\bWednesday\\b|\\bThursday\\b|\\bFriday\\b|\\bSaturday\\b|\\bSunday\\b)(,|)( |))+");
            Matcher m = p.matcher(workday);
            boolean b = m.matches();
            if(b) {
                confirmedValue = workday.trim();
            }
        }
        catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
        return confirmedValue;
    }

    public static String verifyEmail(String email){
        String confirmedValue = "";
        try {
            email = email.replaceAll("( |)", "");
            Pattern p = Pattern.compile("\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b");
            Matcher m = p.matcher(email);
            boolean b = m.matches();
            if(b) {
                confirmedValue = email.trim();
            }
        }
        catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
        return confirmedValue;
    }

    public static Integer verifyVetId(int vetId){
        int confirmedValue =0;
        try {
            if(Math.log10(vetId) < 7) {
                confirmedValue = vetId;
            }
            else{
                while (Math.log10(vetId) > 6){
                    vetId = vetId /10;
                }
                confirmedValue = vetId;
            }
        }
        catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
        return confirmedValue;
    }

    public static Integer verifyIsActive(int isActive){
        int confirmedValue =0;
        try {
            confirmedValue = 1;
            if (isActive > -1 && isActive < 2) {
                confirmedValue = isActive;
            }
        }
        catch (HttpClientErrorException ex){
            throw handleHttpClientException(ex);
        }
        return confirmedValue;
    }

    private static RuntimeException handleHttpClientException(HttpClientErrorException ex){
        switch (ex.getStatusCode()){
            case NOT_FOUND:
                throw new NotFoundException(getErrorMessage(ex));
            case UNPROCESSABLE_ENTITY:
                throw new InvalidInputException(getErrorMessage(ex));
            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusText());
                LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                throw ex;
        }
    }
    private static String getErrorMessage(HttpClientErrorException ex) {
        final ObjectMapper mapper = null;
        try{
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();

        }catch(IOException ioex){
            return ioex.getMessage();

        }
    }
}
