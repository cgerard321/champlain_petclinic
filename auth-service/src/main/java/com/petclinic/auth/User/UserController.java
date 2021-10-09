package com.petclinic.auth.User;

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

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/{userId}")
    public User getUser(@PathVariable long userId) {
        log.info("Getting user with id: {}", userId);
        return userService.getUserById(userId);
    }

    @GetMapping
    public Page<User> getAllUsers(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {

        log.info("page={}", page);
        final Page<User> all = userService.findAll(PageRequest.of(page - 1, size));
        log.info("Retrieved paginated result with {} entries and {} pages", all.getTotalElements(), all.getTotalPages());
        return all;
    }

    @PostMapping
    public UserIDLessPasswordLessDTO createUser(@RequestBody @Valid UserIDLessDTO dto) {

        log.info("Trying to persist user");
        final User saved = userService.createUser(dto);
        log.info("Successfully persisted user");

        return userMapper.modelToIDLessPasswordLessDTO(saved);
    }

    @PutMapping("/{userId}")
    public void passwordReset(@PathVariable long userId, @RequestBody String newPassword) {

        userService.passwordReset(userId, newPassword);
        log.info("Password for User with id {} with new password {}", userId, newPassword);
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
        log.info("Deleted role with id {}", userId);
    }
}
