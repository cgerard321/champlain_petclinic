package com.petclinic.auth.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import javax.validation.Valid;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepo userRepo;
    private final UserMapper userMapper;

    @Override
    public User createUser(@Valid UserIDLessDTO userIDLessDTO) {

        log.info("Saving user with username {}", userIDLessDTO.getUsername());
        User user = userMapper.idLessDTOToModel(userIDLessDTO);
        return userRepo.save(user);

    }
}
