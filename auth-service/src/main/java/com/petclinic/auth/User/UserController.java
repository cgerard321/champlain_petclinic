package com.petclinic.auth.User;

/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 24/10/21
 * Ticket: feat(AUTH-CPC-310)
 *
 * User: @Fube
 * Date: 2021-10-10
 * Ticket: feat(AUTH-CPC-357)
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

import com.petclinic.auth.Exceptions.IncorrectPasswordException;
import com.petclinic.auth.JWT.JWTService;
import com.petclinic.auth.User.data.User;
import com.petclinic.auth.User.data.UserIDLessRoleLessDTO;
import com.petclinic.auth.User.data.UserPasswordLessDTO;
import com.petclinic.auth.User.data.UserTokenPair;
import com.petclinic.auth.Exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Base64;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final JWTService jwtService;

    @GetMapping("/{userId}")
    public User getUser(@PathVariable long userId) {
        log.info("Getting user with id: {}", userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    public Page<User> getAllUsers(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {

        log.info("page={}", page);
        final Page<User> all = userService.findAll(PageRequest.of(page - 1, size));
        log.info("Retrieved paginated result with {} entries and {} pages", all.getTotalElements(), all.getTotalPages());
        return all;
    }

    @PostMapping
    public UserPasswordLessDTO createUser(@RequestBody @Valid UserIDLessRoleLessDTO dto) {

        log.info("Trying to persist user");
        final User saved = userService.createUser(dto);
        log.info("Successfully persisted user");

        return userMapper.modelToPasswordLessDTO(saved);
    }

    @PutMapping("/{userId}")
    public void passwordReset(@PathVariable long userId, @RequestBody String newPassword) {

        userService.passwordReset(userId, newPassword);
        log.info("Password for User with id {} with new password {}", userId, newPassword);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
        log.info("Deleted role with id {}", userId);
    }

    @GetMapping("/verification/{base64EncodedToken}")
    public UserPasswordLessDTO verifyEmail(@PathVariable String base64EncodedToken) {
        return userService.verifyEmailFromToken(new String(Base64.getDecoder().decode(base64EncodedToken)));
    }

    @PostMapping("/login")
    public ResponseEntity<UserPasswordLessDTO> login(@RequestBody UserIDLessRoleLessDTO user) throws IncorrectPasswordException {
        final UserTokenPair login = userService.login(user);
        return ok()
                .header(AUTHORIZATION, login.getToken())
                .body(userMapper.modelToPasswordLessDTO(login.getUser()));
    }
    
    public Boolean verifyUser (@RequestBody UserIDLessUsernameLessDTO loginUser) throws NotFoundException {
        final User user = userService.getUserByEmail(loginUser.getEmail());
        return userService.verifyPassword(user, loginUser);
    }
}
