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
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;

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

    @GetMapping("/withoutPages")
    public List<UserDetails> getUserWithoutPage() {
        return userService.findAllWithoutPage();
    }

    @PostMapping
    public ResponseEntity<UserPasswordLessDTO> createUser(@RequestBody @Valid UserIDLessRoleLessDTO dto){

        log.info("Trying to persist user");
        final User saved = userService.createUser(dto);
        log.info("Successfully persisted user");

        return ResponseEntity.ok()
                .body(userMapper.modelToPasswordLessDTO(saved));
    }


//    @DeleteMapping("/{userId}")
//    public void deleteUser(@PathVariable long userId) {
//        userService.deleteUser(userId);
//        log.info("Deleted role with id {}", userId);
//    }

    @GetMapping("/verification/{base64EncodedToken}")
    public ResponseEntity <UserPasswordLessDTO> verifyEmail(@PathVariable String base64EncodedToken) {

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
    public ResponseEntity<String> validateToken() {
            log.info("Token is valid");
            return ResponseEntity.ok().build();
     
    }




    @PostMapping("/forgot_password")
    public ResponseEntity<Void> processForgotPassword(@RequestBody UserResetPwdRequestModel userResetPwdRequestModel) {
        userService.processForgotPassword(userResetPwdRequestModel);
        return ResponseEntity.ok().build();
    }



    @PostMapping("/reset_password")
    public ResponseEntity<Void> processResetPassword(@RequestBody UserResetPwdWithTokenRequestModel resetRequest, Model model) {
        userService.processResetPassword(resetRequest);

        return ResponseEntity.ok().build();
    }

    private boolean isValidBase64(String s) {
        try {
            Base64.getDecoder().decode(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
