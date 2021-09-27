package com.petclinic.vets.datalayer;

import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.beans.support.MutableSortDefinition;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.core.style.ToStringCreator;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.xml.bind.annotation.XmlElement;
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
public class Vet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "vet_id")
    private Integer vetId;

    @Column(name = "first_name")
    @NotEmpty
    private String firstName;

    @Column(name = "last_name")
    @NotEmpty
    private String lastName;

    @Column(name = "email")
    @NotEmpty
    private String email;

    @Column(name = "phone_number")
    @NotEmpty
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
        firstName = firstName.replaceAll("( |\\d)", "");
        Pattern p = Pattern.compile("^(a-z| |,|.|-)+");
        Matcher m = p.matcher(firstName);
        boolean b = m.matches();
        if(b) {
            String confirmedValue = firstName.trim();
            return confirmedValue;
        }
        else{
            return "Invalid First Name";
        }
    }


    public String verifyLastName(String lastName){
        lastName = lastName.replaceAll("( |\\d)", "");
        Pattern p = Pattern.compile("^(a-z| |,|.|-)+");
        Matcher m = p.matcher(lastName);
        boolean b = m.matches();
        if(b) {
            String confirmedValue = lastName.trim();
            return confirmedValue;
        }
        else{
            return "Invalid Last Name";
        }
    }

    public String verifyPhoneNumber(String phoneNumber){
            phoneNumber = phoneNumber.replaceAll("( |#|\\D)", "");
            Pattern p = Pattern.compile("^(\\d){4}$");
            Matcher m = p.matcher(phoneNumber);
            boolean b = m.matches();
            if(b) {
                String confirmedValue = phoneNumber.trim();
                return "(514)-634-8276 #"+confirmedValue;
            }
            else{
            return "Invalid phone number";
        }
    }

    public String verifyWorkday(String workday){
        workday = workday.replaceAll("( )", "");
        workday = workday.replaceAll("(,)", ", ");
        Pattern p = Pattern.compile("((\\bMonday\\b|\\bTuesday\\b|\\bWednesday\\b|\\bThursday\\b|\\bFriday\\b|\\bSaturday\\b|\\bSunday\\b)(,|)( |))+");
        Matcher m = p.matcher(workday);
        boolean b = m.matches();
        if(b) {
            String confirmedValue = workday.trim();
            return confirmedValue;
        }
        else{
            return "Invalid Workday Value";
        }
    }

    public String verifyEmail(String email){
        email = email.replaceAll("( |)", "");
        Pattern p = Pattern.compile("\\b[\\w.%-]+@[-.\\w]+\\.[A-Za-z]{2,4}\\b");
        Matcher m = p.matcher(email);
        boolean b = m.matches();
        if(b) {
            String confirmedValue = email.trim();
            return confirmedValue;
        }
        else{
            return "Invalid Email";
        }
    }

    public Integer verifyVetId(int vetId){
        if(Math.log10(vetId) < 7) {
            int confirmedValue = vetId;
            return confirmedValue;
        }
        else{
            while (Math.log10(vetId) > 6){
                vetId = vetId /10;
            }
            int confirmedValue = vetId;
            return confirmedValue;
        }
    }

    public Integer verifyIsActive(int isActive){
        int confirmedValue = 1;
        if (isActive > -1 && isActive < 2){
            confirmedValue = isActive;
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
}