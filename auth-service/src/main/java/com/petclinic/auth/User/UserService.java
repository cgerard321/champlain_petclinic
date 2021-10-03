package com.petclinic.auth.User;

import javassist.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface UserService {


    User createUser(UserIDLessDTO user);

    User passwordReset(long id, String passwd) throws NotFoundException;

    User getUserById(long id) throws NotFoundException;

    Page<User> findAll(PageRequest of);

    void deleteUser(long id);
}
