package com.petclinic.auth.User;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public User createUser(@RequestBody @Valid UserIDLessDTO dto) {

        log.info("Creating user with username {}", dto.getUsername());
        return userService.createUser(dto);

    }

    @PutMapping("/{id}")
    public void passwordReset(@PathVariable long id,  @RequestBody String pwd) throws Exception {

        log.info("changing password {} with userId {} ", id, pwd);
        userService.passwordReset(id,pwd);

    }
}
