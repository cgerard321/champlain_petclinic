/**
 * Created by IntelliJ IDEA.
 *
 * User: @MaxGrabs
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-13)
 *
 * User: @Trilikin21
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-64)
 *
 * User: @JordanAlbayrak
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-102)
 *
 * User: @Zellyk
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-104)
 *
 * User: @Fube
 * Date: 09/10/21
 * Ticket: feat(AUTH-CPC-310)
 *
 * User: @Fube
 * Date: 2021-10-10
 * Ticket: feat(AUTH-CPC-357)
 */
package com.petclinic.auth.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.auth.Exceptions.IncorrectPasswordException;
import com.petclinic.auth.JWT.JWTService;
import com.petclinic.auth.Mail.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.validation.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceUserControllerTests {

    private final static Random rng;

    @Autowired
    private ObjectMapper objectMapper;

    static {
        rng = new Random();
    }

    final String
            USER = "user",
            PASS = "Pas$word123",
            EMAIL = "email@gmail.com";

    private final String VALID_TOKEN = "a.fake.token";


    private Validator validator;

    @BeforeEach
    void setup() {
        userRepo.deleteAllInBatch();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserController userController;

    @MockBean
    private MailService mailService;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private UserService userService;

    private final UserIDLessDTO ID_LESS_USER = new UserIDLessDTO(USER, PASS, EMAIL);

    @Test
    @DisplayName("Create a user from controller")
    void create_user_from_controller() {

        final User hypothetical = userMapper.idLessDTOToModel(ID_LESS_USER);

        when(jwtService.encrypt(any()))
                .thenReturn("a.fake.token");
        when(userService.createUser(ID_LESS_USER))
                .thenReturn(hypothetical);

        final UserPasswordLessDTO user = userController.createUser(ID_LESS_USER);
        assertNotNull(user);
        assertThat(user.getId(), instanceOf(Long.TYPE));
    }
    @Test
    @DisplayName("Check the required fields with empty data")
    void check_empty_require_fields() {


        UserIDLessDTO userIDLessDTO = new UserIDLessDTO();

        assertThrows(ConstraintViolationException.class, () -> userController.createUser(userIDLessDTO));
    }

    @Test
    @DisplayName("Check the username field in order to refused if it is empty")
    void check_empty_username() {


        UserIDLessDTO userIDLessDTO = new UserIDLessDTO(null, PASS,EMAIL);

        assertThrows(ConstraintViolationException.class, () -> userController.createUser(userIDLessDTO));
    }

    @Test
    @DisplayName("Check the password field in order to refused if it is empty")
    void check_empty_password() {


        UserIDLessDTO userIDLessDTO = new UserIDLessDTO( USER, null,EMAIL);

        assertThrows(ConstraintViolationException.class, () -> userController.createUser(userIDLessDTO));
    }
    @Test
    @DisplayName("Check the password field in order to refused if no special character")
    void check_missing_specialchar_password() {


        UserIDLessDTO userIDLessDTO = new UserIDLessDTO( USER, "Password123",EMAIL);
        Set<ConstraintViolation<UserIDLessDTO>> violations = validator.validate(userIDLessDTO);
        assertFalse(violations.isEmpty());
    }
    @Test
    @DisplayName("Check the password field in order to refused if no number")
    void check_missing_number_password() {


        UserIDLessDTO userIDLessDTO = new UserIDLessDTO( USER, "Pas$word",EMAIL);

        Set<ConstraintViolation<UserIDLessDTO>> violations = validator.validate(userIDLessDTO);
        assertFalse(violations.isEmpty());
    }
    @Test
    @DisplayName("Check the password field in order to refused if no uppercase character")
    void check_missing_uppercase_password() {


        UserIDLessDTO userIDLessDTO = new UserIDLessDTO( USER, "pas$word123",EMAIL);

        Set<ConstraintViolation<UserIDLessDTO>> violations = validator.validate(userIDLessDTO);
        assertFalse(violations.isEmpty());
    }
    @Test
    @DisplayName("Check the password field in order to refused if no lowercase character")
    void check_missing_lowercase_password() {


        UserIDLessDTO userIDLessDTO = new UserIDLessDTO( USER, "PAS$WORD123",EMAIL);

        Set<ConstraintViolation<UserIDLessDTO>> violations = validator.validate(userIDLessDTO);
        assertFalse(violations.isEmpty());
    }
    @Test
    @DisplayName("Check the email field in order to refused if it is empty")
    void check_empty_email(){

        UserIDLessDTO userIDLessDTO = new UserIDLessDTO(USER, PASS,null);

        assertThrows(ConstraintViolationException.class, () -> userController.createUser(userIDLessDTO));
    }
    @Test
    @DisplayName("Check if the input ID is correct")
    void check_empty_id() throws Exception{


        mockMvc.perform(put("/users/1000"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Get all users from controller")
    void get_all_users_from_controller() {

        final int
                USER_COUNT = 20,
                PAGE_LIM = 10,
                STARTING_PAGE = 1;

        for (int i = 0; i < USER_COUNT; i++) {
            userRepo.save(new User("Username-1", "password"+i, "email@gmail.com"+i));
        }

        final Page<User> all = userRepo.findAll(PageRequest.of(STARTING_PAGE - 1, PAGE_LIM));
        when(userService.findAll(PageRequest.of(STARTING_PAGE - 1, PAGE_LIM)))
                .thenReturn(all);

        assertEquals(USER_COUNT, userRepo.count());

        Page<User> userPage = userController.getAllUsers(STARTING_PAGE, PAGE_LIM);
        assertNotNull(userPage);
        assertEquals(USER_COUNT, userPage.getTotalElements());
        assertEquals(USER_COUNT / PAGE_LIM, userPage.getTotalPages());
    }

    @Test
    @DisplayName("Add then delete role from controller")
    void add_then_delete_user_from_controller() {
        // Idempotency check
        for (int i = 0; i < rng.nextInt(100); i++) {
            userController.deleteUser(1);
        }
    }

    @Test
    public void  get_user() throws Exception {

        final long ID = 123;

        when(userService.getUserById(ID))
                .thenReturn(new User());

        mockMvc.perform(get("/users/" + ID))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void  reset_password() throws Exception {

        User entity = new User("Username", "password", "email@gmail.com");
        String newPass = "newPassword";
        User saved = userRepo.save(entity);

        when(userService.passwordReset(saved.getId(), newPass))
                .thenReturn(saved.toBuilder().password(newPass).build());
        when(userService.getUserById(saved.getId()))
                .thenReturn(saved.toBuilder().password(newPass).build());

        assertTrue(userRepo.findById(saved.getId()).isPresent());
        userController.passwordReset(saved.getId(), newPass);

        User found = userController.getUser(saved.getId());
        assertEquals(newPass, found.getPassword());

        mockMvc.perform(get("/users/" + entity.getId()))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("When POST on users endpoint with valid data, allow any")
    void allow_any_on_users() throws Exception {

        final UserIDLessDTO userIDLessDTO = new UserIDLessDTO(USER, PASS, EMAIL);
        final String asString = objectMapper.writeValueAsString(userIDLessDTO);

        when(jwtService.encrypt(any()))
                .thenReturn("a.fake.token");

        when(userService.createUser(
                argThat( n -> n.getUsername().equals(USER) && n.getPassword().equals(PASS) && n.getEmail().equals(EMAIL) )) )
                .thenReturn(User.builder()
                    .username(USER)
                    .email(EMAIL).build()
                );

        mockMvc.perform(post("/users").contentType(APPLICATION_JSON).content(asString))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("When GET on verification endpoint, allow any")
    void allow_any_on_verification() throws Exception {

        final User user = User.builder()
                        .username("test")
                        .email("fake@email.com")
                        .password("fakePassword")
                        .id(12345)
                        .build();

        final String fakeToken = "a.fake.token";
        final String base64Token =
                Base64.getEncoder().withoutPadding().encodeToString(fakeToken.getBytes(StandardCharsets.UTF_8));

        when(userService.verifyEmailFromToken(fakeToken))
                .thenReturn(UserPasswordLessDTO.builder()
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .roles(Collections.EMPTY_SET)
                        .id(user.getId())
                        .build());


        mockMvc.perform(get("/users/verification/" + base64Token))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    @DisplayName("When POST on login, allow any")
    void allow_any_on_login() throws Exception {
        mockMvc.perform(post("/users/login"))
                .andExpect(status().is(400)); // Bad request means that it passed spring security & it found the controller action
    }

    @Test
    @DisplayName("When login successful, get JWT")
    void login_get_jwt() throws Exception {

        final UserIDLessDTO build = UserIDLessDTO.builder().email(EMAIL).password(PASS).build();
        final String asString = objectMapper.writeValueAsString(build);

        when(userService.login(
                argThat( n -> n.getEmail().equals(EMAIL) && n.getPassword().equals(PASS) )))
                .thenReturn(VALID_TOKEN);

        mockMvc.perform(post("/users/login").contentType(APPLICATION_JSON).content(asString))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.token").value(VALID_TOKEN));
    }

    @Test
    @DisplayName("When bad password, throw IncorrectPasswordException")
    void bad_login_throw_incorrect_password_exception() throws Exception {

        final String EXCEPTION_MESSAGE = format("Password not valid for email %s", EMAIL);

        final UserIDLessDTO build = UserIDLessDTO.builder().email(EMAIL).password(PASS).build();
        final String asString = objectMapper.writeValueAsString(build);

        when(userService.login(
                argThat( n -> n.getEmail().equals(EMAIL) && !n.getPassword().equals(PASS) )))
                .thenThrow(new IncorrectPasswordException(EXCEPTION_MESSAGE));

        mockMvc.perform(post("/users/login").contentType(APPLICATION_JSON).content(asString))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value(EXCEPTION_MESSAGE));
    }
}