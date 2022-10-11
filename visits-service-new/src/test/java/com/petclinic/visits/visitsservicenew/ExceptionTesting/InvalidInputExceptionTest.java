package com.petclinic.visits.visitsservicenew.ExceptionTesting;

import com.petclinic.visits.visitsservicenew.Exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class InvalidInputExceptionTest {

    @Test
    void TestInvalidInputEmptyConstructor(){
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException();
        });
    }
    @Test
    void TestInvalidInputString(){
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException("An Error Occurred");
        });
    }
    @Test
    void TestInvalidInputCauseOnlyConstructor(){
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException(new Exception());
        });
    }
    @Test
    void TestInvalidInputMessageCauseConstructor(){
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException("An error Occurred", new Exception());
        });
    }


}
