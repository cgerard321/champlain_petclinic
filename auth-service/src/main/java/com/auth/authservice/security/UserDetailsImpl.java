package com.auth.authservice.security;

import com.auth.authservice.datalayer.user.User;
import com.auth.authservice.datalayer.user.UserRepo;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsImpl implements UserDetailsService {

    private final UserRepo userRepo;

    public UserDetailsImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserPrincipalImpl loadUserByUsername(String email) {
        Optional<User> user = userRepo.findByEmail(email);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException(email);
        }
        return new UserPrincipalImpl(user);
    }


}
