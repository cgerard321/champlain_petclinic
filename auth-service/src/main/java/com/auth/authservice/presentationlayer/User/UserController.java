package com.auth.authservice.presentationlayer.User;

/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @Fube
 * Date: 24/10/21
 * Ticket: feat(AUTH-CPC-310)
 * <p>
 * User: @Fube
 * Date: 2021-10-10
 * Ticket: feat(AUTH-CPC-357)
 * <p>
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

import com.auth.authservice.Util.Exceptions.IncorrectPasswordException;
import com.auth.authservice.businesslayer.UserService;
import com.auth.authservice.datalayer.user.User;
import com.auth.authservice.datamapperlayer.UserMapper;

import com.auth.authservice.security.JwtTokenUtil;
import com.auth.authservice.security.SecurityConst;
import com.auth.authservice.security.UserPrincipalImpl;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
@Validated
public class UserController {

    private final SecurityConst securityConst;

    private final AuthenticationManager authenticationManager;

    private final UserService userService;
    private final UserMapper userMapper;

    private final JwtTokenUtil jwtService;

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

    @GetMapping("/withoutPages")
    public List<User> getUserWithoutPage() {

        return userService.findAllWithoutPage();
    }

    @PostMapping
    public UserPasswordLessDTO createUser(
            @RequestBody @Valid UserIDLessRoleLessDTO dto,
            BindingResult bindingResult) {

        log.info("Trying to persist user");
        final User saved = userService.createUser(dto);
        log.info("Successfully persisted user");

        return userMapper.modelToPasswordLessDTO(saved);
    }

    @PutMapping("/passwordReset/{userId}")
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
    public ResponseEntity<UserPasswordLessDTO> login(@RequestBody UserIDLessUsernameLessDTO login, HttpServletResponse response) throws IncorrectPasswordException {
        log.info("In controller");

        try {

            HashMap<String, Object> userAndToken = userService.login(login);
            ResponseCookie token = (ResponseCookie) userAndToken.get("token");
            User loggedInUser = (User) userAndToken.get("user");
            response.setHeader(HttpHeaders.SET_COOKIE, token.toString());
            log.info("Token : {}", token.getValue());

            log.info("In controller after set header");
            UserPasswordLessDTO testUser = userMapper.modelToIDLessPasswordLessDTO(loggedInUser);
            return ResponseEntity.ok()
                    .body(testUser);
        } catch (BadCredentialsException ex) {
            log.info("Bad credentials exception");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<String> validateToken(@RequestBody String token) {
        log.info("Validating token");
        if (jwtService.validateToken(token)) {
            log.info("Token is valid");
            return ResponseEntity.ok().build();
        } else {
            log.info("Token is invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @RequestMapping(method = RequestMethod.HEAD)
    public void preflight() {
        log.info("Preflight request received and accepted");
    }


}
