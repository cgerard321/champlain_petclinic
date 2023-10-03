/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.authservice.businesslayer;

import com.petclinic.authservice.Util.Exceptions.*;
import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.roles.RoleRepo;
import com.petclinic.authservice.datamapperlayer.UserMapper;
import com.petclinic.authservice.domainclientlayer.Mail.Mail;
import com.petclinic.authservice.domainclientlayer.Mail.MailService;
import com.petclinic.authservice.presentationlayer.User.*;
import com.petclinic.authservice.security.JwtTokenUtil;
import com.petclinic.authservice.security.SecurityConst;
import com.petclinic.authservice.datalayer.user.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;

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
    private final RoleRepo roleRepo;
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
    public List<UserDetails> findAllWithoutPage() {
        return userMapper.modelToDetailsList(userRepo.findAll());
    }

    @Override
    public User createUser(@Valid UserIDLessRoleLessDTO userIDLessDTO) {

            final Optional<User> byEmail = userRepo.findByEmail(userIDLessDTO.getEmail());

            if (byEmail.isPresent()) {
                throw new EmailAlreadyExistsException(
                        format("User with e-mail %s already exists", userIDLessDTO.getEmail()));
            }

            User user = userMapper.idLessRoleLessDTOToModel(userIDLessDTO);

            Optional<Role> role = roleRepo.findById(3L);
            Set<Role> roleSet = new HashSet<>();
            role.ifPresent(roleSet::add);
            user.setRoles(roleSet);
            user.setUserIdentifier(new UserIdentifier(userIDLessDTO.getUserId()));
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            log.info("Sending email to {}...", userIDLessDTO.getEmail());
            log.info(mailService.sendMail(generateVerificationMail(user)));
            log.info("Email sent to {}", userIDLessDTO.getEmail());

            return userRepo.save(user);

    }

//    @Override
//    public void deleteUser
//            (long userId) {
//        log.info("deleteUser: trying to delete entity with userId: {}", userId);
//        userRepo.findById(userId).ifPresent(userRepo::delete);
//    }

    @Override
    public Mail generateVerificationMail(User user) {
        final String base64Token = Base64.getEncoder()
                .withoutPadding()
                .encodeToString(jwtService.generateToken(user).getBytes(StandardCharsets.UTF_8));

        // Remove dangling . in case of empty sub
        String niceSub = gatewaySubdomain.length() > 0 ? gatewaySubdomain + "." : "";

        String email = format("""
                     <!DOCTYPE html>
                     <html lang="en">
                     <head>
                         <meta charset="UTF-8">
                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
                         <title>Email Verification</title>
                         <style>
                             body {
                                 font-family: Arial, sans-serif;
                                 background-color: #f4f4f4;
                                 margin: 0;
                                 padding: 0;
                             }
                             .container {
                                 max-width: 600px;
                                 margin: 0 auto;
                                 padding: 20px;
                                 background-color: #fff;
                                 border-radius: 5px;
                                 box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
                             }
                             h1 {
                                 color: #333;
                             }
                             p {
                                 color: #555;
                             }
                             a {
                                 color: #007BFF;
                             }
                         </style>
                     </head>
                     <body>
                         <div class="container">
                             <h1>Thank you for Signing Up with us - Verify your email address</h1>
                             <h3>We have received a request to create an account for Pet Clinic from this email.</h3>
                            \s
                             <ol>
                                 <li>Click on the following link to verify your identity: <a href="%s://%s%s/verification/%s">Verify Email</a></li>
                             </ol>
                            \s
                             <p>If you do not wish to create an account, please disregard this email.</p>
                            \s
                             <p>Thank you for choosing Pet Clinic.</p>
                         </div>
                     </body>
                     </html>
                     """, gatewayProtocol, niceSub, gatewayOrigin, base64Token);

        return Mail.builder()
                .message(email)
                .subject("PetClinic e-mail verification")
                .to(user.getEmail())
                .build();

//                .message(format("Your verification link: %s://%s%s/verification/%s",
//                gatewayProtocol, niceSub, gatewayOrigin, base64Token))
    }

    @Override
    public UserPasswordLessDTO verifyEmailFromToken(String token) {

        final Optional<User> decryptUser = userRepo.findByUsername(jwtService.getUsernameFromToken(token));
        if (decryptUser.isEmpty()) {
            throw new NotFoundException("User not found");
        }
        decryptUser.get().setVerified(true);
        final User save = userRepo.save(decryptUser.get());
        return userMapper.modelToPasswordLessDTO(save);
    }


    @Override
    public HashMap<String,Object> login(UserIDLessUsernameLessDTO login) throws IncorrectPasswordException {
        User loggedInUser = getUserByEmail(login.getEmail());

        if (loggedInUser == null) {
            throw new NotFoundException("User not found");
        }


        try {
            authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    login.getEmail(), login.getPassword()
                            )
                    );

            if (!loggedInUser.isVerified()) {
                mailService.sendMail(generateVerificationMail(loggedInUser));
                throw new UnverifiedUserException("Your account is not verified ! A link has been sent to verify the account !");
            }

            ResponseCookie token = ResponseCookie.from(securityConst.getTOKEN_PREFIX(), jwtService.generateToken(loggedInUser))
                    .httpOnly(true)
                    .secure(true)
                    .path("/api/gateway")
                    .maxAge(Duration.ofHours(1))
                    .sameSite("Lax").build();



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
    public void processForgotPassword(UserResetPwdRequestModel userResetPwdRequestModel) {
        String email = userResetPwdRequestModel.getEmail();
        String token = UUID.randomUUID().toString();
        try {
            getUserByEmail(email);
        }
        catch(RuntimeException e){
            throw new NotFoundException("Could not find any customer with the email " + email);
        }

        try {
            updateResetPasswordToken(token, email);


            String resetPasswordLink =  "http://localhost:8080/#!/reset_password/" + token;
            sendEmailForgotPassword(email, resetPasswordLink);
        } catch (Exception ex) {
            throw new InvalidInputException(ex.getMessage());
        }
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
        String hashedToken = BCrypt.hashpw(token, salt);
        ResetPasswordToken resetPasswordToken = tokenRepository.findResetPasswordTokenByToken(hashedToken);
        if (resetPasswordToken == null) {
            throw new InvalidBearerTokenException("Token not found");
        }

        final Calendar cal = Calendar.getInstance();
        Optional<User> user =userRepo.findById(resetPasswordToken.getUserIdentifier());
        if(resetPasswordToken.getExpiryDate().after(cal.getTime()) && user.isPresent())
            return userMapper.modelToPasswordLessDTO(user.get());
        else
            throw new IllegalArgumentException("Token is expired (in getByResetPasswordToken()");
    }


    @Override
    public void updatePassword(String newPassword, String token) {

        final Calendar cal = Calendar.getInstance();
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
    public void processResetPassword(UserResetPwdWithTokenRequestModel resetRequest) {
        String token = resetRequest.getToken();
        String password = resetRequest.getPassword();
        UserPasswordLessDTO userResponseModel = getByResetPasswordToken(token);




        if (userResponseModel == null) {
            throw new InvalidInputException("Invalid token");
        } else {
            updatePassword(password, token);
        }
    }


    public void sendEmailForgotPassword(String recipientEmail, String link){

         String email = format("""
                     <!DOCTYPE html>
                     <html lang="en">
                     <head>
                         <meta charset="UTF-8">
                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
                         <title>Password Reset</title>
                         <style>
                             body {
                                 font-family: Arial, sans-serif;
                                 background-color: #f4f4f4;
                                 margin: 0;
                                 padding: 0;
                             }
                             .container {
                                 max-width: 600px;
                                 margin: 0 auto;
                                 padding: 20px;
                                 background-color: #fff;
                                 border-radius: 5px;
                                 box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
                             }
                             h1 {
                                 color: #333;
                             }
                             p {
                                 color: #555;
                             }
                             a {
                                 color: #007BFF;
                             }
                         </style>
                     </head>
                     <body>
                         <div class="container">
                             <h1>Reset Your Password - Verification Code</h1>
                             <h3>We have received a request to reset your password for your Pet Clinic account. To ensure the security of your account, please follow the instructions below to reset your password.</h3>
                            \s
                             <ol>
                                 <li>Click on the following link to access the password reset page: <a href="%s">Reset Password</a></li>
                                 <li>Follow the on-screen instructions to create a new password for your account.</li>
                             </ol>
                            \s
                             <p>If you did not request this password reset, please disregard this email. Your account security is important to us, and no changes will be made without your verification.</p>
                            \s
                             <p>Thank you for choosing Pet Clinic.</p>
                         </div>
                     </body>
                     </html>
                     """, link);

        Mail mail = Mail.builder()
                .message(email)
                .subject("PetClinic forgot password")
                .to(recipientEmail)
                .build();

        mailService.sendMail(mail);
    }


    @Override
    public User getUserByEmail(String email) throws NotFoundException {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("No account found for email: " + email));
    }
}
