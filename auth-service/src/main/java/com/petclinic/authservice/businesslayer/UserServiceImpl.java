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

        // Remove dangling . in case of empty sub
//        String niceSub = gatewaySubdomain.length() > 0 ? gatewaySubdomain + "." : "";

        String formatedLink = format("<a class=\"email-button\" href=\"%s/verification/%s\">Verify Email</a>", gatewayOrigin, base64Token);

        return new Mail(
                user.getEmail(), "Verification Email", "Default", "Pet clinic - Verification Email",
                "Thank you for Signing Up with us.\n" +
                        "We have received a request to create an account for Pet Clinic from this email.\n\n" +
                        "Click on the following button to verify your identity: " + formatedLink + "\n\n\n" +
                        "If you do not wish to create an account, please disregard this email.",
                "Thank you for choosing Pet Clinic.", user.getUsername(), "ChamplainPetClinic@gmail.com");



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
        User usernameLogin = getUserbyUsername(login.getEmail());
        User emailLogin = getUserByEmail(login.getEmail());
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
            throw new IncorrectPasswordException("Incorrect username or password for user: " + login.getEmail());
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
