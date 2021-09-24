package com.petclinic.auth.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserRepo userRepo;
    private final UserServiceImpl userServ;
    private final UserMapper userMapper;

    @PostMapping
    public User createUser(@RequestBody @Valid UserIDLessDTO dto) {

        log.info("Received user dto, trying to convert model");
        log.info("DTO info: { username={}, password={}, email={} }", dto.getUsername(), dto.getPassword(), dto.getEmail());
        final User user = userMapper.idLessDTOToModel(dto);
        log.info("Successfully converted dto -> model");

        log.info("Trying to persist user");
        final User saved = userRepo.save(user);
        log.info("Successfully persisted user");

        return saved;
    }

    @PutMapping
    public void passwordReset(@RequestParam long id, String pwd){

        log.info("id={}", id);
        try {
            userServ.passwordReset(pwd);
        } catch (EmptyResultDataAccessException e) {
            log.info("No user with id {}. Ignoring", id);
            return;
        }
        log.info("Password for User with id {}, reset", id);
    }
}