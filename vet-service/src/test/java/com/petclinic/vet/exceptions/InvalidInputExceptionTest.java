package com.petclinic.vet.exceptions;

import com.petclinic.vet.dataaccesslayer.photos.PhotoRepository;
import com.petclinic.vet.utils.exceptions.InvalidInputException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.r2dbc.init.R2dbcScriptDatabaseInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class InvalidInputExceptionTest {
    //To counter missing bean error
    @MockBean
    ConnectionFactoryInitializer connectionFactoryInitializer;
    @MockBean
    R2dbcScriptDatabaseInitializer r2dbcScriptDatabaseInitializer;

    @Test
    void TestInvalidInputEmptyConstructor() {
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException();
        });
    }
    @Test
    void TestInvalidInputString() {
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException("Something went wrong");
        });
    }
    @Test
    void TestInvalidInputCauseOnlyConstructor() {
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException(new Exception());
        });
    }

    @Test
    void TestInvalidInputMessageAndCauseConstructor() {
        assertThrows(InvalidInputException.class, () -> {
            throw new InvalidInputException("Something went wrong", new Exception());
        });
    }
}