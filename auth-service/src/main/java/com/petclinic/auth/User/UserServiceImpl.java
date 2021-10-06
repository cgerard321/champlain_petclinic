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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepo userRepo;
    private final UserMapper userMapper;

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
