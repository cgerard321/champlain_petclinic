package com.petclinic.auth.User;

import com.petclinic.auth.Role.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserRepo userRepo;
    private final UserMapper userMapper;

    @PostMapping
    public User createUser(@RequestBody @Valid UserIDLessDTO dto) {
        final User saved;
        final User user;

        log.info("Received user dto, trying to convert model");
        log.info("DTO info: { username={}, password={}, email={} }", dto.getUsername(), dto.getPassword(), dto.getEmail());

        user = userMapper.idLessDTOToModel(dto);
        log.info("Successfully converted dto -> model");

        log.info("Trying to persist user");
        saved = userRepo.save(user);
        log.info("Successfully persisted user");

        return saved;
    }
}