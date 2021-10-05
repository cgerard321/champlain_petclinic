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
