/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.auth.User;

import com.petclinic.auth.Exceptions.EmailAlreadyExistsException;
import com.petclinic.auth.Exceptions.IncorrectPasswordException;
import com.petclinic.auth.Exceptions.InvalidInputException;
import com.petclinic.auth.Exceptions.NotFoundException;
import com.petclinic.auth.JWT.JWTService;
import com.petclinic.auth.Mail.Mail;
import com.petclinic.auth.Mail.MailService;
import com.petclinic.auth.User.data.User;
import com.petclinic.auth.User.data.UserIDLessRoleLessDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;


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

    private String VALID_TOKEN = "a.fake.token";


    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @MockBean
    private MailService mailService;

    @MockBean
    private JWTService jwtService;

    @BeforeEach
    void setup() {
        userRepo.deleteAllInBatch();
        when(jwtService.encrypt(any()))
                .thenReturn(VALID_TOKEN);
        when(mailService.sendMail(any()))
                .thenReturn("Your verification link: someFakeLink");
    }

    @Test
    @DisplayName("Create new user")
    void create_new_user() {
        UserIDLessRoleLessDTO userIDLessDTO = new UserIDLessRoleLessDTO(USER, PASS, EMAIL);

        when(jwtService.encrypt(argThat( n -> n.getEmail().equals(EMAIL) )))
                .thenReturn(VALID_TOKEN);

        final User createdUser = userService.createUser(userIDLessDTO);
        assertEquals(createdUser.getUsername(), userIDLessDTO.getUsername());
        assertNotEquals(createdUser.getPassword(), userIDLessDTO.getPassword());
        assertEquals(createdUser.getEmail(), userIDLessDTO.getEmail());
        assertFalse(createdUser.isVerified());
    }

    @Test
    @DisplayName("Reset user password")
    void test_user_password_reset() {

        final User u = new User(USER, PASS, EMAIL);
        userRepo.save(u);

        User user = userService.passwordReset(u.getId(), NEWPASSWORD);

        Optional<User> find = userRepo.findById(user.getId());
        assertTrue(find.isPresent());
        assertEquals(NEWPASSWORD, user.getPassword());
        assertEquals(NEWPASSWORD, find.get().getPassword());
    }

    @Test
    @DisplayName("Reset password, passed wrong ID")
    void test_user_password_reset_ID() {

        final User u = new User(USER, PASS, EMAIL);
        userRepo.save(u);

        assertThrows(NotFoundException.class, () -> userService.passwordReset(10000, ""));
    }

    @Test
    @DisplayName("Create new user with email already in use")
    void create_new_user_with_same_email() {
        UserIDLessRoleLessDTO userIDLessDTO = new UserIDLessRoleLessDTO(USER, PASS, EMAIL);
        User userMap = userMapper.idLessRoleLessDTOToModel(userIDLessDTO);
        userRepo.save(userMap);


        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(userIDLessDTO));
    }

    @Test
    @DisplayName("get user by id and succeed")
    void get_user_by_id() throws NotFoundException {
        User user = new User(USER, PASS, EMAIL);
        User saved = userRepo.save(user);
        User found = userService.getUserById(saved.getId());
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    @DisplayName("get user by id and fail")
    void get_user_by_id_and_fail() {
        long id = 1;
        assertFalse(userRepo.findById(id).isPresent());
        assertThrows(NotFoundException.class, () -> userService.getUserById(id));
    }

    @Test
    @DisplayName("Get all users with pages")
    void get_all_users_with_pages() {

        final int USER_COUNT = 10;

        for (int i = 0; i < USER_COUNT; i++) {
            userRepo.save(new User(USER, PASS, EMAIL + i));
        }

        assertEquals(USER_COUNT, userRepo.count());
        assertEquals(USER_COUNT, userService.findAll(PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    @DisplayName("Get all users")
    void get_all_users() {

        final int USER_COUNT = 10;

        for (int i = 0; i < USER_COUNT; i++) {
            userRepo.save(new User(USER, PASS, EMAIL + i));
        }

        assertEquals(USER_COUNT, userRepo.count());
        assertEquals(USER_COUNT, userService.findAllWithoutPage().size());
    }

    @Test
    @DisplayName("Delete user by id")
    void delete_role_by_id() {

        final UserIDLessRoleLessDTO userIDLessDTO = new UserIDLessRoleLessDTO(USER, PASS, EMAIL);


        final User saved = userService.createUser(userIDLessDTO);

        userService.deleteUser(saved.getId());
    }

    @Test
    @DisplayName("Delete user by id and fail")
    void delete_role_by_id_and_fail() {
        assertEquals(Optional.empty(), userRepo.findById(1l));
    }

    @Test
    @DisplayName("When creating user, encrypt password")
    void encrypt_password_before_persistence() {

        final UserIDLessRoleLessDTO userIDLessDTO = new UserIDLessRoleLessDTO(USER, PASS, EMAIL);


        final User saved = userService.createUser(userIDLessDTO);

        assertNotNull(saved.getPassword());
        assertFalse(saved.getPassword().isEmpty());
        assertNotEquals(saved.getPassword(), userIDLessDTO.getPassword());
    }

    @Test
    @DisplayName("When creating user, send verification email")
    void send_email_on_register() {

        AtomicInteger callCount = new AtomicInteger();
        AtomicReference<Mail> mailRef = new AtomicReference<>();

        when(mailService.sendMail(any())).then(args -> {
            callCount.incrementAndGet();
            final Mail mail = args.getArgument(0, Mail.class);
            mailRef.set(mail);
            return "Email sent to " + mail.getMessage();
        });

        final UserIDLessRoleLessDTO userIDLessDTO = new UserIDLessRoleLessDTO(USER, PASS, EMAIL);


        final User saved = userService.createUser(userIDLessDTO);

        assertEquals(1, callCount.get());
        assertTrue(mailRef.get().getMessage().contains("Your verification link: "));
    }

    @Test
    @DisplayName("Given user, generate verification email")
    void generate_verification_email() {

        final UserIDLessRoleLessDTO userIDLessDTO = new UserIDLessRoleLessDTO(USER, PASS, EMAIL);


        final User saved = userService.createUser(userIDLessDTO);

        when(mailService.sendMail(any()))
                .thenReturn("Your verification link: someFakeLink");

        final Mail mail = userService.generateVerificationMail(saved);

        assertTrue(mail.getMessage().contains("Your verification link: "));
    }

    @Test
    @DisplayName("Given user exists in database and email + password match & e-mail is verified, return JWT")
    void successful_login() throws IncorrectPasswordException, SQLIntegrityConstraintViolationException {


        final UserIDLessRoleLessDTO userIDLessDTO = new UserIDLessRoleLessDTO(USER, PASS, EMAIL);
        userService.createUser(userIDLessDTO);

        final User byEmail = userRepo.findByEmail(EMAIL).get();
        byEmail.setVerified(true);
        userRepo.save(byEmail);

        assertEquals(VALID_TOKEN, userService.login(UserIDLessRoleLessDTO.builder()
                .username(USER)
                .password(PASS)
                .email(EMAIL)
                .build())
                .getToken()
        );
    }

    @Test
    @DisplayName("Given user exists in database but password is incorrect, throw IncorrectPasswordException")
    void bad_password_exception() {


        final UserIDLessRoleLessDTO userIDLessDTO = new UserIDLessRoleLessDTO(USER, PASS, EMAIL);
        userService.createUser(userIDLessDTO);

        final User user = userRepo.findByEmail(EMAIL).get();
        user.setVerified(true);
        userRepo.save(user);

        IncorrectPasswordException incorrectPasswordException = assertThrows(IncorrectPasswordException.class, () -> userService.login(UserIDLessRoleLessDTO.builder()
                .username(USER)
                .password(PASS + "bad")
                .email(EMAIL)
                .build())
        );

        assertEquals(format("Password not valid for email %s", EMAIL), incorrectPasswordException.getMessage());
    }

    @Test
    @DisplayName("Verify email that does not exist")
    void verify_email_failure() {
        assertThrows(NotFoundException.class, () -> userService.getUserByEmail(BADEMAIL));
    }

    @Test
    @DisplayName("if id is not unprocessable. then throw InvalidInputExeption")
    void when_id_is_not_unprocessable_then_throw_InvalidInputExeption(){
        assertThrows(InvalidInputException.class, () -> userService.getUserById(BADID));
    }
}
