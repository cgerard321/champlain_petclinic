package com.petclinic.authservice.presentationlayer.User;

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

import com.petclinic.authservice.Util.Exceptions.IncorrectPasswordException;
import com.petclinic.authservice.businesslayer.UserService;
import com.petclinic.authservice.datalayer.user.User;
import com.petclinic.authservice.datamapperlayer.UserMapper;

import com.petclinic.authservice.security.JwtTokenUtil;
import com.petclinic.authservice.security.SecurityConst;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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

    @GetMapping("/all")
    public ResponseEntity<List<UserDetails>> getAllUsers() {
        List<UserDetails> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }


    @GetMapping("/withoutPages")
    public List<UserDetails> getUserWithoutPage() {
        return userService.findAllWithoutPage();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetails> getUserByUserId(@PathVariable String userId) {
        User user = userService.getUserByUserId(userId);
        return ResponseEntity.ok(userMapper.modelToDetails(user));
    }

    //add pagination to this method later
    @GetMapping("/")
    public ResponseEntity<List<UserDetails>> getAllUsers(@RequestParam Optional<String> username) {
        List<UserDetails> users;

        if (username.isPresent()) {
            users = userService.getUsersByUsernameContaining(username.get());
        } else {
            users = userService.findAllWithoutPage();
        }

        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserPasswordLessDTO> createUser(@RequestBody @Valid UserIDLessRoleLessDTO dto) {
        final User saved = userService.createUser(dto);
        return ResponseEntity.ok()
                .body(userMapper.modelToPasswordLessDTO(saved));
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserPasswordLessDTO> updateUserRole(@PathVariable String userId, @RequestBody RolesChangeRequestDTO roleChanged, @CookieValue("Bearer") String token) {
        return ResponseEntity.ok().body(userService.updateUserRole(userId, roleChanged, token));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable String userId, @RequestBody UserPasswordLessDTO userPasswordLessDTO) {
        return ResponseEntity.ok().body(userService.updateUser(userId, userPasswordLessDTO));
    }

    @GetMapping("/verification/{base64EncodedToken}")
    public ResponseEntity<UserPasswordLessDTO> verifyEmail(@PathVariable String base64EncodedToken) {

        try {
            // Validate the base64EncodedToken (optional)
            if (!isValidBase64(base64EncodedToken)) {
                return ResponseEntity.badRequest().body(null);
            }

            String decodedToken = new String(Base64.getDecoder().decode(base64EncodedToken));
            UserPasswordLessDTO result = userService.verifyEmailFromToken(decodedToken);

            // Handle cases where verification fails
            if (result == null) {
                return ResponseEntity.badRequest().body(null);
            }

            return ResponseEntity.ok().body(result);
        } catch (IllegalArgumentException | NullPointerException e) {
            // Handle decoding exceptions
            return ResponseEntity.badRequest().body(null);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<UserPasswordLessDTO> login(@RequestBody UserIDLessUsernameLessDTO login,
                                                     HttpServletResponse response) throws IncorrectPasswordException {
        try {

            HashMap<String, Object> userAndToken = userService.login(login);
            ResponseCookie token = (ResponseCookie) userAndToken.get("token");
            User loggedInUser = (User) userAndToken.get("user");
            response.setHeader(HttpHeaders.SET_COOKIE, token.toString());
            UserPasswordLessDTO testUser = userMapper.modelToIDLessPasswordLessDTO(loggedInUser);
            return ResponseEntity.ok()
                    .body(testUser);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<TokenResponseDTO> validateToken(@CookieValue("Bearer") String token) {

        TokenResponseDTO tokenResponseDTO = TokenResponseDTO.builder()
                .token(token)
                .userId(jwtService.getIdFromToken(token))
                .email(jwtService.getUsernameFromToken(token))
                .roles(jwtService.getRolesFromToken(token))
                .build();

        return ResponseEntity.ok(tokenResponseDTO);

    }

    @PatchMapping("/{userId}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable String userId) {
        userService.disableUser(userId);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{userId}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable String userId) {
        userService.enableUser(userId);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/forgot_password")
    public ResponseEntity<Void> processForgotPassword(@RequestBody UserResetPwdRequestModel userResetPwdRequestModel) {
        userService.processForgotPassword(userResetPwdRequestModel);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/reset_password")
    public ResponseEntity<Void> processResetPassword(@RequestBody @Valid UserResetPwdWithTokenRequestModel resetRequest) {
        userService.processResetPassword(resetRequest);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {

        userService.deleteUser(userId);

        return ResponseEntity.noContent().build();
    }


    private boolean isValidBase64(String s) {
        try {
            Base64.getDecoder().decode(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @PatchMapping("/{userId}/username")
    public ResponseEntity<String> updateUserUsername (@PathVariable String userId, @RequestBody String username, @CookieValue("Bearer") String token) {
        return ResponseEntity.ok().body(userService.updateUserUsername(userId, username, token));
    }

}
