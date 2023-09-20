/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.auth.authservice.businesslayer;

import com.auth.authservice.Util.Exceptions.EmailAlreadyExistsException;
import com.auth.authservice.Util.Exceptions.IncorrectPasswordException;
import com.auth.authservice.Util.Exceptions.InvalidInputException;
import com.auth.authservice.Util.Exceptions.NotFoundException;
import com.auth.authservice.datalayer.roles.Role;
import com.auth.authservice.datalayer.user.User;
import com.auth.authservice.datalayer.user.UserRepo;
import com.auth.authservice.datamapperlayer.UserMapper;
import com.auth.authservice.domainclientlayer.Mail.Mail;
import com.auth.authservice.domainclientlayer.Mail.MailService;
import com.auth.authservice.presentationlayer.User.UserIDLessRoleLessDTO;
import com.auth.authservice.presentationlayer.User.UserPasswordLessDTO;
import com.auth.authservice.presentationlayer.User.UserTokenPair;
import com.auth.authservice.security.JwtTokenUtil;
import com.auth.authservice.security.SecurityConst;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.UserPrincipal;
import java.time.Duration;
import java.util.*;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SecurityConst securityConst;
    private final UserRepo userRepo;
    //private final RoleRepo roleRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final JwtTokenUtil jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${gateway.origin}")
    private String gatewayOrigin;
    @Value("${gateway.subdomain}")
    private String gatewaySubdomain;
    @Value("${gateway.protocol}")
    private String gatewayProtocol;

    @Override
    public User getUserById(long id) {
        if (id <= 0){
            throw new InvalidInputException("Id cannot be a negative number for " + id);
        }
        else {
            User entity  = userRepo.findById(id)
                    .orElseThrow(() -> new NotFoundException("No user found for userID " + id));
            log.info("User getUserById: found userId: {}", entity.getId());
            return entity;
        }
    }

    @Override
    public Page<User> findAll(PageRequest of) {
        return userRepo.findAll(of);
    }

    @Override
    public List<User> findAllWithoutPage() {
        return userRepo.findAll();
    }

    @Override
    public User createUser(@Valid UserIDLessRoleLessDTO userIDLessDTO) {

        final Optional<User> byEmail = userRepo.findByEmail(userIDLessDTO.getEmail());

        if(byEmail.isPresent()) {
            throw new EmailAlreadyExistsException(
                    format("User with e-mail %s already exists", userIDLessDTO.getEmail()));
        }

        log.info("Saving user with email {}", userIDLessDTO.getEmail());
        User user = userMapper.idLessRoleLessDTOToModel(userIDLessDTO);

        //Optional<Role> role = roleRepo.findById(1L);
        Set<Role> roleSet = new HashSet<>();
        //role.ifPresent(roleSet::add);
        user.setRoles(roleSet);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        log.info("Sending email to {}...", userIDLessDTO.getEmail());
        log.info(mailService.sendMail(generateVerificationMail(user)));
        log.info("Email sent to {}", userIDLessDTO.getEmail());

        return userRepo.save(user);
    }

    @Override
    public User passwordReset(long userId, @Valid String newPassword) {

        log.info("id={}", userId);
        User user = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("No user for id:" + userId));
        user.setPassword(newPassword);
        return userRepo.save(user);

    }

    @Override
    public void deleteUser
            (long userId) {
        log.info("deleteUser: trying to delete entity with userId: {}", userId);
        userRepo.findById(userId).ifPresent(userRepo::delete);
    }

    @Override
    public Mail generateVerificationMail(User user) {
        final String base64Token = Base64.getEncoder()
                .withoutPadding()
                .encodeToString(jwtService.generateToken(user).getBytes(StandardCharsets.UTF_8));

        // Remove dangling . in case of empty sub
        String niceSub = gatewaySubdomain.length() > 0 ? gatewaySubdomain + "." : "";

        return Mail.builder()
                .message(format("Your verification link: %s://%s%s/verification/%s",
                        gatewayProtocol, niceSub, gatewayOrigin, base64Token))
                .subject("PetClinic e-mail verification")
                .to(user.getEmail())
                .build();
    }

    @Override
    public UserPasswordLessDTO verifyEmailFromToken(String token) {
        final Optional<User> decryptUser = userRepo.findByEmail(jwtService.getUsernameFromToken(token));
        log.info("Decrypted user with email {} from token", decryptUser.get().getEmail());

        decryptUser.get().setVerified(true);
        final User save = userRepo.save(decryptUser.get());
        log.info("Updated user with email {} to verified=true", save.getEmail());

        return userMapper.modelToPasswordLessDTO(save);
    }


    @Override
    public HashMap<String,Object> login(UserIDLessRoleLessDTO login) throws IncorrectPasswordException {


        try {
            authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    login.getEmail(), login.getPassword()
                            )
                    );
            log.info("User authenticated");

            log.info("User principal retrieved");

            User loggedInUser = getUserByEmail(login.getEmail());
            log.info("User retrieved from db");

            ResponseCookie token = ResponseCookie.from(securityConst.getTOKEN_PREFIX(), jwtService.generateToken(loggedInUser))
                    .httpOnly(true)
                    .secure(true)
                    .path("/api/gateway")
                    .maxAge(Duration.ofHours(1))
                    .sameSite("Lax").build();


            log.info("In controller before set header");

            return new HashMap<>() {{
                put("token", token);
                put("user", loggedInUser);
            }};
        }
        catch (BadCredentialsException e){
            throw new IncorrectPasswordException("Incorrect password for user with email: " + login.getEmail());
        }
    }

    @Override
    public User getUserByEmail(String email) throws NotFoundException {

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No account found for email: " + email));
    }
}
