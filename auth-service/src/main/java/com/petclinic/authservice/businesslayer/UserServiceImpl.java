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


    @Value("${frontend.url}")
    private String gatewayOrigin;
    @Override
    public List<UserDetails> findAllWithoutPage() {
        return userMapper.modelToDetailsList(userRepo.findAll());
    }

@Override
    public List<UserDetails> getAllUsers() {
        List<User> users = userRepo.findAll();
        return userMapper.modelToDetailsList(users);
    }

    @Override
    public User createUser(@Valid UserIDLessRoleLessDTO userIDLessDTO) {

            final Optional<User> byEmail = userRepo.findByEmail(userIDLessDTO.getEmail());
            final Optional<User> byUsername = userRepo.findByUsername(userIDLessDTO.getUsername());

            if (byEmail.isPresent()) {
                throw new EmailAlreadyExistsException(
                        format("User with e-mail %s already exists", userIDLessDTO.getEmail()));
            }

            if (byUsername.isPresent()) {
                throw new IllegalArgumentException(
                        format("User with username %s already exists", userIDLessDTO.getUsername()));
            }

            User user = userMapper.idLessRoleLessDTOToModel(userIDLessDTO);

            if (userIDLessDTO.getDefaultRole() == null|| userIDLessDTO.getDefaultRole().isEmpty()){
            log.info("No default role provided, setting default role to OWNER");
            Optional<Role> role = roleRepo.findById(3L);
            Set<Role> roleSet = new HashSet<>();
            role.ifPresent(roleSet::add);
            user.setRoles(roleSet);
            }else{
                log.info("Default role provided, setting default role to {}", userIDLessDTO.getDefaultRole());
                Role role = roleRepo.findRoleByName(userIDLessDTO.getDefaultRole());
                Set<Role> roleSet = new HashSet<>();
                if(role == null)
                    throw new NotFoundException("No role with name: " + userIDLessDTO.getDefaultRole());
                roleSet.add(role);
                user.setRoles(roleSet);
            }
            user.setUserIdentifier(new UserIdentifier(userIDLessDTO.getUserId()));
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            log.info("Sending email to {}...", userIDLessDTO.getEmail());

            //Commented out the New emailing service and replaced it with the old emailing service as I implemented the new one but was told by Christine to revert back to the old one
            mailService.sendMail(generateVerificationMail(user));  //Old
            //generateVerificationMailWithNewEmailingService(user);          //New

            log.info("Email sent to {}", userIDLessDTO.getEmail());

            //////////////////////////////////////// BROKEN CODE -> Cart decided to add a code the create a cart and DIDN'T TEST IT! Turns out it breaks everything when trying to sign up :)
            //////////////////////////////////////// So I commented it out and notified the one who initially wrote this piece of code, the error should be fixed in a future pull request
//            User savedUser = userRepo.save(user);
            //CartResponse cartResponse = cartService.createCart(new CartRequest(savedUser.getUserIdentifier().getUserId()));
            ////////////////////////////////////////

            return userRepo.save(user);
    }

    @Override
    public User updateUser(String userId, UserPasswordLessDTO userPasswordLessDTO) {
        User existingUser = userRepo.findUserByUserIdentifier_UserId(userId);
        if (existingUser != null) {
            existingUser.setUserIdentifier(new UserIdentifier(userId));
            existingUser.setUsername(userPasswordLessDTO.getUsername());
            existingUser.setEmail(userPasswordLessDTO.getEmail());
            return userRepo.save(existingUser);
        } else {
            throw new NotFoundException("User not found for ID: " + userId);
        }
    }

    @Override
    public Mail generateVerificationMail(User user) {
        final String base64Token = Base64.getEncoder()
                .withoutPadding()
                .encodeToString(jwtService.generateToken(user).getBytes(StandardCharsets.UTF_8));

        String verificationLink = format("%s/verification/%s", gatewayOrigin, base64Token);
        String currentYear = String.valueOf(java.time.Year.now().getValue());

        String primaryColor = "#4A90E2";
        String secondaryColor = "#F5F7FA";
        String textColor = "#2C3E50";
        String lightText = "#7F8C8D";

        String htmlContent = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <style>" +
                "        @import url('https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600&display=swap');" +
                "    </style>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; font-family: 'Poppins', Arial, sans-serif; background-color: #f4f4f4; color: " + textColor + ";'>" +
                "    <div style='max-width: 600px; margin: 0 auto; background: #ffffff;'>" +
                "        <!-- Header -->" +
                "        <div style='background: " + primaryColor + "; padding: 30px 0; text-align: center; color: white;'>" +
                "            <h1 style='margin: 0; font-size: 24px; font-weight: 600;'>Pet Clinic</h1>" +
                "            <p style='margin: 5px 0 0; opacity: 0.9;'>Account Verification</p>" +
                "        </div>" +
                "        " +
                "        <!-- Main Content -->" +
                "        <div style='padding: 40px;'>" +
                "            <h1 style='color: " + primaryColor + "; margin-top: 0;'>Verify Your Email</h1>" +
                "            <p>Hello <strong>" + user.getUsername() + "</strong>,</p>" +
                "            <p>Welcome to Pet Clinic! We're thrilled to have you on board. To get started, please verify your email address by clicking the button below:</p>" +
                "            " +
                "            <!-- Verification Button -->" +
                "            <div style='margin: 40px 0; text-align: center;'>" +
                "                <a href='" + verificationLink + "' style='background: " + primaryColor + "; color: white; padding: 14px 28px; text-decoration: none; border-radius: 50px; font-weight: 600; font-size: 16px; display: inline-block; box-shadow: 0 4px 15px rgba(74, 144, 226, 0.3);'>" +
                "                    Verify Email Address" +
                "                </a>" +
                "            </div>" +
                "            " +
                "            <!-- Secondary Verification -->" +
                "            <div style='background: " + secondaryColor + "; padding: 20px; border-radius: 8px; margin: 30px 0;'>" +
                "                <p style='margin-top: 0;'><strong>Having trouble with the button?</strong></p>" +
                "                <p style='margin-bottom: 0;'>Copy and paste this link into your browser:</p>" +
                "                <p style='word-break: break-all; color: " + primaryColor + "; font-family: monospace;'>" + verificationLink + "</p>" +
                "            </div>" +
                "            " +
                "            <!-- Additional Information -->" +
                "            <div style='border-top: 1px solid #eee; padding-top: 20px; margin-top: 30px;'>" +
                "                <p style='font-size: 14px; color: " + lightText + ";'>If you didn't create an account with Pet Clinic, you can safely ignore this email.</p>" +
                "            </div>" +
                "        </div>" +
                "        " +
                "        <!-- Footer -->" +
                "        <div style='background: #f8f9fa; padding: 20px; text-align: center; font-size: 12px; color: " + lightText + "; border-top: 1px solid #eee;'>" +
                "            <p>© " + java.time.Year.now().getValue() + " Pet Clinic. All rights reserved.</p>" +
                "            <p style='margin-bottom: 0;'>This is an automated message, please do not reply.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";
        String plainText = "VERIFY YOUR PET CLINIC ACCOUNT\n\n" +
                "Hello " + user.getUsername() + ",\n\n" +
                "Welcome to Pet Clinic! We're excited to have you on board.\n\n" +
                "To complete your registration, please verify your email by clicking the link below:\n\n" +
                verificationLink + "\n\n" +
                "If the link doesn't work, copy and paste the URL into your web browser.\n\n" +
                "If you didn't create an account with Pet Clinic, you can safely ignore this email.\n\n" +
                "Best regards,\n" +
                "The Pet Clinic Team\n\n" +
                "© " + java.time.Year.now().getValue() + " Pet Clinic. All rights reserved.";

        return new Mail(
                user.getEmail(),
                "🔐 Verify Your Pet Clinic Account",
                "text/html",
                "Pet Clinic - Verify Your Email",
                htmlContent,
                plainText,
                user.getUsername(),
                "noreply@petclinic.com"
        );
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
    public void disableUser(String userId) {
        User user = userRepo.findUserByUserIdentifier_UserId(userId);
        if (user != null) {
            user.setDisabled(true);
            userRepo.save(user);
            log.info("User with ID {} has been disabled", userId);
        } else {
            throw new NotFoundException("No user found with userId: " + userId);
        }
    }

    @Override
    public void enableUser(String userId) {
        User user = userRepo.findUserByUserIdentifier_UserId(userId);
        if (user != null) {
            user.setDisabled(false);
            userRepo.save(user);
            log.info("User with ID {} has been enabled", userId);
        } else {
            throw new NotFoundException("No user found with userId: " + userId);
        }
    }

    @Override
    public HashMap<String,Object> login(UserIDLessUsernameLessDTO login) throws IncorrectPasswordException {
        User loggedInUser;
        //Create two variables to test if we can find by email or username
        //If the methods dont find anything they return null
        //The logged in user then becomes the one thats not null
        User usernameLogin = getUserbyUsername(login.getEmailOrUsername());
        User emailLogin = getUserByEmail(login.getEmailOrUsername());
        if (usernameLogin == null) {
            loggedInUser = emailLogin;
        }
        else {
            loggedInUser = usernameLogin;
        }



        if (loggedInUser == null) {
            throw new NotFoundException("User not found");
        }

        if (loggedInUser.isDisabled()) {
            throw new DisabledAccountException("Your account has been disabled. Please contact support.");
        }

        try {
            authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    login.getEmailOrUsername(), login.getPassword()
                            )
                    );

            if (!loggedInUser.isVerified()) {
                mailService.sendMail(generateVerificationMail(loggedInUser));
                throw new UnverifiedUserException("Your account is not verified ! A link has been sent to verify the account !");
            }

            ResponseCookie token = ResponseCookie.from(securityConst.getTOKEN_PREFIX(), jwtService.generateToken(loggedInUser))
                    .httpOnly(true)
                    .secure(true)
                    .maxAge(Duration.ofHours(1))
                    .path("/")
                    .sameSite("None")
                    .build();



            return new HashMap<>() {{
                put("token", token);
                put("user", loggedInUser);
            }};
        }
        catch (BadCredentialsException e){
            throw new IncorrectPasswordException("Incorrect username or password for user: " + login.getEmailOrUsername());
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

            // why :-;
            // String resetPasswordLink =  "http://localhost:8080/#!/reset_password/" + token;
            String resetPasswordLink =  userResetPwdRequestModel.getUrl() + token;
            sendEmailForgotPassword(email, resetPasswordLink);
        } catch (Exception ex) {
            throw new NotFoundException(ex.getMessage());
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
            throw new NotFoundException("Could not find any customer with the email " + email);
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


        Mail newMail = new Mail(
                recipientEmail, "Password Reset", "Default", "Reset Your Password - Verification Code",
                "Click on the following link to access the password reset page: " + link
                        + "Follow the on-screen instructions to create a new password for your account."
                        + "If you did not request this password reset, please disregard this email. Your account security is important to us, and no changes will be made without your verification.",
                "Thank you for choosing Pet Clinic.", "", "ChamplainPetClinic@gmail.com");
        mailService.sendMail(newMail);
    }

    @Override
    public User getUserByEmail(String email) throws NotFoundException {
        return userRepo.findByEmail(email).orElse(null);
    }

    @Override
    public UserPasswordLessDTO updateUserRole(String userId, RolesChangeRequestDTO roles, String token) {
        User existingUser = userRepo.findUserByUserIdentifier_UserId(userId);

        if(existingUser == null) {
            throw new NotFoundException("No user was found with id : " + userId);
        }


        if (userId.equals(jwtService.getIdFromToken(token)))
            throw new InvalidRequestException("You can't change your own roles !");



        existingUser.setId(existingUser.getId());
        existingUser.setUserIdentifier(new UserIdentifier(userId));

        Set<Role> newRoles = new HashSet<>();
        for (String role:
             roles.getRoles()) {
            Role newRole = roleRepo.findRoleByName(role);
            if (newRole == null)
                throw new NotFoundException("Role was not found with name : " + newRole);
            newRoles.add(newRole);
        }


        existingUser.setRoles(newRoles);

        return userMapper.modelToPasswordLessDTO(userRepo.save(existingUser));
    }
    public User getUserByUserId(String userId) {
        return userRepo.findOptionalUserByUserIdentifier_UserId(userId)
                .orElseThrow(() -> new NotFoundException("No user with userId: " + userId));
    }

    @Override
    public void deleteUser(String userId) {
        User user = userRepo.findUserByUserIdentifier_UserId(userId);
        if (user != null) {
            userRepo.delete(user);
        } else {
            throw new NotFoundException("No user with userId: " + userId);
        }
    }


    @Override
    public List<UserDetails> getUsersByUsernameContaining(String username) {
        return userMapper.modelToDetailsList(userRepo.findByUsernameContaining(username));
    }

    @Override
    public String updateUserUsername(String userId, String username, String token) {
        User existingUser = userRepo.findUserByUserIdentifier_UserId(userId);

        if(existingUser == null) {
            throw new NotFoundException("No user was found with id : " + userId);
        }
        username = username.replace("{\"username\":\"", "");
        username = username.replace("\"}", "");
        existingUser.setUsername(username);
        userRepo.save(existingUser);
        return existingUser.getUsername();
    }

    @Override
    public User getUserbyUsername(String username) throws NotFoundException {
      return userRepo.findByUsername(username).orElse(null);
    }

}
