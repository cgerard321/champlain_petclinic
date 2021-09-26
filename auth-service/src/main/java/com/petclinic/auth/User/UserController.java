package com.petclinic.auth.User;


import javassist.NotFoundException;  
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    private final UserService userService;


    @PostMapping
    public User createUser(@RequestBody @Valid UserIDLessDTO dto) {

        log.info("Received user dto, trying to convert model");
        log.info("DTO info: { username={}, password={}, email={} }", dto.getUsername(), dto.getPassword(), dto.getEmail());
        log.info("Trying to persist user");
        final User saved = userService.createUser(dto);
        log.info("Successfully persisted user");

        return saved;
    }

    @PutMapping("/{id}")
    public void passwordReset(@PathVariable long id,  @RequestBody String pwd){


        log.info("id={}", id);
        try {
            userServ.passwordReset(id,pwd);
        } catch (NotFoundException e) {
            log.info("No user with id {}. Ignoring", id);
            return;
        }
        log.info("Password for User with id {}, reset", id);
    }
}
