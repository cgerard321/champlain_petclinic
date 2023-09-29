package com.auth.authservice.businesslayer;

import com.auth.authservice.Util.Exceptions.IncorrectPasswordException;
import com.auth.authservice.Util.Exceptions.InvalidInputException;
import com.auth.authservice.datalayer.roles.Role;
import com.auth.authservice.datalayer.user.*;
import com.auth.authservice.datamapperlayer.UserMapper;
import com.auth.authservice.domainclientlayer.Mail.MailService;
import com.auth.authservice.presentationlayer.User.UserIDLessUsernameLessDTO;
import com.auth.authservice.presentationlayer.User.UserResetPwdRequestModel;
import com.auth.authservice.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        assertThrows(IllegalArgumentException.class, () -> userService.updateResetPasswordToken(token, email));
        // Assert
        // IllegalArgumentException should be thrown
    }




}