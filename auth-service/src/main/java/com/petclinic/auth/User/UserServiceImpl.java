package com.petclinic.auth.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
public class UserServiceImpl implements UserService{

    @Override
    public User createUser(@Valid UserIDLessDTO user) {

        return new User();
    }
}
