/**
 * Created by IntelliJ IDEA.
 *
 * User: @MaxGrabs
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-13)
 *
 * User: @Trilikin21
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-64)
 *
 * User: @JordanAlbayrak
 * Date: 24/09/21
 * Ticket: feat(AUTH-CPC-102)
 *
 * User: @Zellyk
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-104)
 *
 */
package com.petclinic.auth.User;


import com.petclinic.auth.Role.Role;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class UserController {


    private final UserServiceImpl userServ;
    private final UserService userService;


    @GetMapping("/{id}")
    public User getUser(@PathVariable long id) throws NotFoundException {
        log.info("Getting user with id: {}" , id);
        return userService.getUserById(id);
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

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable long id){
        userService.deleteUser(id);
        log.info("Deleted role with id {}", id);
    }
}
