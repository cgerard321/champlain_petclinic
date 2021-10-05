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
 */
package com.petclinic.auth.User;

import javassist.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

public interface UserService {


    User createUser(UserIDLessDTO user);

    User passwordReset(long id, String passwd);

    User findUserById(long id);

    Page<User> findAll(PageRequest of);

    void deleteUser(long id);
}
