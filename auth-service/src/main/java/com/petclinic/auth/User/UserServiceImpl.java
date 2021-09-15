package com.petclinic.auth.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    public static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepo repository;


    public UserServiceImpl(UserRepo repository) {
        this.repository = repository;
    }

    @Override
    public User signup(User user) {


        return null;
    }
}
