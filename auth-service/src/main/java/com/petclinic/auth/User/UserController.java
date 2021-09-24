package com.petclinic.auth.User;

<<<<<<< HEAD
=======
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.petclinic.auth.Role.Role;
>>>>>>> origin/main
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
<<<<<<< HEAD
    private final UserMapper userMapper;
=======
>>>>>>> origin/main

    @PostMapping
    public User createUser(@RequestBody @Valid UserIDLessDTO dto) {
        final User saved;
        final User user;

        log.info("Received user dto, trying to convert model");
        log.info("DTO info: { username={}, password={}, email={} }", dto.getUsername(), dto.getPassword(), dto.getEmail());
<<<<<<< HEAD

        user = userMapper.idLessDTOToModel(dto);
        log.info("Successfully converted dto -> model");

        log.info("Trying to persist user");
        saved = userService.createUser(user);
=======

        log.info("Trying to persist user");
        final User saved = userService.createUser(dto);
>>>>>>> origin/main
        log.info("Successfully persisted user");

        return saved;
    }
}