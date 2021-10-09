package com.petclinic.auth.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.petclinic.auth.Exceptions.NotFoundException;

import javax.validation.Valid;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserById(long id) {
        User entity  = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("No user found for userID" + id));
        log.info("User getUserById: found userId: {}", entity.getId());
        return entity;
    }

    @Override
    public Page<User> findAll(PageRequest of) {
        return userRepo.findAll(of);
    }

    @Override
    public User createUser(@Valid UserIDLessDTO userIDLessDTO) {

        log.info("Saving user with email {}", userIDLessDTO.getEmail());
        User user = userMapper.idLessDTOToModel(userIDLessDTO);
        return userRepo.save(user);

    }

    @Override
    public User passwordReset(long userId, @Valid String newPassword) {

        log.info("id={}", userId);
        User user = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("No user for id:" + userId));
        user.setPassword(newPassword);
        return userRepo.save(user);

    }

    @Override
    public void deleteUser(long userId) {
        log.info("deleteUser: trying to delete entity with userId: {}", userId);
        userRepo.findById(userId).ifPresent(userRepo::delete);
    }
}
