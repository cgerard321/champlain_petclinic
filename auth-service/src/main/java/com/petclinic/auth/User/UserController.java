package com.petclinic.auth.User;


import javassist.NotFoundException;  
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
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
@Validated
public class UserController {


    private final UserServiceImpl userServ;
    private final UserService userService;


    @PostMapping
    public User createUser(@RequestBody @Valid UserIDLessDTO dto) {

        log.info("Trying to persist user");
        final User saved = userService.createUser(dto);
        log.info("Successfully persisted user");

        return saved;
    }

    @PutMapping("/{id}")
    public void passwordReset(@PathVariable long id,  @RequestBody String pwd) throws NotFoundException {

        userServ.passwordReset(id,pwd);
        log.info("Password for User with id {} with new password {}", id, pwd);
    }
}
