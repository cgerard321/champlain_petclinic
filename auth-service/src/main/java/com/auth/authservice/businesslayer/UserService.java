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

package com.auth.authservice.businesslayer;

import com.auth.authservice.Util.Exceptions.IncorrectPasswordException;
import com.auth.authservice.Util.Exceptions.NotFoundException;
import com.auth.authservice.datalayer.user.User;
import com.auth.authservice.domainclientlayer.Mail.Mail;
import com.auth.authservice.presentationlayer.User.UserIDLessRoleLessDTO;
import com.auth.authservice.presentationlayer.User.UserPasswordLessDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.nio.file.attribute.UserPrincipal;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public interface UserService {


    User createUser(UserIDLessRoleLessDTO user);

    User passwordReset(long id, String newPassword);

    User getUserById(long id);

    Page<User> findAll(PageRequest of);

    List<User> findAllWithoutPage();

    void deleteUser(long id);

    Mail generateVerificationMail(User user);

    UserPasswordLessDTO verifyEmailFromToken(String token);

    HashMap<String, Object> login(UserIDLessRoleLessDTO user) throws IncorrectPasswordException;


    User getUserByEmail(String email) throws NotFoundException;
}
