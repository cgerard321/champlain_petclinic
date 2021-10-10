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
 */
package com.petclinic.auth.User;

import com.petclinic.auth.Role.Role;
import com.petclinic.auth.Role.RoleController;
import com.petclinic.auth.Role.RoleIDLessDTO;
import com.petclinic.auth.Role.RoleRepo;
import com.petclinic.auth.User.User;
import com.petclinic.auth.User.UserController;
import com.petclinic.auth.User.UserIDLessDTO;
import com.petclinic.auth.User.UserRepo;
import javassist.NotFoundException;
import org.apache.tomcat.util.http.fileupload.MultipartStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.validation.*;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static java.lang.Math.min;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthServiceUserControllerTests {

    private final static Random rng;

    static {
        rng = new Random();
    }

    final String
            USER = "user",
            PASS = "Pas$word123",
            EMAIL = "email@gmail.com";


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
    private UserRepo userRepo;

    @Autowired
    private UserController userController;

    private final UserIDLessDTO ID_LESS_USER = new UserIDLessDTO(USER, PASS, EMAIL);

    @Test
    @DisplayName("Create a user from controller")
    void create_user_from_controller() {
        final User user = userController.createUser(ID_LESS_USER);
        assertNotNull(user);
        assertThat(user.getId(), instanceOf(Long.TYPE));
        assertTrue(userRepo.findById(user.getId()).isPresent());
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

        assertEquals(USER_COUNT, userRepo.count());

        Page<User> userPage = userController.getAllUsers(STARTING_PAGE, PAGE_LIM);
        assertNotNull(userPage);
        assertEquals(USER_COUNT, userPage.getTotalElements());
        assertEquals(USER_COUNT / PAGE_LIM, userPage.getTotalPages());
    }

    @Test
    @DisplayName("Add then delete role from controller")
    void add_then_delete_user_from_controller() {

        final User save = userRepo.save(new User("Username", "password", "email@gmail.com"));
        final Optional<User> found = userRepo.findById(save.getId());
        assertTrue(found.isPresent());
        assertEquals("Username", found.get().getUsername());
        assertEquals("password", found.get().getPassword());
        assertEquals("email@gmail.com", found.get().getEmail());

        // Idempotency check
        for (int i = 0; i < rng.nextInt(100); i++) {
            userController.deleteUser(save.getId());
            assertFalse(userRepo.findById(save.getId()).isPresent());
        }
    }

    @Test
    public void  get_user() throws Exception {

        User entity = new User("Username", "password", "email@gmail.com");
        userRepo.save(entity);

        assertTrue(userRepo.findById(entity.getId()).isPresent());
        User found = userController.getUser(entity.getId());
        assertEquals("Username", found.getUsername());
        assertEquals("password", found.getPassword());
        assertEquals("email@gmail.com", found.getEmail());

        mockMvc.perform(get("/users/" + entity.getId()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    public void  reset_password() throws Exception {

        User entity = new User("Username", "password", "email@gmail.com");
        String newPass = "newPassword";
        User saved = userRepo.save(entity);

        assertTrue(userRepo.findById(saved.getId()).isPresent());
        userController.passwordReset(saved.getId(), newPass);

        User found = userRepo.findById(saved.getId()).get();
        assertEquals(newPass, found.getPassword());

        mockMvc.perform(get("/users/" + entity.getId()))
                .andDo(print())
                .andExpect(status().isForbidden());

    }
}