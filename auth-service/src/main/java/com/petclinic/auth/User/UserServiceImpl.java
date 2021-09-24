package com.petclinic.auth.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepo userRepo;
    private final UserMapper userMapper;

    @Override
<<<<<<< HEAD
    public User createUser(@Valid User user) {
=======
    public User createUser(@Valid UserIDLessDTO userIDLessDTO) {

        log.info("Saving user with username {}", userIDLessDTO.getUsername());
        User user = userMapper.idLessDTOToModel(userIDLessDTO);
        return userRepo.save(user);
>>>>>>> origin/main

        if (userRepo.findByEmail(user.getEmail()) == null){
            throw new DuplicateKeyException("Duplicate email for " + user.getEmail());
        }
        else {
            log.info("Saving user with username {}", user.getUsername());
            return userRepo.save(user);
        }
    }
}
