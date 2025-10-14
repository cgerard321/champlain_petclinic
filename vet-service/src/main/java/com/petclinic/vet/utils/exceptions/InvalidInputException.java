package com.petclinic.vet.utils.exceptions;
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

public class InvalidInputException  extends RuntimeException{
    public InvalidInputException() {}
    public InvalidInputException(String message) { super(message); }
    public InvalidInputException(Throwable cause) { super(cause); }
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause); }
}
