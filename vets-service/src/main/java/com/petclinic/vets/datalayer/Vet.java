package com.petclinic.vets.datalayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.vets.utils.exceptions.InvalidInputException;
import com.petclinic.vets.utils.exceptions.NotFoundException;
import com.petclinic.vets.utils.http.GlobalControllerExceptionHandler;
import com.petclinic.vets.utils.http.HttpErrorInfo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.UniqueElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.core.style.ToStringCreator;
import org.springframework.web.client.HttpClientErrorException;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple JavaBean domain object representing a veterinarian.
 *
 * @author Ken Krebs
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @author Arjen Poutsma
 * @author Maciej Szarlinski
 * Copied from https://github.com/spring-petclinic/spring-petclinic-microservices
 */

@Entity
@Table(name = "vets")
@NoArgsConstructor
@AllArgsConstructor
public class Vet {

    private static final Logger LOG = LoggerFactory.getLogger(Vet.class);
    private final ObjectMapper mapper = null;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "vet_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    @UniqueElements(groups = Vet.class)
    private Integer vetId;

    @Column(name = "first_name")
    @NotEmpty
    private String firstName;

    @Column(name = "last_name")
    @NotEmpty
    private String lastName;

    @Column(name = "email")
    @NotEmpty(message = "Please enter email")
    private String email;

    @Column(name = "phone_number")
    @NotEmpty(message = "Please enter phoneNumber")
    private String phoneNumber;

    @Column(name = "resume")
    private String resume;

    @Column(name = "workday")
    private String workday;

    @Column(name = "is_active")
    private Integer isActive;

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = verifyIsActive(isActive);
    }

    public Integer getVetId() {
        return vetId;
    }

    public void setVetId(Integer vetId) {
        this.vetId = verifyVetId(vetId);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = verifyEmail(email);
    }

    public String getPostNumber()
    {
        String postNumber = getPhoneNumber().replaceAll("\\(\\d{3}\\)-\\d{3}-\\d{4}", "");
        return postNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }


    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = verifyPhoneNumber(phoneNumber);
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getWorkday() {
        return workday;
    }

    public void setWorkday(String workday) {
        this.workday = verifyWorkday(workday);
    }


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "vet_specialties", joinColumns = @JoinColumn(name = "vet_id"),
            inverseJoinColumns = @JoinColumn(name = "specialty_id"))
    private Set<Specialty> specialties;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = verifyFirstName(firstName);
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = verifyLastName(lastName);
    }

    protected Set<Specialty> getSpecialtiesInternal() {
        if (this.specialties == null) {
            this.specialties = new HashSet<>();
        }
        return this.specialties;
    }

    @XmlElement
    public List<Specialty> getSpecialties() {
        List<Specialty> sortedSpecs = new ArrayList<>(getSpecialtiesInternal());
        PropertyComparator.sort(sortedSpecs, new MutableSortDefinition("name", true, true));
        return Collections.unmodifiableList(sortedSpecs);
    }

    public int getNrOfSpecialties() {
        return getSpecialtiesInternal().size();
    }

    public void addSpecialty(Specialty specialty) {
        getSpecialtiesInternal().add(specialty);
    }


    public String verifyFirstName(String firstName){
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


    public String verifyLastName(String lastName){
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

    public String verifyPhoneNumber(String phoneNumber){
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

    public String verifyWorkday(String workday){
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

    public String verifyEmail(String email){
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

    public Integer verifyVetId(int vetId){
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

    public Integer verifyIsActive(int isActive){
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

    @Override
    public String toString(){
        return new ToStringCreator(this).append("id", this.getId())
                .append("firstName", this.getFirstName())
                .append("lastName", this.getLastName())
                .append("email", this.getEmail())
                .append("phoneNumber", this.getPhoneNumber())
                .append("resume", this.getResume())
                .append("workday", this.getWorkday()).toString();
    }

    private RuntimeException handleHttpClientException(HttpClientErrorException ex){
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
    private String getErrorMessage(HttpClientErrorException ex) {

        try{
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();

        }catch(IOException ioex){
            return ioex.getMessage();

        }
    }
}