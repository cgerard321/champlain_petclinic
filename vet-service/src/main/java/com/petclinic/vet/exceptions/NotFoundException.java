package com.petclinic.vet.exceptions;
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

public class NotFoundException extends RuntimeException{
    public NotFoundException() {}
    public NotFoundException(String message) { super(message); }
    public NotFoundException(Throwable cause) { super(cause); }
    public NotFoundException(String message, Throwable cause) {
        super(message, cause); }
}
