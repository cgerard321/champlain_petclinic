/**
 * Created by IntelliJ IDEA.
 * <p>
 * User: @Trilikin21
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-64)
 * <p>
 * User: @JordanAlbayrak
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-102)
 * <p>
 * User: @Zellyk
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-104)
 * <p>
 * User: @Fube
 * Date: 10/10/21
 * Ticket: feat(AUTH-CPC-357)
 * <p>
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.authservice.businesslayer;

import com.petclinic.authservice.Util.Exceptions.IncorrectPasswordException;
import com.petclinic.authservice.Util.Exceptions.NotFoundException;
import com.petclinic.authservice.datalayer.user.User;
import com.petclinic.authservice.domainclientlayer.Mail.Mail;
import com.petclinic.authservice.presentationlayer.User.*;

import java.util.HashMap;
import java.util.List;

public interface UserService {

    List<UserDetails> getAllUsers();

    User createUser(UserIDLessRoleLessDTO user);

    User updateUser(String userID, UserPasswordLessDTO userPasswordLessDTO);

    List<UserDetails> findAllWithoutPage();

//    NewMail generateVerificationMail(User user);

    Mail generateVerificationMail(User user);

    UserPasswordLessDTO verifyEmailFromToken(String token);

    HashMap<String, Object> login(UserIDLessUsernameLessDTO user) throws IncorrectPasswordException;

    User getUserByEmail(String email) throws NotFoundException;

    User getUserByUserId(String userId);

    void deleteUser(String userId);

    List<UserDetails> getUsersByUsernameContaining(String username);

    void processForgotPassword(UserResetPwdRequestModel userResetPwdWithTokenRequestModel);

    void updateResetPasswordToken(String token, String email);

    UserPasswordLessDTO getByResetPasswordToken(String token);

    void updatePassword(String newPassword, String token);


    void disableUser(String userId);
    void enableUser(String userId);

    void processResetPassword(UserResetPwdWithTokenRequestModel resetRequest);
    UserPasswordLessDTO updateUserRole(String userId, RolesChangeRequestDTO roles, String token);
    public String updateUserUsername(String userId, String username, String token);

    User getUserbyUsername(String username);

}
