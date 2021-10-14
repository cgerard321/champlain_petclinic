package com.petclinic.auth.User;
import com.petclinic.auth.JWT.JWTService;
import com.petclinic.auth.Mail.Mail;
import com.petclinic.auth.Mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.petclinic.auth.Exceptions.NotFoundException;

import javax.validation.Valid;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

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

    @Value("${gateway.origin}")
    private String gatewayOrigin;
    @Value("${gateway.subdomain}")
    private String gatewaySubdomain;
    @Value("${gateway.protocol}")
    private String gatewayProtocol;

    @Override
    public User getUserById(long id) {
        User entity  = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("No user found for userID" + id));
        log.info("User getUserById: found userId: {}", entity.getId());
        return entity;
    }

    @Override
    public Page<User> findAll(PageRequest of) {
        return userRepo.findAll(of);
    }

    @Override
    public User createUser(@Valid UserIDLessDTO userIDLessDTO) {

        log.info("Saving user with email {}", userIDLessDTO.getEmail());
        User user = userMapper.idLessDTOToModel(userIDLessDTO);
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

        final User byEmail = userRepo.findByEmail(decrypt.getEmail());
        byEmail.setVerified(true);
        final User save = userRepo.save(byEmail);
        log.info("Updated user with email {} to verified=true", decrypt.getEmail());

        return userMapper.modelToIDLessPasswordLessDTO(save);
    }

    @Override
    public String login(UserIDLessDTO user) {
        final User byEmail = userRepo.findByEmail(user.getEmail());

        if(!isSamePassword(user.getPassword(), byEmail.getPassword())) {
            throw new IncorrectPasswordException(format("Password not valid for email %s", user.getEmail()));
        }

        return jwtService.encrypt(byEmail);
    }

    private boolean isSamePassword(String raw, String encoded) {
        return passwordEncoder.encode(raw).equals(encoded);
    }
}
