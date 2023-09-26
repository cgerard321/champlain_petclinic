package com.auth.authservice.businesslayer;

import com.auth.authservice.Util.Exceptions.IncorrectPasswordException;
import com.auth.authservice.Util.Exceptions.InvalidInputException;
import com.auth.authservice.datalayer.roles.Role;
import com.auth.authservice.datalayer.user.User;
import com.auth.authservice.datalayer.user.UserRepo;
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


    @Autowired
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

        when(mailService.sendMail(any()))
                .thenReturn("Your verification link: someFakeLink");

        userService.processForgotPassword(userResetPwdRequestModel);


        verify(mailService,times(1)).sendMail(any());

    }


    @Test
    @DisplayName("if id is not unprocessable. then throw InvalidInputExeption")
    void when_id_is_not_unprocessable_then_throw_InvalidInputException(){
        assertThrows(InvalidInputException.class, () -> userService.getUserById(BADID));
    }



}