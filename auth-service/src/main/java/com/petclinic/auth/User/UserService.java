/**
 * Created by IntelliJ IDEA.
 *
 * User: @Trilikin21
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-64)
 *
 * User: @JordanAlbayrak
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-102)
 *
 * User: @Zellyk
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-104)
 *
 * User: @Fube
 * Date: 10/10/21
 * Ticket: feat(AUTH-CPC-357)
 *
 * User: @Fube
 * Date: 2021-10-14
 * Ticket: feat(AUTH-CPC-388)
 */

package com.petclinic.auth.User;

import com.petclinic.auth.Exceptions.IncorrectPasswordException;
import com.petclinic.auth.Exceptions.NotFoundException;
import com.petclinic.auth.Mail.Mail;
import com.petclinic.auth.User.data.User;
import com.petclinic.auth.User.data.UserIDLessRoleLessDTO;
import com.petclinic.auth.User.data.UserPasswordLessDTO;
import com.petclinic.auth.User.data.UserTokenPair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

public interface UserService {


    User createUser(UserIDLessRoleLessDTO user);

    User passwordReset(long id, String newPassword);

    User getUserById(long id);

    Page<User> findAll(PageRequest of);

    List<User> findAllWithoutPage();

    void deleteUser(long id);

    Mail generateVerificationMail(User user);

    UserPasswordLessDTO verifyEmailFromToken(String token);

    UserTokenPair login(UserIDLessRoleLessDTO user) throws IncorrectPasswordException;

    User getUserByEmail(String email) throws NotFoundException;
}
