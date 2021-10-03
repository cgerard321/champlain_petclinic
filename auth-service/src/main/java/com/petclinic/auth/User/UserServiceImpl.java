/**
 * Created by IntelliJ IDEA.
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


import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepo userRepo;
    private final UserMapper userMapper;

    @Override
    public User getUserById(long id) throws NotFoundException {
        User entity = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("No user was found for userId: " + id));
        log.info("User getUserById: found userId: {}", entity.getId());
        return entity;
    }

    @Override
    public Page<User> findAll(PageRequest of) {
        return userRepo.findAll(of);
    }

    @Override
    public void deleteUser(long id) {
        try {
            log.info("Deleting user with id {}", id);
            userRepo.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            log.info("No user with id {}. Ignoring", id);
        }
    }

    @Override
    public User createUser(@Valid UserIDLessDTO userIDLessDTO) {

        if (userRepo.findByEmail(userIDLessDTO.getEmail()) != null){
            throw new DuplicateKeyException("Duplicate email for " + userIDLessDTO.getEmail());
        }
        else {
            log.info("Saving user with username {}", userIDLessDTO.getUsername());
            User user = userMapper.idLessDTOToModel(userIDLessDTO);
            return userRepo.save(user);
        }
    }

    public User passwordReset(long id, String passwd) throws NotFoundException {

        log.info("id={}", id);
        User user = userRepo.findById(id).orElseThrow(() -> new NotFoundException("No user for id:" + id));
        user.setPassword(passwd);
        return userRepo.save(user);

    }
}
