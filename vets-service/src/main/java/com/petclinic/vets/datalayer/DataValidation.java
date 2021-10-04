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

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

public class DataValidation {


    private static final Logger LOG = LoggerFactory.getLogger(Vet.class);



    public static String verifyFirstName(String firstName){
        String confirmedValue = "";
            firstName = firstName.replaceAll("( |\\d)", "");
            firstName.substring(0, Math.min(firstName.length(), 30));
            Pattern p = Pattern.compile("^([A-Z]|[a-z]|\\\\.| |,|-)+");
            Matcher m = p.matcher(firstName);
            boolean b = m.matches();
            if(b) {
                confirmedValue = firstName.trim();
            }
            else {
                throw new InvalidInputException("Invalid input for first name");
            }
        return confirmedValue;
    }

    public static String verifySpeciality(String speciality){
        String confirmedValue = "";
            speciality = speciality.replaceAll("( |\\d)", "");
            speciality.substring(0, Math.min(speciality.length(), 80));
            Pattern p = Pattern.compile("^([A-Z]|[a-z]|\\\\.| |,|-)+");
            Matcher m = p.matcher(speciality);
            boolean b = m.matches();
            if(b) {
                confirmedValue = speciality.trim();
            }
            else {
            throw new InvalidInputException("Invalid input for speciality");
        }
        return confirmedValue;
    }

    public static String verifyLastName(String lastName){
        String confirmedValue = "";
            lastName = lastName.replaceAll("( |\\d)", "");
            lastName.substring(0, Math.min(lastName.length(), 30));
            Pattern p = Pattern.compile("^([A-Z]|[a-z]|\\\\.| |,|-)+");
            Matcher m = p.matcher(lastName);
            boolean b = m.matches();
            if(b) {
                confirmedValue = lastName.trim();
            }
            else {
                throw new InvalidInputException("Invalid input for last name");
            }
        return confirmedValue;
    }

    public static String verifyPhoneNumber(String phoneNumber){
        String confirmedValue = "";
            phoneNumber = phoneNumber.replaceAll("( |#|\\D)", "");
            phoneNumber.substring(0, Math.min(phoneNumber.length(), 30));
            Pattern p = Pattern.compile("^(\\d){4}$");
            Matcher m = p.matcher(phoneNumber);
            boolean b = m.matches();
            if(b) {
                confirmedValue = "(514)-634-8276 #"+phoneNumber.trim();
            }
            else {
            throw new InvalidInputException("Invalid input for phone number");
        }
        return confirmedValue;
    }

    public static String verifyWorkday(String workday){
        String confirmedValue = "";
            workday = workday.replaceAll("( )", "");
            workday = workday.replaceAll("(,)", ", ");
            workday.substring(0, Math.min(workday.length(), 250));
            Pattern p = Pattern.compile("((\\bMonday\\b|\\bTuesday\\b|\\bWednesday\\b|\\bThursday\\b|\\bFriday\\b|\\bSaturday\\b|\\bSunday\\b)(,|)( |))+");
            Matcher m = p.matcher(workday);
            boolean b = m.matches();
            if(b) {
                confirmedValue = workday.trim();
            }
            else {
                throw new InvalidInputException("Invalid input for work days");
            }
        return confirmedValue;
    }

    public static String verifyEmail(String email){
        String confirmedValue = "";
            email = email.replaceAll("( |)", "");
            email.substring(0, Math.min(email.length(), 100));
            Pattern p = Pattern.compile("\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b");
            Matcher m = p.matcher(email);
            boolean b = m.matches();
            if(b) {
                confirmedValue = email.trim();
            }
            else {
                throw new InvalidInputException("Invalid input for email");
            }
        return confirmedValue;
    }

    public static Integer verifyVetId(int vetId){
        int confirmedValue =0;
        if(vetId < 1){
            throw new InvalidInputException("Vet Id number has an invalid format"+vetId);
        }
        else if(Math.log10(vetId) < 7) {
                confirmedValue = vetId;
        }
        else if (Math.log10(vetId) > 6){
                while (Math.log10(vetId) > 6){
                    vetId = vetId /10;
                }
                confirmedValue = vetId;
        }
        else {
                throw new InvalidInputException("Invalid input for vetId");
        }
        return confirmedValue;
    }

    public static Integer verifyIsActive(int isActive){
        int confirmedValue =0;
            if (isActive > -1 && isActive < 2) {
                confirmedValue = isActive;
            }
            else {
                throw new InvalidInputException("Invalid input for isActive");
            }
        return confirmedValue;
    }
}
