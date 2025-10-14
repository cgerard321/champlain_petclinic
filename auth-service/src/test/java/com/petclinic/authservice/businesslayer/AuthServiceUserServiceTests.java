package com.petclinic.authservice.businesslayer;

import com.petclinic.authservice.Util.Exceptions.IncorrectPasswordException;
import com.petclinic.authservice.Util.Exceptions.NotFoundException;
import com.petclinic.authservice.Util.Exceptions.UnverifiedUserException;
import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.user.*;
import com.petclinic.authservice.datamapperlayer.UserMapper;
import com.petclinic.authservice.domainclientlayer.Mail.MailService;
import com.petclinic.authservice.presentationlayer.User.UserIDLessUsernameLessDTO;
import com.petclinic.authservice.presentationlayer.User.UserResetPwdRequestModel;
import com.petclinic.authservice.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceUserServiceTests {

    final String
            USER = "user",
            PASS = "pas$word123",
            EMAIL = "email@gmail.com",
            NEWPASSWORD = "change",
            BADPASS = "123",
            BADEMAIL = null;
    final long BADID = -1;

    private final String VALID_TOKEN = "a.fake.token";
    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private ResetPasswordTokenRepository tokenRepository;
    @MockBean
    private UserRepo userRepo;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @MockBean
    private MailService mailService;

    @MockBean
    private JwtTokenUtil jwtService;

    public AuthServiceUserServiceTests() {
    }

    @BeforeEach
    void setup() {
        userRepo.deleteAllInBatch();
        when(jwtService.generateToken(any()))
                .thenReturn(VALID_TOKEN);
        when(mailService.sendMail(any()))
                .thenReturn("Your verification link: someFakeLink");
    }

    @Test
    void loginUser_ShouldReturnHashMap_TokenAndUser() throws IncorrectPasswordException {


        Set<Role> roles = new HashSet<>();

        User admin = User.builder()
                .username("Admin")
                .roles(roles)
                .email("admin@admin.com")
                .password(passwordEncoder.encode("pwd"))
                .verified(true)
                .build();
        userRepo.save(admin);

        when(userRepo.findByEmail(any()))
                .thenReturn(Optional.of(admin));

        when(jwtService.generateToken(any()))
                .thenReturn(VALID_TOKEN);
        UserIDLessUsernameLessDTO user2 = new UserIDLessUsernameLessDTO("admin@admin.com", "pwd");
        assertEquals(VALID_TOKEN,userService.login(user2).get("token").toString().substring(7,19));
        assertNotNull(userService.login(user2).get("user"));
    }

    @Test
    @DisplayName("Send email should succeed")
    void sendEmail_ShouldSucceed() {

        User user = User.builder()
                .username(USER)
                .email(EMAIL)
                .password(passwordEncoder.encode(PASS))
                .verified(true)
                .build();
        userRepo.save(user);

        UserResetPwdRequestModel userResetPwdRequestModel = UserResetPwdRequestModel.builder().email(EMAIL).url("someFakeLink").build();

        when(userRepo.findByEmail(any()))
                .thenReturn(Optional.of(user));

        when(mailService.sendMail(any()))
                .thenReturn("Your verification link: someFakeLink");

        userService.processForgotPassword(userResetPwdRequestModel);


        verify(mailService,times(1)).sendMail(any());

    }
    @Test
    @DisplayName("Verify email from token should throw NotFoundException for non-existing user")
    void verifyEmailFromToken_ShouldThrowNotFoundException() {
        // Arrange
        String token = "invalidToken";

        when(jwtService.getUsernameFromToken(token)).thenReturn("username");

        when(userRepo.findByUsername("username")).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.verifyEmailFromToken(token);
        });

        assertEquals("User not found", exception.getMessage());

        verify(userRepo).findByUsername("username");
    }

    @Test
    void loginWithUnverifiedUser_ShouldThrowException() {
        User user = User.builder()
                .username(USER)
                .email(EMAIL)
                .password(passwordEncoder.encode(PASS))
                .verified(false)
                .build();
        userRepo.save(user);

        UserIDLessUsernameLessDTO user2 = new UserIDLessUsernameLessDTO(EMAIL, PASS);

        when(userRepo.findByEmail(any()))
                .thenReturn(Optional.of(user));

        assertThrows(UnverifiedUserException.class, () -> userService.login(user2));
    }


    @Test
    @DisplayName("Get all users, should succeed")
    void getAllUsers_ShouldSucceed() {

        User user = User.builder()
                .username(USER)
                .userIdentifier(new UserIdentifier())
                .email(EMAIL)
                .password(passwordEncoder.encode(PASS))
                .verified(true)
                .build();
        userRepo.save(user);

        User user2 = User.builder()
                .username("user2")
                .userIdentifier(new UserIdentifier())
                .email("email@email.com")
                .password(passwordEncoder.encode(PASS))
                .verified(true)
                .build();
        userRepo.save(user2);

        when(userRepo.findAll())
                .thenReturn(List.of(user,user2));

        assertEquals(2,userService.findAllWithoutPage().size());
    }

    @Test
    @DisplayName("Get user by user ID should succeed")
    void getUserByUserId_ShouldSucceed() {
        // Arrange
        User user = User.builder()
                .username(USER)
                .userIdentifier(new UserIdentifier())
                .email(EMAIL)
                .password(passwordEncoder.encode(PASS))
                .verified(true)
                .build();
        userRepo.save(user);

        when(userRepo.findOptionalUserByUserIdentifier_UserId(any()))
                .thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserByUserId(user.getUserIdentifier().getUserId());

        // Assert
        assertEquals(user, result);
    }

    @Test
    public void testUpdateResetPasswordToken_Success() {
        // Arrange
        User user = User.builder()
                .username(USER)
                .userIdentifier(new UserIdentifier())
                .email(EMAIL)
                .password(passwordEncoder.encode(PASS))
                .verified(true)
                .build();
        userRepo.save(user);
        String token = "newToken";

        when(userRepo.findByEmail(any()))
                .thenReturn(Optional.of(user));

        when(tokenRepository.findResetPasswordTokenByUserIdentifier(any()))
                .thenReturn(new ResetPasswordToken(1L,token));

        when(tokenRepository.save(any()))
                .thenReturn(new ResetPasswordToken(1L,token));

        // Act
        userService.updateResetPasswordToken(token, EMAIL);

        // Assert
        verify(tokenRepository, times(1)).delete(any()); // Verify that delete was called once
        verify(tokenRepository, times(1)).save(any());   // Verify that save was called once
    }

    @Test
    public void testUpdateResetPasswordToken_UserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        String token = "newToken";

            when(userRepo.findByEmail(email))
                    .thenReturn(Optional.empty());

        // Act
        assertThrows(NotFoundException.class, () -> userService.updateResetPasswordToken(token, email));
        // Assert
        // IllegalArgumentException should be thrown
    }

    @Test
    @DisplayName("updatePassword should throw NotFoundException for non-existing user")
    void updatePassword_ShouldThrowNotFoundExceptionForNonExistingUser() {
        // Arrange
        String newPassword = "newPassword";
        String token = "validToken";

        ResetPasswordToken validToken = new ResetPasswordToken();
        validToken.setUserIdentifier(1L);
        validToken.setExpiryDate(new Date(System.currentTimeMillis() + 1000));

        when(tokenRepository.findResetPasswordTokenByToken(any())).thenReturn(validToken);

        when(userRepo.findById(validToken.getUserIdentifier())).thenReturn(Optional.empty());

        // Act and Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.updatePassword(newPassword, token);
        });

        assertEquals("Could not find any customer with the token " + token, exception.getMessage());

        verify(userRepo).findById(validToken.getUserIdentifier());
    }

    @Test
    @DisplayName("Delete user, should succeed")
    void deleteUser_ShouldSucceed() {
        // Arrange
        User user = User.builder()
                .username(USER)
                .userIdentifier(new UserIdentifier())
                .email(EMAIL)
                .password(passwordEncoder.encode(PASS))
                .verified(true)
                .build();
        userRepo.save(user);

        when(userRepo.findUserByUserIdentifier_UserId(any()))
                .thenReturn(user);

        // Act
        userService.deleteUser(user.getUserIdentifier().getUserId());

        // Assert
        verify(userRepo, times(1)).delete(user);
    }

    @Test
    @DisplayName("Delete user, should throw NotFoundException")
    void deleteUser_ShouldThrowNotFoundException() {
        // Arrange
        String nonExistentUserId = "nonExistentUserId";

        when(userRepo.findUserByUserIdentifier_UserId(nonExistentUserId))
                .thenReturn(null);

        // Act and Assert
        assertThrows(NotFoundException.class, () -> userService.deleteUser(nonExistentUserId));
    }




// Next Story
//    @Test
//    @DisplayName("processForgotPassword should handle InvalidInputException")
//    void processForgotPassword_ShouldHandleInvalidInputException() {
//        // Arrange
//        UserResetPwdRequestModel userResetPwdRequestModel = UserResetPwdRequestModel.builder()
//                .email("test@example.com")
//                .build();
//
//        when(userRepo.findByEmail(any())).thenReturn(Optional.empty());
//
//        when(tokenRepository.save(any())).thenThrow(new RuntimeException("Simulated Exception"));
//
//        // Act and Assert
//        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
//            userService.processForgotPassword(userResetPwdRequestModel);
//        });
//
//        // Verify that the InvalidInputException is thrown with the expected message
//        assertEquals("Simulated Exception", exception.getMessage());
//
//        // Verify that the NotFoundException is caught and rethrown as an InvalidInputException
//        verify(userRepo).findByEmail("test@example.com");
//        verify(tokenRepository).save(any());
//    }


}