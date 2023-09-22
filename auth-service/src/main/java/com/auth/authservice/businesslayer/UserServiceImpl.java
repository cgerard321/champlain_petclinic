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
import com.auth.authservice.datalayer.user.ResetPasswordToken;
import com.auth.authservice.datalayer.user.ResetPasswordTokenRepository;
import com.auth.authservice.datalayer.user.User;
import com.auth.authservice.datalayer.user.UserRepo;
import com.auth.authservice.datamapperlayer.UserMapper;
import com.auth.authservice.domainclientlayer.Mail.Mail;
import com.auth.authservice.domainclientlayer.Mail.MailService;
import com.auth.authservice.presentationlayer.User.*;
import com.auth.authservice.security.JwtTokenUtil;
import com.auth.authservice.security.SecurityConst;
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
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static java.lang.String.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SecurityConst securityConst;
    private final ResetPasswordTokenRepository tokenRepository;
    private final UserRepo userRepo;
    //private final RoleRepo roleRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final JwtTokenUtil jwtService;
    private final AuthenticationManager authenticationManager;
    private final String salt = BCrypt.gensalt(10);


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
    public HashMap<String,Object> login(UserIDLessUsernameLessDTO login) throws IncorrectPasswordException {


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
    public Model processForgotPassword(UserResetPwdRequestModel userResetPwdRequestModel, Model model) {
        String email = userResetPwdRequestModel.getEmail();
        String token = UUID.randomUUID().toString();
        log.info("Generated token: " + token);
        try {
            log.info("Line 214");
            getUserByEmail(email);
        }
        catch(RuntimeException e){
            model.addAttribute("message", "This Email is not registered to any account !");
            return model;
        }

        try {

            updateResetPasswordToken( token, email);
            log.info("Line 155");


            String resetPasswordLink =  userResetPwdRequestModel.getUrl()+ "/api/v1/users/reset_password?token=" + token;
            sendEmail(email, resetPasswordLink);
            model.addAttribute("message", "We have sent a reset password link to your email. Please check.");
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return model;
    }

    @Override
    public void updateResetPasswordToken(String token, String email) {
        Optional<User> user = userRepo.findByEmail(email);
        if (user.isPresent()) {
            if(tokenRepository.findResetPasswordTokenByUserIdentifier(user.get().getId()) != null){
                tokenRepository.delete(tokenRepository.findResetPasswordTokenByUserIdentifier(user.get().getId()));
            }
            //Hash the tokens
            ResetPasswordToken resetPasswordToken = new ResetPasswordToken(user.get().getId(), BCrypt.hashpw(token,salt));
            tokenRepository.save(resetPasswordToken);
        } else {
            throw new IllegalArgumentException("Could not find any customer with the email " + email);
        }
    }

    @Override
    public UserPasswordLessDTO getByResetPasswordToken(String token) {
        log.info("Token: " + token);
        log.info("Line 187");
        String hashedToken = BCrypt.hashpw(token, salt);
        log.info("Hashed token: " + hashedToken);
        ResetPasswordToken resetPasswordToken = tokenRepository.findResetPasswordTokenByToken(hashedToken);
        final Calendar cal = Calendar.getInstance();
        Optional<User> user =userRepo.findById(resetPasswordToken.getUserIdentifier());

        if(resetPasswordToken.getExpiryDate().after(cal.getTime()) && user.isPresent())
            return userMapper.modelToPasswordLessDTO(user.get());
        else
            throw new IllegalArgumentException("Token is expired (in getByResetPasswordToken()");    }


    @Override
    public void updatePassword(String newPassword, String token) {

        final Calendar cal = Calendar.getInstance();
        log.info("line 201");
        ResetPasswordToken resetPasswordToken = tokenRepository.findResetPasswordTokenByToken(BCrypt.hashpw(token, salt));
        if(resetPasswordToken.getExpiryDate().before(cal.getTime())){
            throw new IllegalArgumentException("Token expired");
        }

        String encodedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
        Optional<User> user = userRepo.findById(resetPasswordToken.getUserIdentifier());


        if(user.isPresent()) {

            user.get().setPassword(encodedPassword);

            userRepo.save(user.get());
            tokenRepository.delete(resetPasswordToken);
        }else
            throw new NotFoundException("Could not find any customer with the token " + token);


    }

    @Override
    public Model showResetPasswordForm(Map<String, String> querryParams, Model model) {
        String token = querryParams.get("token");


        UserPasswordLessDTO userResponseModel = getByResetPasswordToken(token);
        model.addAttribute("token", token);

        if (userResponseModel == null) {
            model.addAttribute("message", "Invalid Token");
        }
        else{
            model.addAttribute("message", "You have successfully changed your password.");
        }
        return model;
    }

    @Override
    public Model processResetPassword(UserResetPwdWithTokenRequestModel resetRequest, Model model) {
        String token = resetRequest.getToken();
        String password = resetRequest.getPassword();



        //Hash token
        UserPasswordLessDTO userResponseModel = getByResetPasswordToken(token);



        model.addAttribute("title", "Reset your password");

        if (userResponseModel == null) {
            model.addAttribute("message", "Invalid Token");
        } else {
            updatePassword(password, token);

            model.addAttribute("message", "You have successfully changed your password.");
        }
        return model;
    }


    public void sendEmail(String recipientEmail, String link){
        Mail mail = Mail.builder()
                .message(format("Your verification link: %s", link))
                .subject("PetClinic e-mail verification")
                .to(recipientEmail)
                .build();

        mailService.sendMail(mail);
    }

    @Override
    public User getUserByEmail(String email) throws NotFoundException {
        log.info("getUserByEmail: trying to find user with email: {}", email);
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No account found for email: " + email));
    }
}
