package com.ecommerce.aplication.services;

import com.ecommerce.aplication.records.UsersRecords.DataPasswordChanged;
import com.ecommerce.aplication.records.UsersRecords.DataUsersRegister;
import com.ecommerce.infra.exceptions.BusinessRuleException;
import com.ecommerce.model.repositorys.UsersRepositroy;
import com.ecommerce.model.users.TypeRole;
import com.ecommerce.model.users.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceUsersTest {
    @Mock
    private UsersRepositroy usersRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private ServiceAsync serviceAsync;

    @InjectMocks
    private ServiceUsers serviceUsers;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_Success() {
        String email = "test@example.com";
        Users user = new Users();
        user.setEmail(email);
        user.setAtivo(true);

        when(usersRepository.findByEmailAndAtivoTrue(email)).thenReturn(Optional.of(user));

        Users result = (Users) serviceUsers.loadUserByUsername(email);

        assertEquals(email, result.getEmail());
        verify(usersRepository).findByEmailAndAtivoTrue(email);
    }

    @Test
    void loadUserByUsername_NotFound() {
        String email = "notfound@example.com";
        when(usersRepository.findByEmailAndAtivoTrue(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> serviceUsers.loadUserByUsername(email));
    }

    @Test
    void registerUser_Success() {
        DataUsersRegister data = new DataUsersRegister("User", "user@example.com", "password", TypeRole.CLIENTE);

        when(usersRepository.findByEmail(data.email())).thenReturn(Optional.empty());
        when(encoder.encode(data.password())).thenReturn("encodedPass");
        Users savedUser = new Users();
        savedUser.setId(1L);
        when(usersRepository.save(any())).thenReturn(savedUser);

        Long id = serviceUsers.registerUser(data);

        assertEquals(1L, id);
        verify(serviceAsync).sendWelcomeEmail(any());
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        DataUsersRegister data = new DataUsersRegister("User", "user@example.com", "password", TypeRole.CLIENTE);
        when(usersRepository.findByEmail(data.email())).thenReturn(Optional.of(new Users()));

        assertThrows(BusinessRuleException.class, () -> serviceUsers.registerUser(data));
    }

    @Test
    void changePassword_Success() {
        Users user = new Users();
        user.setId(1L);
        user.setAtivo(true);
        user.setPassword("encodedOldPass");

        DataPasswordChanged data = new DataPasswordChanged("oldPass", "newPass", "newPass");

        when(encoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
        when(encoder.encode("newPass")).thenReturn("encodedNewPass");
        when(usersRepository.save(user)).thenReturn(user);

        serviceUsers.changePassword(data, user);

        verify(usersRepository).save(user);
        assertEquals("encodedNewPass", user.getPassword());
    }

    @Test
    void changePassword_IncorrectCurrentPassword() {
        Users user = new Users();
        user.setId(1L);
        user.setAtivo(true);
        user.setPassword("encodedOldPass");

        DataPasswordChanged data = new DataPasswordChanged("wrongOld", "newPass", "newPass");

        when(encoder.matches("wrongOld", "encodedOldPass")).thenReturn(false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> serviceUsers.changePassword(data, user));
        assertEquals("Senha atual está incorreta.", ex.getMessage());
    }

    @Test
    void changePassword_UserInactive() {
        Users user = new Users();
        user.setAtivo(false);
        DataPasswordChanged data = new DataPasswordChanged("oldPass", "newPass", "newPass");

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> serviceUsers.changePassword(data, user));
        assertEquals("Usuário inativo não pode alterar senha.", ex.getMessage());
    }

    @Test
    void changePassword_NewPasswordsDontMatch() {
        Users user = new Users();
        user.setAtivo(true);
        user.setPassword("encodedOldPass");

        DataPasswordChanged data = new DataPasswordChanged("oldPass", "newPass1", "newPass2");

        when(encoder.matches("oldPass", "encodedOldPass")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> serviceUsers.changePassword(data, user));
        assertEquals("Nova senha e confirmação não conferem.", ex.getMessage());
    }

    @Test
    void delete_Success() {
        Users user = new Users();
        user.setId(1L);
        user.setAtivo(true);

        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));
        when(usersRepository.save(user)).thenReturn(user);

        serviceUsers.delete(1L);

        assertFalse(user.isAtivo());
        verify(usersRepository).save(user);
    }

    @Test
    void delete_UserNotFound() {
        when(usersRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> serviceUsers.delete(1L));
    }

    @Test
    void delete_AlreadyInactive() {
        Users user = new Users();
        user.setId(1L);
        user.setAtivo(false);

        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> serviceUsers.delete(1L));
        assertEquals("Usuário já está inativo.", ex.getMessage());
    }

    @Test
    void findById_Found() {
        Users user = new Users();
        user.setId(1L);
        user.setAtivo(true);

        when(usersRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(user));

        Optional<Users> result = serviceUsers.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findById_NotFound() {
        when(usersRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.empty());

        Optional<Users> result = serviceUsers.findById(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void save_Success() {
        Users user = new Users();
        user.setId(1L);

        when(usersRepository.save(user)).thenReturn(user);

        Users saved = serviceUsers.save(user);
        assertEquals(user, saved);
    }

    @Test
    void save_NullUser() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> serviceUsers.save(null));
        assertEquals("Usuário não pode ser nulo", ex.getMessage());
    }

    @Test
    void findEmailById_Success() {
        when(usersRepository.findEmailById(1L)).thenReturn(Optional.of("email@example.com"));

        String email = serviceUsers.findEmailById(1L);
        assertEquals("email@example.com", email);
    }

    @Test
    void findEmailById_NotFound() {
        when(usersRepository.findEmailById(1L)).thenReturn(Optional.empty());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> serviceUsers.findEmailById(1L));
        assertEquals("Usuário não encontrado com ID: 1", ex.getMessage());
    }
  
}