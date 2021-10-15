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
 */

import com.petclinic.auth.Exceptions.IncorrectPasswordException;
import com.petclinic.auth.JWT.JWTService;
import io.jsonwebtoken.lang.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Base64;
import java.util.Map;

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
    private final AuthenticationManager authenticationManager;
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
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );

            User principal = (User) authentication.getPrincipal();

            final String token = jwtService.encrypt(principal);

            return ok()
                    .header(AUTHORIZATION, token)
                    .body(userMapper.modelToPasswordLessDTO(principal));
        } catch (BadCredentialsException ex) {
            throw new IncorrectPasswordException(format("Password not valid for email %s", user.getEmail()));
        }
    }
}
