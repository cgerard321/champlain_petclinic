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

        mailService.sendMail(generateVerificationMail(user));

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
        return Mail.builder()
                .message(format("Your verification link: %s/verification/%s", gatewayOrigin, base64Token))
                .subject("PetClinic e-mail verification")
                .to(user.getEmail())
                .build();
    }
}
