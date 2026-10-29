package com.petclinic.authservice.businesslayer;

import com.petclinic.authservice.Util.Exceptions.NotFoundException;
import com.petclinic.authservice.datalayer.roles.Role;
import com.petclinic.authservice.datalayer.roles.RoleRepo;
import com.petclinic.authservice.datalayer.user.ResetPasswordTokenRepository;
import com.petclinic.authservice.datalayer.user.User;
import com.petclinic.authservice.datalayer.user.UserRepo;
import com.petclinic.authservice.datamapperlayer.UserMapper;
import com.petclinic.authservice.domainclientlayer.Mail.MailService;
import com.petclinic.authservice.presentationlayer.User.UserIDLessRoleLessDTO;
import com.petclinic.authservice.security.JwtTokenUtil;
import com.petclinic.authservice.security.SecurityConst;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceCreateUserRoleTest {

    @Mock private SecurityConst securityConst;
    @Mock private ResetPasswordTokenRepository tokenRepository;
    @Mock private UserRepo userRepo;
    @Mock private RoleRepo roleRepo;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private MailService mailService;
    @Mock private JwtTokenUtil jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserServiceImpl userService;

    @ParameterizedTest
    @NullAndEmptySource
    void createUser_withoutDefaultRole_assignsExistingCustomerRole(String defaultRole) {
        UserIDLessRoleLessDTO request = prepareRequest(defaultRole);
        Role customerRole = Role.builder().id(42L).name("CUST").build();
        when(roleRepo.findRoleByName("CUST")).thenReturn(customerRole);
        prepareSuccessfulCreation(request);

        User result = userService.createUser(request);

        assertSavedCustomerRole(result, customerRole);
        verify(roleRepo).findRoleByName("CUST");
        verify(roleRepo, never()).findById(any());
    }

    @Test
    void createUser_withExplicitCustomerRole_resolvesAndAssignsExistingRole() {
        UserIDLessRoleLessDTO request = prepareRequest("CUST");
        Role customerRole = Role.builder().id(42L).name("CUST").build();
        when(roleRepo.findRoleByName("CUST")).thenReturn(customerRole);
        prepareSuccessfulCreation(request);

        User result = userService.createUser(request);

        assertSavedCustomerRole(result, customerRole);
        verify(roleRepo).findRoleByName("CUST");
        verify(roleRepo, never()).findById(any());
    }

    @Test
    void createUser_withUnknownExplicitRole_rejectsBeforeSavingOrSendingMail() {
        UserIDLessRoleLessDTO request = prepareRequest("NOT_A_ROLE");
        when(roleRepo.findRoleByName("NOT_A_ROLE")).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.createUser(request));

        assertEquals("No role with name: NOT_A_ROLE", exception.getMessage());
        verify(roleRepo).findRoleByName("NOT_A_ROLE");
        verify(roleRepo, never()).findById(any());
        verify(userRepo, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, mailService, jwtService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createUser_withoutDefaultRoleAndMissingCustomerRole_rejectsBeforeSideEffects(String defaultRole) {
        UserIDLessRoleLessDTO request = prepareRequest(defaultRole);
        when(roleRepo.findRoleByName("CUST")).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.createUser(request));

        assertEquals("No role with name: CUST", exception.getMessage());
        verify(roleRepo).findRoleByName("CUST");
        verify(roleRepo, never()).findById(any());
        verify(userRepo, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder, mailService, jwtService, tokenRepository, authenticationManager);
    }

    private UserIDLessRoleLessDTO prepareRequest(String defaultRole) {
        UserIDLessRoleLessDTO request = UserIDLessRoleLessDTO.builder()
                .userId("7c0d42c2-0c2d-41ce-bd9c-6ca67478956f")
                .username("Ricky")
                .email("customer@example.com")
                .password("pwd%jfjfjDkkkk8")
                .defaultRole(defaultRole)
                .build();
        when(userRepo.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepo.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userMapper.idLessRoleLessDTOToModel(request)).thenReturn(User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
                .build());
        return request;
    }

    private void prepareSuccessfulCreation(UserIDLessRoleLessDTO request) {
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(jwtService.generateToken(any(User.class))).thenReturn("a.fake.token");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void assertSavedCustomerRole(User result, Role customerRole) {
        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepo).save(savedUser.capture());
        assertSame(result, savedUser.getValue());
        assertEquals(Set.of(customerRole), savedUser.getValue().getRoles());
        Role assignedRole = savedUser.getValue().getRoles().iterator().next();
        assertEquals(42L, assignedRole.getId());
        assertEquals("CUST", assignedRole.getName());
    }
}
