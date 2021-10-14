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
 */
package com.petclinic.auth.User;

import com.petclinic.auth.Exceptions.IncorrectPasswordException;
import com.petclinic.auth.Mail.Mail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface UserService {


    User createUser(UserIDLessRoleLessDTO user);

    User passwordReset(long id, String newPassword);

    User getUserById(long id);

    Page<User> findAll(PageRequest of);

    void deleteUser(long id);

    Mail generateVerificationMail(User user);

    UserPasswordLessDTO verifyEmailFromToken(String token);

    String login(UserIDLessRoleLessDTO user) throws IncorrectPasswordException;
}
