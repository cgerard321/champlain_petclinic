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
import com.petclinic.auth.Role.data.Role;
import com.petclinic.auth.User.data.User;
import com.petclinic.auth.User.data.UserIDLessRoleLessDTO;
import com.petclinic.auth.User.data.UserPasswordLessDTO;
import com.petclinic.auth.User.data.UserTokenPair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final JWTService jwtService;
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
    public void deleteUser(long userId) {
        log.info("deleteUser: trying to delete entity with userId: {}", userId);
        userRepo.findById(userId).ifPresent(userRepo::delete);
    }

    @Override
    public Mail generateVerificationMail(User user) {
        final String base64Token = Base64.getEncoder()
                .withoutPadding()
                .encodeToString(jwtService.encrypt(user).getBytes(StandardCharsets.UTF_8));

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
        final User decrypt = jwtService.decrypt(token);
        log.info("Decrypted user with email {} from token", decrypt.getEmail());

        final User byEmail = userRepo.findByEmail(decrypt.getEmail()).get();
        byEmail.setVerified(true);
        final User save = userRepo.save(byEmail);
        log.info("Updated user with email {} to verified=true", decrypt.getEmail());

        return userMapper.modelToPasswordLessDTO(save);
    }

    @Override
    public UserTokenPair login(UserIDLessRoleLessDTO user) throws IncorrectPasswordException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );

            final Object rawPrincipal = authentication.getPrincipal();
            User principal;
            if(rawPrincipal instanceof User) {

                principal = (User) authentication.getPrincipal();
            } else {
                final UserDetails userDetails = (UserDetails) rawPrincipal;
                principal = User.builder()
                        .id(-1)
                        .username(userDetails.getUsername())
                        .email(userDetails.getUsername())
                        .verified(true)
                        .password(userDetails.getPassword())
                        .roles(userDetails.getAuthorities().parallelStream()
                                .map(n -> Role.builder()
                                        .id(-1)
                                        .name(n.getAuthority().split("_")[1].toUpperCase())
                                        .build())
                                .collect(Collectors.toSet()))
                        .build();
            }

            return UserTokenPair.builder()
                    .token(jwtService.encrypt(principal))
                    .user(principal)
                    .build();
        } catch (BadCredentialsException ex) {
            throw new IncorrectPasswordException(format("Password not valid for email %s", user.getEmail()));
        }
    }

    @Override
    public User getUserByEmail(String email) throws NotFoundException {

        return userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No account found for email: " + email));
    }
}
